package winylka.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import winylka.dto.*;
import winylka.event.OrderShippedEvent;
import winylka.infra.OrderRepository;
import winylka.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final ProductService products;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(ProductService products, OrderRepository orderRepository, ApplicationEventPublisher eventPublisher) {
        this.products = products;
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<AdminOrderListItemResponse> findAllForAdmin() {
        return orderRepository.findAllForAdmin()
                .stream()
                .map(order -> {
                    int itemsCount = order.getItems()
                            .stream()
                            .mapToInt(CustomerOrderItem::getAmount)
                            .sum();

                    return new AdminOrderListItemResponse(
                            order.getId(),
                            order.getCreatedAt(),
                            order.getCustomerFullName(),
                            order.getCustomerEmail(),
                            itemsCount,
                            order.getItemsTotal(),
                            order.getStatus()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminOrderDetailsResponse findAdminOrderById(long id) {

        CustomerOrder order = orderRepository.findDetailedById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found: " + id));

        return toAdminDetails(order);
    }

    @Transactional
    public AdminOrderDetailsResponse ship(long id, String trackingNumber) {
        CustomerOrder order = orderRepository.findDetailedById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found: " + id
                        )
                );

        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new IllegalStateException(
                    "Order has already been shipped"
            );
        }

        order.setStatus(OrderStatus.SHIPPED);
        order.setShippedAt(Instant.now());
        order.setTrackingNumber(
                normalizeTrackingNumber(trackingNumber)
        );

        eventPublisher.publishEvent(
                new OrderShippedEvent(
                        order.getId(),
                        order.getCustomerFullName(),
                        order.getCustomerEmail(),
                        order.getTrackingNumber(),
                        order.getShippedAt()
                )
        );

        return toAdminDetails(order);
    }

    private String normalizeTrackingNumber(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            return null;
        }

        return trackingNumber.trim();
    }

    @Transactional
    public OrderResponse create(OrderRequest req) {
        validate(req);

        CustomerOrder order = new CustomerOrder();
        order.setCustomerFullName(req.getCustomer().fullName);
        order.setCustomerEmail(req.getCustomer().email);
        order.setCustomerPhone(req.getCustomer().phone);

        order.setShippingCity(req.getShipping().city);
        order.setShippingAddress(req.getShipping().address);
        order.setShippingPostalCode(req.getShipping().postalCode);

        order.setComment(req.getComment());
        order.setCreatedAt(Instant.now());
        order.setStatus(OrderStatus.NEW);

        BigDecimal total = BigDecimal.ZERO;

        Map<Integer, Integer> requestedAmounts = new LinkedHashMap<>();

        for (OrderItemRequest item : req.getItems()) {
            if (item.getAmount() <= 0) {
                throw new IllegalArgumentException(
                        "Invalid amount for productId=" + item.getProductId()
                );
            }

            requestedAmounts.merge(
                    item.getProductId(),
                    item.getAmount(),
                    Math::addExact
            );
        }

        for (Map.Entry<Integer, Integer> entry : requestedAmounts.entrySet()) {
            int productId = entry.getKey();
            int requestedAmount = entry.getValue();

            Product product = products.findById(productId);

            if (product == null) {
                throw new IllegalArgumentException(
                        "Unknown productId=" + productId
                );
            }

            int stockQuantity = product.getStockQuantity() == null
                    ? 0
                    : product.getStockQuantity();

            if (stockQuantity < requestedAmount) {
                throw new IllegalArgumentException(
                        "Only " + stockQuantity
                                + " copies of \"" + product.getArtist()
                                + " — " + product.getName()
                                + "\" are available"
                );
            }

            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(requestedAmount));

            CustomerOrderItem item = new CustomerOrderItem();
            item.setProduct(product);
            item.setAmount(requestedAmount);
            item.setUnitPrice(product.getPrice());
            item.setLineTotal(lineTotal);

            order.addItem(item);
            total = total.add(lineTotal);

            product.setStockQuantity(stockQuantity - requestedAmount);
        }

        order.setItemsTotal(total);

        CustomerOrder savedOrder = orderRepository.save(order);

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getItemsTotal(),
                savedOrder.getCreatedAt()
        );
    }

    private void validate(OrderRequest req) {
        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain items");
        }

        if (req.getCustomer() == null
                || blank(req.getCustomer().fullName)
                || blank(req.getCustomer().email)) {
            throw new IllegalArgumentException(
                    "Customer fullName and email are required"
            );
        }

        if (req.getShipping() == null
                || blank(req.getShipping().city)
                || blank(req.getShipping().address)) {
            throw new IllegalArgumentException(
                    "Shipping city and address are required"
            );
        }
    }

    private AdminOrderDetailsResponse toAdminDetails(CustomerOrder order) {

        var items = order.getItems()
                .stream()
                .map(item -> new AdminOrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getArtist(),
                        item.getProduct().getName(),
                        item.getAmount(),
                        item.getUnitPrice(),
                        item.getLineTotal()
                ))
                .toList();

        int itemsCount = order.getItems()
                .stream()
                .mapToInt(CustomerOrderItem::getAmount)
                .sum();

        return new AdminOrderDetailsResponse(
                order.getId(),
                order.getCreatedAt(),

                order.getCustomerFullName(),
                order.getCustomerEmail(),
                order.getCustomerPhone(),

                order.getShippingCity(),
                order.getShippingAddress(),
                order.getShippingPostalCode(),

                order.getComment(),

                items,
                itemsCount,
                order.getItemsTotal(),

                order.getStatus(),
                order.getShippedAt(),
                order.getTrackingNumber()
        );
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}