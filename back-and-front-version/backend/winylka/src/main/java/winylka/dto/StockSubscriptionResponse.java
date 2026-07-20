package winylka.dto;

public class StockSubscriptionResponse {

    private final String message;
    private final boolean alreadySubscribed;

    public StockSubscriptionResponse(
            String message,
            boolean alreadySubscribed
    ) {
        this.message = message;
        this.alreadySubscribed = alreadySubscribed;
    }

    public String getMessage() {
        return message;
    }

    public boolean isAlreadySubscribed() {
        return alreadySubscribed;
    }
}