package winylka.dto;

import winylka.model.StockSubscriptionType;

public class StockSubscriptionRequest {

    private String email;
    private StockSubscriptionType type;

    public StockSubscriptionRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public StockSubscriptionType getType() {
        return type;
    }

    public void setType(StockSubscriptionType type) {
        this.type = type;
    }
}