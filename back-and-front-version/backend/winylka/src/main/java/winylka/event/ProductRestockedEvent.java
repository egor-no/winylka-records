package winylka.event;

public class ProductRestockedEvent {

    private final int productId;
    private final int oldStockQuantity;
    private final int newStockQuantity;

    public ProductRestockedEvent(
            int productId,
            int oldStockQuantity,
            int newStockQuantity
    ) {
        this.productId = productId;
        this.oldStockQuantity = oldStockQuantity;
        this.newStockQuantity = newStockQuantity;
    }

    public int getProductId() {
        return productId;
    }

    public int getOldStockQuantity() {
        return oldStockQuantity;
    }

    public int getNewStockQuantity() {
        return newStockQuantity;
    }
}