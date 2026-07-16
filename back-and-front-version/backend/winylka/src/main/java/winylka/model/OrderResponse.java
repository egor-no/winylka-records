package winylka.model;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderResponse {
    private long orderId;
    private BigDecimal itemsTotal;
    private Instant createdAt;

    public OrderResponse() {
    }

    public OrderResponse(long orderId, BigDecimal itemsTotal, Instant createdAt) {
        this.orderId = orderId;
        this.itemsTotal = itemsTotal;
        this.createdAt = createdAt;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getItemsTotal() {
        return itemsTotal;
    }

    public void setItemsTotal(BigDecimal itemsTotal) {
        this.itemsTotal = itemsTotal;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
