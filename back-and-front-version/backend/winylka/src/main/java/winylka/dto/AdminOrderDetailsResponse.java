package winylka.dto;

import winylka.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class AdminOrderDetailsResponse {

    private Long id;
    private Instant createdAt;

    private String customerFullName;
    private String customerEmail;
    private String customerPhone;

    private String shippingCity;
    private String shippingAddress;
    private String shippingPostalCode;

    private String comment;

    private List<AdminOrderItemResponse> items;
    private int itemsCount;
    private BigDecimal itemsTotal;

    private OrderStatus status;
    private Instant shippedAt;
    private String trackingNumber;

    public AdminOrderDetailsResponse(
            Long id,
            Instant createdAt,
            String customerFullName,
            String customerEmail,
            String customerPhone,
            String shippingCity,
            String shippingAddress,
            String shippingPostalCode,
            String comment,
            List<AdminOrderItemResponse> items,
            int itemsCount,
            BigDecimal itemsTotal,
            OrderStatus status,
            Instant shippedAt,
            String trackingNumber
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.customerFullName = customerFullName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.shippingCity = shippingCity;
        this.shippingAddress = shippingAddress;
        this.shippingPostalCode = shippingPostalCode;
        this.comment = comment;
        this.items = items;
        this.itemsCount = itemsCount;
        this.itemsTotal = itemsTotal;
        this.status = status;
        this.shippedAt = shippedAt;
        this.trackingNumber = trackingNumber;
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

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getShippingCity() {
        return shippingCity;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getShippingPostalCode() {
        return shippingPostalCode;
    }

    public String getComment() {
        return comment;
    }

    public List<AdminOrderItemResponse> getItems() {
        return items;
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

    public Instant getShippedAt() {
        return shippedAt;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}