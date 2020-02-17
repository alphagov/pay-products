package uk.gov.pay.products.model;

public class ProductMetadata {

    private Integer productId;
    private String key;
    private String value;

    public ProductMetadata(Integer productId, String key, String value) {
        this.productId = productId;
        this.key = key;
        this.value = value;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
