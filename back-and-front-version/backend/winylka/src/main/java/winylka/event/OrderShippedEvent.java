package winylka.event;

import java.time.Instant;

public class OrderShippedEvent {

    private final long orderId;
    private final String customerFullName;
    private final String customerEmail;
    private final String trackingNumber;
    private final Instant shippedAt;

    public OrderShippedEvent(
            long orderId,
            String customerFullName,
            String customerEmail,
            String trackingNumber,
            Instant shippedAt
    ) {
        this.orderId = orderId;
        this.customerFullName = customerFullName;
        this.customerEmail = customerEmail;
        this.trackingNumber = trackingNumber;
        this.shippedAt = shippedAt;
    }

    public long getOrderId() {
        return orderId;
    }

    public String getCustomerFullName() {
        return customerFullName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }
}