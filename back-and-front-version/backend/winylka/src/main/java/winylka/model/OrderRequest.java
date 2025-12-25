package winylka.model;

import java.util.List;

public class OrderRequest {
    private Customer customer;
    private Shipping shipping;
    private String comment;
    private List<OrderItemRequest> items;

    public static class Customer {
        public String fullName;
        public String email;
        public String phone;
    }

    public static class Shipping {
        public String city;
        public String address;
        public String postalCode;
    }

    public OrderRequest() {
    }

    public OrderRequest(Customer customer, Shipping shipping, String comment, List<OrderItemRequest> items) {
        this.customer = customer;
        this.shipping = shipping;
        this.comment = comment;
        this.items = items;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Shipping getShipping() {
        return shipping;
    }

    public void setShipping(Shipping shipping) {
        this.shipping = shipping;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}

