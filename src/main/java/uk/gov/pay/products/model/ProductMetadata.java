package uk.gov.pay.products.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProductMetadata {

    @JsonIgnore
    private String key;
    @JsonIgnore
    private String value;

    public ProductMetadata(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public static ProductMetadata from(ProductMetadataEntity productMetadataEntity) {
        return new ProductMetadata(
                productMetadataEntity.getMetadataKey(),
                productMetadataEntity.getMetadataValue());
    }
    
    @JsonValue()
    public Map<String, String> getKvPair() {
        Map<String, String> map = new HashMap<>();
        map.put(this.key, this.value);
        return map;
    }

    @Override
    public String toString() {
        return "key= " + key + " value= " + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductMetadata that = (ProductMetadata) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
