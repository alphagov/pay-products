package uk.gov.pay.products.persistence.entity;

import uk.gov.pay.products.model.ProductMetadata;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "products_metadata")
public class ProductMetadataEntity extends AbstractEntity {

    @ManyToOne()
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private ProductEntity productEntity;

    @Column(name = "metadata_key")
    private String metadataKey;

    @Column(name = "metadata_value")
    private String metadataValue;

    public ProductMetadataEntity() {
        //for jpa
    }

    public ProductEntity getProductEntity() {
        return productEntity;
    }

    public String getMetadataKey() {
        return metadataKey;
    }

    public String getMetadataValue() {
        return metadataValue;
    }

    public void setProductEntity(ProductEntity productEntity) {
        this.productEntity = productEntity;
    }

    public void setMetadataKey(String metadataKey) {
        this.metadataKey = metadataKey;
    }

    public void setMetadataValue(String metadataValue) {
        this.metadataValue = metadataValue;
    }

    @Override
    public String toString() {
        return "ProductMetadataEntity{" +
                "productEntity=" + productEntity +
                ", metadataKey='" + metadataKey + '\'' +
                ", metadataValue='" + metadataValue + '\'' +
                '}';
    }

    public ProductMetadata toMetadata() {
        return new ProductMetadata(this.metadataKey, this.metadataValue);
    }

    public static ProductMetadataEntity from(ProductEntity productEntity, ProductMetadata metadata) {
        ProductMetadataEntity pmde = new ProductMetadataEntity();
        pmde.setProductEntity(productEntity);
        pmde.setMetadataKey(metadata.getKey());
        pmde.setMetadataValue(metadata.getValue());
        return pmde;

    }
}
