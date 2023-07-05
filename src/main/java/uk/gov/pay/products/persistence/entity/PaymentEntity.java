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
import javax.persistence.Transient;
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
    private ProductEntity product;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "gateway_account_id")
    private Integer gatewayAccountId;

    @Column(name = "reference_number")
    private String referenceNumber;
    
    @Transient
    private int errorStatusCode;
    @Transient
    private String errorCode;

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
        return product;
    }

    public void setProductEntity(ProductEntity productEntity) {
        this.product = productEntity;
    }

    public PaymentStatus getStatus() { return status; }

    public void setStatus(PaymentStatus status) { this.status = status; }

    public int getErrorStatusCode() {
        return errorStatusCode;
    }

    public void setErrorStatusCode(int errorStatusCode) {
        this.errorStatusCode = errorStatusCode;
    }

    public Payment toPayment() {
        Payment payment = new Payment(
                this.getExternalId(),
                this.getGovukPaymentId(),
                this.getNextUrl(),
                this.getAmount(),
                this.product != null ? this.product.getExternalId() : null,
                this.status,
                this.product != null ? this.product.getId() : null,
                this.getReferenceNumber()
        );
        payment.setDateCreated(this.dateCreated);
        return payment;
    }

    @Override
    public String toString() {
        return "PaymentEntity{" +
                "externalId='" + externalId + '\'' +
                ", govukPaymentId='" + govukPaymentId + '\'' +
                ", nextUrl='" + nextUrl + '\'' +
                ", amount=" + amount +
                ", dateCreated=" + dateCreated +
                ", productEntity=" + product +
                ", status=" + status +
                '}';
    }

    public Integer getGatewayAccountId() {
        return gatewayAccountId;
    }

    public void setGatewayAccountId(Integer gatewayAccountId) {
        this.gatewayAccountId = gatewayAccountId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public void setErrorCode(String code) {
        this.errorCode = code;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
