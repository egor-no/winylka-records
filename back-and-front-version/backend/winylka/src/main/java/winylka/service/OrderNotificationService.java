package winylka.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderNotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(OrderNotificationService.class);

    public void notifyOrderShipped(
            long orderId,
            String customerFullName,
            String customerEmail,
            String trackingNumber
    ) {
        String message = buildShippedMessage(
                orderId,
                customerFullName,
                trackingNumber
        );

        log.info(
                "Order shipment notification. orderId={}, email={}, message={}",
                orderId,
                customerEmail,
                message
        );
    }

    private String buildShippedMessage(
            long orderId,
            String customerFullName,
            String trackingNumber
    ) {
        StringBuilder message = new StringBuilder();

        message.append("Hello");

        if (customerFullName != null
                && !customerFullName.isBlank()) {
            message.append(", ").append(customerFullName.trim());
        }

        message.append("! Your order #")
                .append(orderId)
                .append(" has been shipped.");

        if (trackingNumber != null
                && !trackingNumber.isBlank()) {
            message.append(" Tracking number: ")
                    .append(trackingNumber.trim())
                    .append(".");
        }

        return message.toString();
    }
}