package winylka.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import winylka.service.OrderNotificationService;

@Component
public class OrderShippedEventListener {

    private final OrderNotificationService notificationService;

    public OrderShippedEventListener(
            OrderNotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(OrderShippedEvent event) {
        notificationService.notifyOrderShipped(
                event.getOrderId(),
                event.getCustomerFullName(),
                event.getCustomerEmail(),
                event.getTrackingNumber()
        );
    }
}