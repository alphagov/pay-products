package uk.gov.pay.products.fixtures;

import org.apache.commons.lang3.RandomUtils;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;

public class ProductMetadataEntityFixture {
    
    private Integer id = RandomUtils.nextInt();
    private ProductEntity productEntity;
    private String metadataKey = "key";
    private String metadataValue = "value";

    private ProductMetadataEntityFixture() {
    }
    
    public ProductMetadataEntity build() {
        ProductMetadataEntity productMetadataEntity = new ProductMetadataEntity();
        productMetadataEntity.setId(this.id);
        productMetadataEntity.setProductId(this.productEntity);
        productMetadataEntity.setMetadataKey(this.metadataKey);
        productMetadataEntity.setMetadataValue(this.metadataValue);
        return productMetadataEntity;
    }

    public static ProductMetadataEntityFixture aProductMetadataEntity() {
        return new ProductMetadataEntityFixture();
    }

    public ProductMetadataEntityFixture withId(Integer id) {
        this.id = id;
        return this;
    }

    public ProductMetadataEntityFixture withProductEntity(ProductEntity productEntity) {
        this.productEntity = productEntity;
        return this;
    }

    public ProductMetadataEntityFixture withMetadataKey(String metadataKey) {
        this.metadataKey = metadataKey;
        return this;
    }

    public ProductMetadataEntityFixture withMetadataValue(String metadataValue) {
        this.metadataValue = metadataValue;
        return this;
    }

}
