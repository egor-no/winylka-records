package winylka.dto;

import java.math.BigDecimal;

public class AdminOrderItemResponse {

    private Integer productId;
    private String artist;
    private String album;
    private Integer amount;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public AdminOrderItemResponse(
            Integer productId,
            String artist,
            String album,
            Integer amount,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
        this.productId = productId;
        this.artist = artist;
        this.album = album;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public Integer getAmount() {
        return amount;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}