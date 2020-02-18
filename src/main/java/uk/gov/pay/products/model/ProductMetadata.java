package uk.gov.pay.products.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;

import java.util.HashMap;
import java.util.Map;

public class ProductMetadata {

    @JsonIgnore
    private Integer productId;
    @JsonIgnore
    private String key;
    @JsonIgnore
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

    public static ProductMetadata from(ProductMetadataEntity productMetadataEntity) {
        return new ProductMetadata(productMetadataEntity.getProductEntity().getId(), 
                productMetadataEntity.getMetadataKey(), 
                productMetadataEntity.getMetadataValue());
    }

    @JsonValue()
    public Map<String, String> getKvPair(){
        Map<String, String> map = new HashMap<>();
        map.put(this.key, this.value);
        return map;
    }
}
