package winylka.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import winylka.service.StockSubscriptionService;

@Component
public class ProductRestockedEventListener {

    private final StockSubscriptionService subscriptionService;

    public ProductRestockedEventListener(
            StockSubscriptionService subscriptionService
    ) {
        this.subscriptionService = subscriptionService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductRestockedEvent event) {
        subscriptionService.notifySubscribers(
                event.getProductId(),
                event.getOldStockQuantity(),
                event.getNewStockQuantity()
        );
    }
}