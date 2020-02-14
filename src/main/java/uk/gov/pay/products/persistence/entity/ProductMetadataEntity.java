package uk.gov.pay.products.persistence.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "products_metadata")
public class ProductMetadataEntity extends AbstractEntity {

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "product_id", updatable = false)
    private ProductEntity productId;

    @Column(name = "metadata_key")
    private String metadataKey;

    @Column(name = "metadata_value")
    private String metadataValue;

    public ProductMetadataEntity() {
        //for jpa
    }

    public ProductEntity getProductId() {
        return productId;
    }

    public String getMetadataKey() {
        return metadataKey;
    }

    public String getMetadataValue() {
        return metadataValue;
    }

    public void setProductId(ProductEntity productId) {
        this.productId = productId;
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
                "productId=" + productId +
                ", metadataKey='" + metadataKey + '\'' +
                ", metadataValue='" + metadataValue + '\'' +
                '}';
    }
}
