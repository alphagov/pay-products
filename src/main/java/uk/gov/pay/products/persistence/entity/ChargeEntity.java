package uk.gov.pay.products.persistence.entity;

import uk.gov.pay.products.util.ChargeStatus;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "charges")
public class ChargeEntity extends AbstractEntity {

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "date_created")
    @Convert(converter = UTCDateTimeConverter.class)
    private ZonedDateTime dateCreated = ZonedDateTime.now(ZoneId.of("UTC"));

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ChargeStatus status = ChargeStatus.CREATED;

    @Column(name = "price")
    private Long price;

    @ManyToOne
    @JoinColumn(name = "product_id", updatable = false)
    private ProductEntity productEntity;

    public ChargeEntity() {
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public ZonedDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(ZonedDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public ChargeStatus getStatus() {
        return status;
    }

    public void setStatus(ChargeStatus status) {
        this.status = status;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public ProductEntity getProductEntity() {
        return productEntity;
    }

    public void setProductEntity(ProductEntity productEntity) {
        this.productEntity = productEntity;
    }
}
