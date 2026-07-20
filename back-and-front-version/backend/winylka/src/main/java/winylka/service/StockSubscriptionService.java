package winylka.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import winylka.dto.StockSubscriptionResponse;
import winylka.infra.ProductRepository;
import winylka.infra.StockSubscriptionRepository;
import winylka.model.Product;
import winylka.model.StockSubscription;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class StockSubscriptionService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private final StockSubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;

    public StockSubscriptionService(StockSubscriptionRepository subscriptionRepository, ProductRepository productRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public StockSubscriptionResponse subscribe(int productId, String rawEmail) {
        String email = normalizeEmail(rawEmail);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product with id " + productId + " was not found"
                        )
                );

        StockSubscription existing = subscriptionRepository
                .findByProductIdAndEmailIgnoreCase(productId, email)
                .orElse(null);

        if (existing != null && existing.isActive()) {
            return new StockSubscriptionResponse(
                    "You are already subscribed to this product.",
                    true
            );
        }

        if (existing != null) {
            existing.setActive(true);
            existing.setCreatedAt(LocalDateTime.now());
            existing.setNotifiedAt(null);

            subscriptionRepository.save(existing);

            return new StockSubscriptionResponse(
                    "You will be notified when this item is back in stock.",
                    false
            );
        }

        StockSubscription subscription = new StockSubscription();
        subscription.setProduct(product);
        subscription.setEmail(email);
        subscription.setUserId(null);
        subscription.setActive(true);

        subscriptionRepository.save(subscription);

        return new StockSubscriptionResponse(
                "You will be notified when this item is back in stock.",
                false
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifySubscribers(int productId) {
        List<StockSubscription> subscriptions =
                subscriptionRepository.findAllByProductIdAndActiveTrue(
                        productId
                );

        if (subscriptions.isEmpty()) {
            return;
        }

        for (StockSubscription subscription : subscriptions) {
            /*
             * Здесь позже будет EmailService или отправка события в Kafka.
             */
            System.out.println(
                    "RESTOCK NOTIFICATION: productId="
                            + productId
                            + ", email="
                            + subscription.getEmail()
            );

            subscription.setActive(false);
            subscription.setNotifiedAt(LocalDateTime.now());
        }

        subscriptionRepository.saveAll(subscriptions);
    }

    private String normalizeEmail(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Email address is required"
            );
        }

        String email = rawEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        if (email.length() > 255 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(
                    "Please enter a valid email address"
            );
        }

        return email;
    }
}