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

import java.math.BigDecimal;
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
    void shouldPersistNotificationStatusAfterRestockEvent() {
        AtomicReference<Long> subscriptionId = new AtomicReference<>();

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(status -> {
            Product product = createProduct();
            product = productRepository.save(product);

            StockSubscription subscription = new StockSubscription();
            subscription.setProduct(product);
            subscription.setEmail("test@mail.com");
            subscription.setActive(true);
            subscription.setNotifiedAt(null);

            subscription = subscriptionRepository.save(subscription);
            subscriptionId.set(subscription.getId());

            eventPublisher.publishEvent(
                    new ProductRestockedEvent(product.getId())
            );
        });

        StockSubscription updatedSubscription =
                subscriptionRepository.findById(subscriptionId.get())
                        .orElseThrow();

        assertFalse(
                updatedSubscription.isActive(),
                "Subscription should become inactive after notification"
        );

        assertNotNull(
                updatedSubscription.getNotifiedAt(),
                "notifiedAt should be filled after notification"
        );
    }

    @Test
    void shouldNotProcessSubscriptionAgainAfterItWasNotified() {
        AtomicReference<Long> subscriptionId = new AtomicReference<>();
        AtomicReference<Integer> productId = new AtomicReference<>();

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(status -> {
            Product product = productRepository.save(createProduct());
            productId.set(product.getId());

            StockSubscription subscription = new StockSubscription();
            subscription.setProduct(product);
            subscription.setEmail("repeat-test@mail.com");
            subscription.setActive(true);

            subscription = subscriptionRepository.save(subscription);
            subscriptionId.set(subscription.getId());

            eventPublisher.publishEvent(
                    new ProductRestockedEvent(product.getId())
            );
        });

        StockSubscription afterFirstEvent =
                subscriptionRepository.findById(subscriptionId.get())
                        .orElseThrow();

        assertFalse(afterFirstEvent.isActive());
        assertNotNull(afterFirstEvent.getNotifiedAt());

        var firstNotifiedAt = afterFirstEvent.getNotifiedAt();

        transactionTemplate.executeWithoutResult(status ->
                eventPublisher.publishEvent(
                        new ProductRestockedEvent(productId.get())
                )
        );

        StockSubscription afterSecondEvent =
                subscriptionRepository.findById(subscriptionId.get())
                        .orElseThrow();

        assertFalse(afterSecondEvent.isActive());

        assertEquals(
                firstNotifiedAt,
                afterSecondEvent.getNotifiedAt(),
                "Second event must not process an inactive subscription again"
        );
    }

    private Product createProduct() {
        Product product = new Product();
        product.setArtist("Madonna");
        product.setName("Confessions on a Dance Floor");
        product.setPrice(new BigDecimal("25.00"));
        product.setStockQuantity(0);

        return product;
    }
}