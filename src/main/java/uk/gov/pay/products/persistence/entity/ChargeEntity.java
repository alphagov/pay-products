package uk.gov.pay.products.persistence.entity;

import uk.gov.pay.products.model.Charge;
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

    @Column(name = "product_external_id")
    private String productExternalId;

    @Column(name = "description")
    private String description;

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



    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static ChargeEntity from(Charge charge) {
        ChargeEntity chargeEntity = new ChargeEntity();
        chargeEntity.setProductExternalId(charge.getProductExternalId());
        chargeEntity.setPrice(charge.getPrice());
        return chargeEntity;
    }

    public Charge toCharge() {
        return new Charge(
                this.externalId,
                this.productExternalId,
                this.price,
                this.description
        );
    }

    public String getProductExternalId() {
        return productExternalId;
    }

    public void setProductExternalId(String productExternalId) {
        this.productExternalId = productExternalId;
    }
}
