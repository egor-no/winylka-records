package winylka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import winylka.infra.ProductRepository;
import winylka.infra.StockSubscriptionRepository;
import winylka.model.Product;
import winylka.model.StockSubscription;
import winylka.model.StockSubscriptionType;
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
    void shouldDeactivateOutOfStockSubscriptionWhenStockBecomesAvailable() {
        int productId = 52;

        StockSubscription subscription = createSubscription(
                productId,
                StockSubscriptionType.OUT_OF_STOCK,
                null
        );

        when(subscriptionRepository.findAllByProductIdAndActiveTrue(productId))
                .thenReturn(List.of(subscription));

        subscriptionService.notifySubscribers(
                productId,
                0,
                5
        );

        assertFalse(subscription.isActive());
        assertNotNull(subscription.getNotifiedAt());

        List<StockSubscription> savedSubscriptions =
                captureSavedSubscriptions();

        assertEquals(1, savedSubscriptions.size());
        assertSame(subscription, savedSubscriptions.get(0));
    }

    @Test
    void shouldNotNotifyOutOfStockSubscriptionForOrdinaryIncrease() {
        int productId = 52;

        StockSubscription subscription = createSubscription(
                productId,
                StockSubscriptionType.OUT_OF_STOCK,
                null
        );

        when(subscriptionRepository.findAllByProductIdAndActiveTrue(productId))
                .thenReturn(List.of(subscription));

        subscriptionService.notifySubscribers(
                productId,
                2,
                5
        );

        assertTrue(subscription.isActive());
        assertNull(subscription.getNotifiedAt());

        verify(subscriptionRepository, never())
                .saveAll(anyList());
    }

    @Test
    void shouldDeactivateStockIncreaseSubscriptionWhenThresholdIsExceeded() {
        int productId = 52;

        StockSubscription subscription = createSubscription(
                productId,
                StockSubscriptionType.STOCK_INCREASE,
                3
        );

        when(subscriptionRepository.findAllByProductIdAndActiveTrue(productId))
                .thenReturn(List.of(subscription));

        subscriptionService.notifySubscribers(
                productId,
                3,
                5
        );

        assertFalse(subscription.isActive());
        assertNotNull(subscription.getNotifiedAt());

        List<StockSubscription> savedSubscriptions =
                captureSavedSubscriptions();

        assertEquals(1, savedSubscriptions.size());
        assertSame(subscription, savedSubscriptions.get(0));
    }

    @Test
    void shouldNotNotifyStockIncreaseSubscriptionWhenThresholdIsNotExceeded() {
        int productId = 52;

        StockSubscription subscription = createSubscription(
                productId,
                StockSubscriptionType.STOCK_INCREASE,
                5
        );

        when(subscriptionRepository.findAllByProductIdAndActiveTrue(productId))
                .thenReturn(List.of(subscription));

        subscriptionService.notifySubscribers(
                productId,
                3,
                5
        );

        assertTrue(subscription.isActive());
        assertNull(subscription.getNotifiedAt());

        verify(subscriptionRepository, never())
                .saveAll(anyList());
    }

    @Test
    void shouldNotNotifySubscriptionAgainAfterItWasDeactivated() {
        int productId = 52;

        StockSubscription subscription = createSubscription(
                productId,
                StockSubscriptionType.OUT_OF_STOCK,
                null
        );

        when(subscriptionRepository.findAllByProductIdAndActiveTrue(productId))
                .thenReturn(
                        List.of(subscription),
                        Collections.emptyList()
                );

        subscriptionService.notifySubscribers(
                productId,
                0,
                5
        );

        assertFalse(subscription.isActive());
        assertNotNull(subscription.getNotifiedAt());

        subscriptionService.notifySubscribers(
                productId,
                0,
                10
        );

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

        subscriptionService.notifySubscribers(
                productId,
                0,
                5
        );

        verify(subscriptionRepository)
                .findAllByProductIdAndActiveTrue(productId);

        verify(subscriptionRepository, never())
                .saveAll(anyList());
    }

    @Test
    void shouldDoNothingWhenStockDoesNotIncrease() {
        int productId = 52;

        subscriptionService.notifySubscribers(
                productId,
                5,
                5
        );

        verify(subscriptionRepository, never())
                .findAllByProductIdAndActiveTrue(anyInt());

        verify(subscriptionRepository, never())
                .saveAll(anyList());
    }

    private StockSubscription createSubscription(
            int productId,
            StockSubscriptionType type,
            Integer stockQuantityAtSubscription
    ) {
        Product product = new Product();
        product.setId(productId);

        StockSubscription subscription = new StockSubscription();
        subscription.setProduct(product);
        subscription.setEmail("test@mail.com");
        subscription.setType(type);
        subscription.setStockQuantityAtSubscription(
                stockQuantityAtSubscription
        );
        subscription.setActive(true);
        subscription.setNotifiedAt(null);

        return subscription;
    }

    @SuppressWarnings("unchecked")
    private List<StockSubscription> captureSavedSubscriptions() {
        ArgumentCaptor<List<StockSubscription>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(subscriptionRepository).saveAll(captor.capture());

        return captor.getValue();
    }
}