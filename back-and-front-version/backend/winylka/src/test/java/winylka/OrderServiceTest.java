package winylka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import winylka.dto.*;
import winylka.event.OrderShippedEvent;
import winylka.infra.OrderRepository;
import winylka.model.*;
import winylka.service.OrderService;
import winylka.service.ProductService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private ProductService productService;
    private OrderRepository orderRepository;
    private ApplicationEventPublisher eventPublisher;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        orderRepository = mock(OrderRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        orderService = new OrderService(
                productService,
                orderRepository,
                eventPublisher
        );
    }

    @Test
    void shouldCreateOrderAndDecreaseStock() {
        Product product = new Product();
        product.setId(1);
        product.setArtist("Madonna");
        product.setName("Confessions on a Dance Floor");
        product.setPrice(new BigDecimal("25.00"));
        product.setStockQuantity(5);

        when(productService.findById(1)).thenReturn(product);

        when(orderRepository.save(any(CustomerOrder.class)))
                .thenAnswer(invocation -> {
                    CustomerOrder order = invocation.getArgument(0);
                    order.setId(1001L);
                    return order;
                });

        OrderRequest request = createRequest(1, 2);

        OrderResponse response = orderService.create(request);

        assertEquals(1001L, response.getOrderId());
        assertEquals(new BigDecimal("50.00"), response.getItemsTotal());
        assertEquals(3, product.getStockQuantity());

        ArgumentCaptor<CustomerOrder> captor =
                ArgumentCaptor.forClass(CustomerOrder.class);

        verify(orderRepository).save(captor.capture());

        CustomerOrder savedOrder = captor.getValue();

        assertEquals(new BigDecimal("50.00"), savedOrder.getItemsTotal());
        assertEquals(1, savedOrder.getItems().size());

        CustomerOrderItem item = savedOrder.getItems().get(0);

        assertEquals(2, item.getAmount());
        assertEquals(new BigDecimal("25.00"), item.getUnitPrice());
        assertEquals(new BigDecimal("50.00"), item.getLineTotal());
        assertSame(savedOrder, item.getOrder());
        assertSame(product, item.getProduct());
    }

    @Test
    void shouldRejectOrderWhenStockIsNotEnough() {
        Product product = createProduct(
                1,
                "Madonna",
                "Confessions on a Dance Floor",
                "25.00",
                1
        );

        when(productService.findById(1)).thenReturn(product);

        OrderRequest request = createRequest(1, 2);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.create(request)
        );

        assertEquals(
                "Only 1 copies of \"Madonna — Confessions on a Dance Floor\" are available",
                exception.getMessage()
        );

        assertEquals(1, product.getStockQuantity());

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectOrderWithUnknownProduct() {
        when(productService.findById(999)).thenReturn(null);

        OrderRequest request = createRequest(999, 1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.create(request)
        );

        assertEquals("Unknown productId=999", exception.getMessage());

        verify(orderRepository, never()).save(any());
    }

    private OrderRequest createRequest(int productId, int amount) {
        OrderRequest.Customer customer = new OrderRequest.Customer();
        customer.fullName = "Egor Test";
        customer.email = "egor@example.com";
        customer.phone = "+70000000000";

        OrderRequest.Shipping shipping = new OrderRequest.Shipping();
        shipping.city = "Astana";
        shipping.address = "Test street 1";
        shipping.postalCode = "010000";

        return new OrderRequest(
                customer,
                shipping,
                "Test order",
                List.of(new OrderItemRequest(productId, amount))
        );
    }

    @Test
    void shouldCreateOrderWithNewStatus() {
        Product product = createProduct(
                1,
                "Madonna",
                "Confessions on a Dance Floor",
                "25.00",
                5
        );

        when(productService.findById(1)).thenReturn(product);

        when(orderRepository.save(any(CustomerOrder.class)))
                .thenAnswer(invocation -> {
                    CustomerOrder order = invocation.getArgument(0);
                    order.setId(1001L);
                    return order;
                });

        OrderResponse response = orderService.create(createRequest(1, 2));

        assertEquals(1001L, response.getOrderId());

        ArgumentCaptor<CustomerOrder> captor =
                ArgumentCaptor.forClass(CustomerOrder.class);

        verify(orderRepository).save(captor.capture());

        CustomerOrder savedOrder = captor.getValue();

        assertEquals(OrderStatus.NEW, savedOrder.getStatus());
        assertNotNull(savedOrder.getCreatedAt());
        assertNull(savedOrder.getShippedAt());
        assertNull(savedOrder.getTrackingNumber());
    }

    @Test
    void shouldReturnOrdersForAdmin() {
        CustomerOrder firstOrder = createOrder(
                1001L,
                "Egor Test",
                "egor@example.com",
                OrderStatus.NEW,
                Instant.parse("2026-07-20T10:00:00Z")
        );

        firstOrder.setItemsTotal(new BigDecimal("75.00"));
        firstOrder.addItem(createOrderItem(
                createProduct(
                        1,
                        "Madonna",
                        "Confessions on a Dance Floor",
                        "25.00",
                        5
                ),
                2,
                "25.00"
        ));
        firstOrder.addItem(createOrderItem(
                createProduct(
                        2,
                        "Nick Drake",
                        "Pink Moon",
                        "25.00",
                        3
                ),
                1,
                "25.00"
        ));

        CustomerOrder secondOrder = createOrder(
                1002L,
                "John Customer",
                "john@example.com",
                OrderStatus.SHIPPED,
                Instant.parse("2026-07-19T10:00:00Z")
        );

        secondOrder.setItemsTotal(new BigDecimal("40.00"));
        secondOrder.addItem(createOrderItem(
                createProduct(
                        3,
                        "Cat Stevens",
                        "Tea for the Tillerman",
                        "20.00",
                        4
                ),
                2,
                "20.00"
        ));

        when(orderRepository.findAllForAdmin())
                .thenReturn(List.of(firstOrder, secondOrder));

        List<AdminOrderListItemResponse> result =
                orderService.findAllForAdmin();

        assertEquals(2, result.size());

        AdminOrderListItemResponse first = result.get(0);

        assertEquals(1001L, first.getId());
        assertEquals("Egor Test", first.getCustomerFullName());
        assertEquals("egor@example.com", first.getCustomerEmail());
        assertEquals(3, first.getItemsCount());
        assertEquals(new BigDecimal("75.00"), first.getItemsTotal());
        assertEquals(OrderStatus.NEW, first.getStatus());

        AdminOrderListItemResponse second = result.get(1);

        assertEquals(1002L, second.getId());
        assertEquals(2, second.getItemsCount());
        assertEquals(OrderStatus.SHIPPED, second.getStatus());

        verify(orderRepository).findAllForAdmin();
    }

    @Test
    void shouldReturnOrderDetailsForAdmin() {
        Product product = createProduct(
                1,
                "Madonna",
                "Confessions on a Dance Floor",
                "25.00",
                5
        );

        CustomerOrder order = createOrder(
                1001L,
                "Egor Test",
                "egor@example.com",
                OrderStatus.NEW,
                Instant.parse("2026-07-20T10:00:00Z")
        );

        order.setCustomerPhone("+70000000000");
        order.setShippingCity("Astana");
        order.setShippingAddress("Test street 1");
        order.setShippingPostalCode("010000");
        order.setComment("Please pack carefully");
        order.setItemsTotal(new BigDecimal("50.00"));

        order.addItem(createOrderItem(product, 2, "25.00"));

        when(orderRepository.findDetailedById(1001L))
                .thenReturn(Optional.of(order));

        AdminOrderDetailsResponse result =
                orderService.findAdminOrderById(1001L);

        assertEquals(1001L, result.getId());
        assertEquals("Egor Test", result.getCustomerFullName());
        assertEquals("egor@example.com", result.getCustomerEmail());
        assertEquals("+70000000000", result.getCustomerPhone());

        assertEquals("Astana", result.getShippingCity());
        assertEquals("Test street 1", result.getShippingAddress());
        assertEquals("010000", result.getShippingPostalCode());

        assertEquals("Please pack carefully", result.getComment());
        assertEquals(2, result.getItemsCount());
        assertEquals(new BigDecimal("50.00"), result.getItemsTotal());
        assertEquals(OrderStatus.NEW, result.getStatus());

        assertEquals(1, result.getItems().size());

        assertEquals(1, result.getItems().get(0).getProductId());
        assertEquals("Madonna", result.getItems().get(0).getArtist());
        assertEquals(
                "Confessions on a Dance Floor",
                result.getItems().get(0).getAlbum()
        );
        assertEquals(2, result.getItems().get(0).getAmount());
        assertEquals(
                new BigDecimal("25.00"),
                result.getItems().get(0).getUnitPrice()
        );
        assertEquals(
                new BigDecimal("50.00"),
                result.getItems().get(0).getLineTotal()
        );

        verify(orderRepository).findDetailedById(1001L);
    }

    @Test
    void shouldRejectAdminOrderDetailsWhenOrderDoesNotExist() {
        when(orderRepository.findDetailedById(999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.findAdminOrderById(999L)
        );

        assertEquals("Order not found: 999", exception.getMessage());
    }

    @Test
    void shouldMarkOrderAsShippedWithTrackingNumber() {
        CustomerOrder order = createOrder(
                1001L,
                "Egor Test",
                "egor@example.com",
                OrderStatus.NEW,
                Instant.parse("2026-07-20T10:00:00Z")
        );

        order.setItemsTotal(new BigDecimal("25.00"));

        Product product = createProduct(
                1,
                "Madonna",
                "Confessions on a Dance Floor",
                "25.00",
                5
        );

        order.addItem(createOrderItem(product, 1, "25.00"));

        when(orderRepository.findDetailedById(1001L))
                .thenReturn(Optional.of(order));

        AdminOrderDetailsResponse result =
                orderService.ship(1001L, "  TRACK-12345  ");

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertNotNull(order.getShippedAt());
        assertEquals("TRACK-12345", order.getTrackingNumber());

        assertEquals(OrderStatus.SHIPPED, result.getStatus());
        assertNotNull(result.getShippedAt());
        assertEquals("TRACK-12345", result.getTrackingNumber());

        ArgumentCaptor<OrderShippedEvent> eventCaptor =
                ArgumentCaptor.forClass(OrderShippedEvent.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        OrderShippedEvent event = eventCaptor.getValue();

        assertEquals(1001L, event.getOrderId());
        assertEquals("Egor Test", event.getCustomerFullName());
        assertEquals("egor@example.com", event.getCustomerEmail());
        assertEquals("TRACK-12345", event.getTrackingNumber());
        assertEquals(order.getShippedAt(), event.getShippedAt());

        verify(orderRepository).findDetailedById(1001L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldMarkOrderAsShippedWithoutTrackingNumber() {
        CustomerOrder order = createOrder(
                1001L,
                "Egor Test",
                "egor@example.com",
                OrderStatus.NEW,
                Instant.now()
        );

        order.setItemsTotal(new BigDecimal("25.00"));

        Product product = createProduct(
                1,
                "Madonna",
                "Confessions on a Dance Floor",
                "25.00",
                5
        );

        order.addItem(createOrderItem(product, 1, "25.00"));

        when(orderRepository.findDetailedById(1001L))
                .thenReturn(Optional.of(order));

        AdminOrderDetailsResponse result =
                orderService.ship(1001L, "   ");

        assertEquals(OrderStatus.SHIPPED, result.getStatus());
        assertNotNull(result.getShippedAt());
        assertNull(result.getTrackingNumber());
        assertNull(order.getTrackingNumber());

        ArgumentCaptor<OrderShippedEvent> eventCaptor =
                ArgumentCaptor.forClass(OrderShippedEvent.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        OrderShippedEvent event = eventCaptor.getValue();

        assertEquals(1001L, event.getOrderId());
        assertNull(event.getTrackingNumber());
        assertEquals(order.getShippedAt(), event.getShippedAt());
    }

    @Test
    void shouldAcceptNullTrackingNumber() {
        CustomerOrder order = createOrder(
                1001L,
                "Egor Test",
                "egor@example.com",
                OrderStatus.NEW,
                Instant.now()
        );

        order.setItemsTotal(BigDecimal.ZERO);

        when(orderRepository.findDetailedById(1001L))
                .thenReturn(Optional.of(order));

        AdminOrderDetailsResponse result =
                orderService.ship(1001L, null);

        assertEquals(OrderStatus.SHIPPED, result.getStatus());
        assertNull(result.getTrackingNumber());
    }

    @Test
    void shouldRejectAlreadyShippedOrder() {
        CustomerOrder order = createOrder(
                1001L,
                "Egor Test",
                "egor@example.com",
                OrderStatus.SHIPPED,
                Instant.now()
        );

        Instant originalShippedAt =
                Instant.parse("2026-07-20T11:00:00Z");

        order.setShippedAt(originalShippedAt);
        order.setTrackingNumber("OLD-TRACK");
        order.setItemsTotal(BigDecimal.ZERO);

        when(orderRepository.findDetailedById(1001L))
                .thenReturn(Optional.of(order));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.ship(1001L, "NEW-TRACK")
        );

        assertEquals(
                "Order has already been shipped",
                exception.getMessage()
        );

        assertEquals(originalShippedAt, order.getShippedAt());
        assertEquals("OLD-TRACK", order.getTrackingNumber());
    }

    @Test
    void shouldRejectShippingWhenOrderDoesNotExist() {
        when(orderRepository.findDetailedById(999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.ship(999L, "TRACK-123")
        );

        assertEquals("Order not found: 999", exception.getMessage());

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldMergeDuplicateProductItems() {
        Product product = createProduct(
                1,
                "Madonna",
                "Confessions on a Dance Floor",
                "25.00",
                10
        );

        when(productService.findById(1)).thenReturn(product);

        when(orderRepository.save(any(CustomerOrder.class)))
                .thenAnswer(invocation -> {
                    CustomerOrder order = invocation.getArgument(0);
                    order.setId(1001L);
                    return order;
                });

        OrderRequest.Customer customer = new OrderRequest.Customer();
        customer.fullName = "Egor Test";
        customer.email = "egor@example.com";

        OrderRequest.Shipping shipping = new OrderRequest.Shipping();
        shipping.city = "Astana";
        shipping.address = "Test street 1";

        OrderRequest request = new OrderRequest(
                customer,
                shipping,
                null,
                List.of(
                        new OrderItemRequest(1, 2),
                        new OrderItemRequest(1, 3)
                )
        );

        OrderResponse response = orderService.create(request);

        assertEquals(new BigDecimal("125.00"), response.getItemsTotal());
        assertEquals(5, product.getStockQuantity());

        ArgumentCaptor<CustomerOrder> captor =
                ArgumentCaptor.forClass(CustomerOrder.class);

        verify(orderRepository).save(captor.capture());

        CustomerOrder savedOrder = captor.getValue();

        assertEquals(1, savedOrder.getItems().size());
        assertEquals(5, savedOrder.getItems().get(0).getAmount());
        assertEquals(
                new BigDecimal("125.00"),
                savedOrder.getItems().get(0).getLineTotal()
        );

        verify(productService, times(1)).findById(1);
    }

    private Product createProduct(
            int id,
            String artist,
            String name,
            String price,
            int stockQuantity
    ) {
        Product product = new Product();
        product.setId(id);
        product.setArtist(artist);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setStockQuantity(stockQuantity);
        return product;
    }

    private CustomerOrder createOrder(
            long id,
            String customerFullName,
            String customerEmail,
            OrderStatus status,
            Instant createdAt
    ) {
        CustomerOrder order = new CustomerOrder();
        order.setId(id);
        order.setCustomerFullName(customerFullName);
        order.setCustomerEmail(customerEmail);
        order.setStatus(status);
        order.setCreatedAt(createdAt);
        return order;
    }

    private CustomerOrderItem createOrderItem(
            Product product,
            int amount,
            String unitPrice
    ) {
        BigDecimal price = new BigDecimal(unitPrice);

        CustomerOrderItem item = new CustomerOrderItem();
        item.setProduct(product);
        item.setAmount(amount);
        item.setUnitPrice(price);
        item.setLineTotal(
                price.multiply(BigDecimal.valueOf(amount))
        );

        return item;
    }
}