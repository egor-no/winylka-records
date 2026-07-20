package winylka.service;

import org.springframework.transaction.annotation.Transactional;
import winylka.dto.OrderItemRequest;
import winylka.dto.OrderRequest;
import winylka.dto.OrderResponse;
import winylka.infra.OrderRepository;
import winylka.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OrderService {

    private final ProductService products;
    private final OrderRepository orderRepository;

    public OrderService(ProductService products, OrderRepository orderRepository) {
        this.products = products;
        this.orderRepository = orderRepository;
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

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}