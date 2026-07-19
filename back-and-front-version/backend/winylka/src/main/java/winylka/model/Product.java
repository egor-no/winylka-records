package winylka.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String artist;
    private String name;

    @Column(name = "cat_no")
    private String catNo;
    private String format;
    private String note;
    private String img;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private Integer stockQuantity;

    public Product() {
    }

    public Product(int id, String artist, String name, String catNo, String format, String note, String img, BigDecimal price, String description, Integer stockQuantity) {
        this.id = id;
        this.artist = artist;
        this.name = name;
        this.catNo = catNo;
        this.format = format;
        this.note = note;
        this.img = img;
        this.price = price;
        this.description = description;
        this.stockQuantity = stockQuantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCatNo() {
        return catNo;
    }

    public void setCatNo(String catNo) {
        this.catNo = catNo;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal  price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStockQuantity() {return stockQuantity;}

    public void setStockQuantity(Integer stockQuantity) {this.stockQuantity = stockQuantity;}

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }
}
