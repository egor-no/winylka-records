package winylka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import winylka.event.ProductRestockedEvent;
import winylka.infra.ProductRepository;
import winylka.infra.StockSubscriptionRepository;
import winylka.model.Product;
import winylka.model.StockSubscription;
import winylka.model.StockSubscriptionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ProductRestockedEventIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockSubscriptionRepository subscriptionRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldNotifyOutOfStockSubscriptionWhenStockChangesFromZeroToPositive() {
        AtomicReference<Long> subscriptionId = new AtomicReference<>();

        executeInTransaction(() -> {
            Product product = productRepository.save(createProduct(0));

            StockSubscription subscription = createSubscription(
                    product,
                    "out-of-stock@mail.com",
                    StockSubscriptionType.OUT_OF_STOCK,
                    null
            );

            subscription = subscriptionRepository.save(subscription);
            subscriptionId.set(subscription.getId());

            eventPublisher.publishEvent(
                    new ProductRestockedEvent(
                            product.getId(),
                            0,
                            5
                    )
            );
        });

        StockSubscription updatedSubscription =
                findSubscription(subscriptionId.get());

        assertFalse(
                updatedSubscription.isActive(),
                "OUT_OF_STOCK subscription should become inactive"
        );

        assertNotNull(
                updatedSubscription.getNotifiedAt(),
                "notifiedAt should be filled after notification"
        );
    }

    @Test
    void shouldNotifyStockIncreaseSubscriptionWhenStockExceedsSubscribedQuantity() {
        AtomicReference<Long> subscriptionId = new AtomicReference<>();

        executeInTransaction(() -> {
            Product product = productRepository.save(createProduct(3));

            StockSubscription subscription = createSubscription(
                    product,
                    "stock-increase@mail.com",
                    StockSubscriptionType.STOCK_INCREASE,
                    3
            );

            subscription = subscriptionRepository.save(subscription);
            subscriptionId.set(subscription.getId());

            eventPublisher.publishEvent(
                    new ProductRestockedEvent(
                            product.getId(),
                            3,
                            5
                    )
            );
        });

        StockSubscription updatedSubscription =
                findSubscription(subscriptionId.get());

        assertFalse(updatedSubscription.isActive());
        assertNotNull(updatedSubscription.getNotifiedAt());
    }

    @Test
    void shouldNotNotifyStockIncreaseSubscriptionWhenNewStockDoesNotExceedSubscribedQuantity() {
        AtomicReference<Long> subscriptionId = new AtomicReference<>();

        executeInTransaction(() -> {
            Product product = productRepository.save(createProduct(3));

            StockSubscription subscription = createSubscription(
                    product,
                    "no-increase@mail.com",
                    StockSubscriptionType.STOCK_INCREASE,
                    5
            );

            subscription = subscriptionRepository.save(subscription);
            subscriptionId.set(subscription.getId());

            eventPublisher.publishEvent(
                    new ProductRestockedEvent(
                            product.getId(),
                            3,
                            5
                    )
            );
        });

        StockSubscription updatedSubscription =
                findSubscription(subscriptionId.get());

        assertTrue(
                updatedSubscription.isActive(),
                "Subscription should remain active when stock has not exceeded the subscribed quantity"
        );

        assertNull(updatedSubscription.getNotifiedAt());
    }

    @Test
    void shouldNotNotifyOutOfStockSubscriptionForOrdinaryStockIncrease() {
        AtomicReference<Long> subscriptionId = new AtomicReference<>();

        executeInTransaction(() -> {
            Product product = productRepository.save(createProduct(3));

            StockSubscription subscription = createSubscription(
                    product,
                    "wrong-type@mail.com",
                    StockSubscriptionType.OUT_OF_STOCK,
                    null
            );

            subscription = subscriptionRepository.save(subscription);
            subscriptionId.set(subscription.getId());

            eventPublisher.publishEvent(
                    new ProductRestockedEvent(
                            product.getId(),
                            3,
                            5
                    )
            );
        });

        StockSubscription updatedSubscription =
                findSubscription(subscriptionId.get());

        assertTrue(updatedSubscription.isActive());
        assertNull(updatedSubscription.getNotifiedAt());
    }

    @Test
    void shouldNotProcessSubscriptionAgainAfterItWasNotified() {
        AtomicReference<Long> subscriptionId = new AtomicReference<>();
        AtomicReference<Integer> productId = new AtomicReference<>();

        executeInTransaction(() -> {
            Product product = productRepository.save(createProduct(0));
            productId.set(product.getId());

            StockSubscription subscription = createSubscription(
                    product,
                    "repeat-test@mail.com",
                    StockSubscriptionType.OUT_OF_STOCK,
                    null
            );

            subscription = subscriptionRepository.save(subscription);
            subscriptionId.set(subscription.getId());

            eventPublisher.publishEvent(
                    new ProductRestockedEvent(
                            product.getId(),
                            0,
                            5
                    )
            );
        });

        StockSubscription afterFirstEvent =
                findSubscription(subscriptionId.get());

        assertFalse(afterFirstEvent.isActive());
        assertNotNull(afterFirstEvent.getNotifiedAt());

        LocalDateTime firstNotifiedAt =
                afterFirstEvent.getNotifiedAt();

        executeInTransaction(() ->
                eventPublisher.publishEvent(
                        new ProductRestockedEvent(
                                productId.get(),
                                0,
                                10
                        )
                )
        );

        StockSubscription afterSecondEvent =
                findSubscription(subscriptionId.get());

        assertFalse(afterSecondEvent.isActive());

        assertEquals(
                firstNotifiedAt,
                afterSecondEvent.getNotifiedAt(),
                "Second event must not process an inactive subscription again"
        );
    }

    private void executeInTransaction(Runnable action) {
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(status ->
                action.run()
        );
    }

    private StockSubscription findSubscription(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow();
    }

    private StockSubscription createSubscription(
            Product product,
            String email,
            StockSubscriptionType type,
            Integer stockQuantityAtSubscription
    ) {
        StockSubscription subscription = new StockSubscription();
        subscription.setProduct(product);
        subscription.setEmail(email);
        subscription.setType(type);
        subscription.setStockQuantityAtSubscription(
                stockQuantityAtSubscription
        );
        subscription.setActive(true);
        subscription.setNotifiedAt(null);

        return subscription;
    }

    private Product createProduct(int stockQuantity) {
        Product product = new Product();
        product.setArtist("Madonna");
        product.setName("Confessions on a Dance Floor");
        product.setPrice(new BigDecimal("25.00"));
        product.setStockQuantity(stockQuantity);

        return product;
    }
}