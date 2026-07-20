package winylka.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import winylka.dto.StockSubscriptionResponse;
import winylka.infra.ProductRepository;
import winylka.infra.StockSubscriptionRepository;
import winylka.model.Product;
import winylka.model.StockSubscription;
import winylka.model.StockSubscriptionType;

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

    public StockSubscriptionService(
            StockSubscriptionRepository subscriptionRepository,
            ProductRepository productRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public StockSubscriptionResponse subscribe(
            int productId,
            String rawEmail,
            StockSubscriptionType type
    ) {
        String email = normalizeEmail(rawEmail);

        if (type == null) {
            throw new IllegalArgumentException(
                    "Subscription type is required"
            );
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product with id " + productId + " was not found"
                        )
                );

        validateSubscriptionType(product, type);

        StockSubscription existing = subscriptionRepository
                .findByProductIdAndEmailIgnoreCaseAndType(
                        productId,
                        email,
                        type
                )
                .orElse(null);

        if (existing != null && existing.isActive()) {
            return new StockSubscriptionResponse(
                    "You are already subscribed to this product.",
                    true
            );
        }

        if (existing != null) {
            reactivateSubscription(existing, product, type);

            subscriptionRepository.save(existing);

            return new StockSubscriptionResponse(
                    getSuccessMessage(type),
                    false
            );
        }

        StockSubscription subscription = new StockSubscription();
        subscription.setProduct(product);
        subscription.setEmail(email);
        subscription.setUserId(null);
        subscription.setType(type);
        subscription.setActive(true);
        subscription.setNotifiedAt(null);
        subscription.setStockQuantityAtSubscription(
                resolveStockQuantityAtSubscription(product, type)
        );

        subscriptionRepository.save(subscription);

        return new StockSubscriptionResponse(
                getSuccessMessage(type),
                false
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifySubscribers(
            int productId,
            int oldStockQuantity,
            int newStockQuantity
    ) {
        if (newStockQuantity <= oldStockQuantity) {
            return;
        }

        List<StockSubscription> subscriptions =
                subscriptionRepository.findAllByProductIdAndActiveTrue(productId);

        if (subscriptions.isEmpty()) {
            return;
        }

        LocalDateTime notifiedAt = LocalDateTime.now();

        List<StockSubscription> notifiedSubscriptions =
                subscriptions.stream()
                        .filter(subscription ->
                                shouldNotify(
                                        subscription,
                                        oldStockQuantity,
                                        newStockQuantity
                                )
                        )
                        .peek(subscription -> {
                            System.out.println(
                                    "RESTOCK NOTIFICATION: productId="
                                            + productId
                                            + ", email="
                                            + subscription.getEmail()
                                            + ", type="
                                            + subscription.getType()
                            );

                            subscription.setActive(false);
                            subscription.setNotifiedAt(notifiedAt);
                        })
                        .toList();

        if (!notifiedSubscriptions.isEmpty()) {
            subscriptionRepository.saveAll(notifiedSubscriptions);
        }
    }

    private boolean shouldNotify(
            StockSubscription subscription,
            int oldStockQuantity,
            int newStockQuantity
    ) {
        if (subscription.getType() == StockSubscriptionType.OUT_OF_STOCK) {
            return oldStockQuantity == 0 && newStockQuantity > 0;
        }

        if (subscription.getType() == StockSubscriptionType.STOCK_INCREASE) {
            Integer subscribedAt =
                    subscription.getStockQuantityAtSubscription();

            return subscribedAt != null
                    && newStockQuantity > subscribedAt;
        }

        return false;
    }

    private void validateSubscriptionType(
            Product product,
            StockSubscriptionType type
    ) {
        if (type == StockSubscriptionType.OUT_OF_STOCK
                && product.getStockQuantity() > 0) {
            throw new IllegalArgumentException(
                    "This product is currently in stock"
            );
        }
    }

    private void reactivateSubscription(
            StockSubscription subscription,
            Product product,
            StockSubscriptionType type
    ) {
        subscription.setActive(true);
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setNotifiedAt(null);
        subscription.setStockQuantityAtSubscription(
                resolveStockQuantityAtSubscription(product, type)
        );
    }

    private Integer resolveStockQuantityAtSubscription(
            Product product,
            StockSubscriptionType type
    ) {
        if (type == StockSubscriptionType.STOCK_INCREASE) {
            return product.getStockQuantity();
        }

        return null;
    }

    private String getSuccessMessage(StockSubscriptionType type) {
        if (type == StockSubscriptionType.STOCK_INCREASE) {
            return "You will be notified when more copies are added.";
        }

        return "You will be notified when this item is back in stock.";
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

        if (email.length() > 255
                || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(
                    "Please enter a valid email address"
            );
        }

        return email;
    }
}