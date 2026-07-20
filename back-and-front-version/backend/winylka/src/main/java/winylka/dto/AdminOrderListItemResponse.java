package winylka.dto;

import winylka.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class AdminOrderListItemResponse {

    private Long id;
    private Instant createdAt;
    private String customerFullName;
    private String customerEmail;
    private int itemsCount;
    private BigDecimal itemsTotal;
    private OrderStatus status;

    public AdminOrderListItemResponse(
            Long id,
            Instant createdAt,
            String customerFullName,
            String customerEmail,
            int itemsCount,
            BigDecimal itemsTotal,
            OrderStatus status
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.customerFullName = customerFullName;
        this.customerEmail = customerEmail;
        this.itemsCount = itemsCount;
        this.itemsTotal = itemsTotal;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCustomerFullName() {
        return customerFullName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public BigDecimal getItemsTotal() {
        return itemsTotal;
    }

    public OrderStatus getStatus() {
        return status;
    }
}