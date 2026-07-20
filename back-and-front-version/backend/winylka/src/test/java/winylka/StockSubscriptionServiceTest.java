package winylka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import winylka.infra.ProductRepository;
import winylka.infra.StockSubscriptionRepository;
import winylka.model.Product;
import winylka.model.StockSubscription;
import winylka.service.StockSubscriptionService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockSubscriptionServiceTest {

    private StockSubscriptionRepository subscriptionRepository;
    private ProductRepository productRepository;
    private StockSubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionRepository = mock(StockSubscriptionRepository.class);
        productRepository = mock(ProductRepository.class);

        subscriptionService = new StockSubscriptionService(
                subscriptionRepository,
                productRepository
        );
    }

    @Test
    void shouldDeactivateSubscriptionAndSetNotifiedAtAfterNotification() {
        int productId = 52;

        Product product = new Product();
        product.setId(productId);

        StockSubscription subscription = new StockSubscription();
        subscription.setProduct(product);
        subscription.setEmail("test@mail.com");
        subscription.setActive(true);
        subscription.setNotifiedAt(null);

        when(subscriptionRepository.findAllByProductIdAndActiveTrue(productId))
                .thenReturn(List.of(subscription));

        subscriptionService.notifySubscribers(productId);

        assertFalse(subscription.isActive());
        assertNotNull(subscription.getNotifiedAt());

        ArgumentCaptor<List<StockSubscription>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(subscriptionRepository).saveAll(captor.capture());

        List<StockSubscription> savedSubscriptions = captor.getValue();

        assertEquals(1, savedSubscriptions.size());
        assertSame(subscription, savedSubscriptions.get(0));
        assertFalse(savedSubscriptions.get(0).isActive());
        assertNotNull(savedSubscriptions.get(0).getNotifiedAt());
    }

    @Test
    void shouldNotNotifySubscriptionAgainAfterItWasDeactivated() {
        int productId = 52;

        Product product = new Product();
        product.setId(productId);

        StockSubscription subscription = new StockSubscription();
        subscription.setProduct(product);
        subscription.setEmail("test@mail.com");
        subscription.setActive(true);

        when(subscriptionRepository.findAllByProductIdAndActiveTrue(productId))
                .thenReturn(
                        List.of(subscription),
                        Collections.emptyList()
                );

        subscriptionService.notifySubscribers(productId);

        assertFalse(subscription.isActive());
        assertNotNull(subscription.getNotifiedAt());

        subscriptionService.notifySubscribers(productId);

        verify(
                subscriptionRepository,
                times(2)
        ).findAllByProductIdAndActiveTrue(productId);

        verify(
                subscriptionRepository,
                times(1)
        ).saveAll(anyList());
    }

    @Test
    void shouldDoNothingWhenThereAreNoActiveSubscriptions() {
        int productId = 52;

        when(subscriptionRepository.findAllByProductIdAndActiveTrue(productId))
                .thenReturn(Collections.emptyList());

        subscriptionService.notifySubscribers(productId);

        verify(subscriptionRepository)
                .findAllByProductIdAndActiveTrue(productId);

        verify(subscriptionRepository, never())
                .saveAll(anyList());
    }
}