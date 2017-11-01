package uk.gov.pay.products.persistence.entity;

import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.util.PaymentStatus;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "payments")
public class PaymentEntity extends AbstractEntity {

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "govuk_payment_id")
    private String govukPaymentId;

    @Column(name = "next_url")
    private String nextUrl;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "date_created")
    @Convert(converter = UTCDateTimeConverter.class)
    private ZonedDateTime dateCreated = ZonedDateTime.now(ZoneId.of("UTC"));

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "product_id", updatable = false)
    private ProductEntity productEntity;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    public PaymentEntity() {
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getGovukPaymentId() {
        return govukPaymentId;
    }

    public void setGovukPaymentId(String govukPaymentId) {
        this.govukPaymentId = govukPaymentId;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public ZonedDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(ZonedDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public ProductEntity getProductEntity() {
        return productEntity;
    }

    public void setProductEntity(ProductEntity productEntity) {
        this.productEntity = productEntity;
    }

    public PaymentStatus getStatus() { return status; }

    public void setStatus(PaymentStatus status) { this.status = status; }

    public Payment toPayment() {
        return new Payment(
                this.getExternalId(),
                this.getGovukPaymentId(),
                this.getNextUrl(),
                this.getAmount(),
                this.productEntity != null ? this.productEntity.getExternalId() : null,
                this.status,
                this.productEntity != null ? this.productEntity.getId() : null
        );
    }

    @Override
    public String toString() {
        return "PaymentEntity{" +
                "externalId='" + externalId + '\'' +
                ", govukPaymentId='" + govukPaymentId + '\'' +
                ", nextUrl='" + nextUrl + '\'' +
                ", amount=" + amount +
                ", dateCreated=" + dateCreated +
                ", productEntity=" + productEntity +
                ", status=" + status +
                '}';
    }
}
