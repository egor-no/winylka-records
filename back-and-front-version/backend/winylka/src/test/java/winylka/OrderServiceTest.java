package winylka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import winylka.dto.OrderItemRequest;
import winylka.dto.OrderRequest;
import winylka.dto.OrderResponse;
import winylka.infra.OrderRepository;
import winylka.model.*;
import winylka.service.OrderService;
import winylka.service.ProductService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private ProductService productService;
    private OrderRepository orderRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        orderRepository = mock(OrderRepository.class);
        orderService = new OrderService(productService, orderRepository);
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
        Product product = new Product();
        product.setId(1);
        product.setPrice(new BigDecimal("25.00"));
        product.setStockQuantity(1);

        when(productService.findById(1)).thenReturn(product);

        OrderRequest request = createRequest(1, 2);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.create(request)
        );

        assertEquals("Not enough stock for productId=1", exception.getMessage());
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
}