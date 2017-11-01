package uk.gov.pay.products.fixtures;

import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.PaymentStatus;

import java.time.ZonedDateTime;

import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class PaymentEntityFixture {

    private String externalId = randomUuid();
    private String govukPaymentId = randomUuid();
    private String nextUrl = "http://localhost:8080/v1/api/next";
    private ZonedDateTime dateCreated = ZonedDateTime.now();
    private PaymentStatus status = PaymentStatus.CREATED;
    private Long amount = 100L;

    private ProductEntity productEntity;

    private PaymentEntityFixture() {
        productEntity = ProductEntityFixture.aProductEntity().build();
    }

    public PaymentEntity build() {
        PaymentEntity payment = new PaymentEntity();
        payment.setExternalId(externalId);
        payment.setGovukPaymentId(govukPaymentId);
        payment.setNextUrl(nextUrl);
        payment.setDateCreated(dateCreated);
        payment.setStatus(status);
        payment.setProductEntity(productEntity);
        payment.setAmount(amount);

        return payment;
    }

    public static PaymentEntityFixture aPaymentEntity() {
        return new PaymentEntityFixture();
    }

    public PaymentEntityFixture withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public PaymentEntityFixture withGovukPaymentId(String govukPaymentId) {
        this.govukPaymentId = govukPaymentId;
        return this;
    }

    public PaymentEntityFixture withNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
        return this;
    }

    public PaymentEntityFixture withDateCreated(ZonedDateTime dateCreated) {
        this.dateCreated = dateCreated;
        return this;
    }

    public PaymentEntityFixture withStatus(PaymentStatus status) {
        this.status = status;
        return this;
    }

    public PaymentEntityFixture withAmount(Long amount) {
        this.amount = amount;
        return this;
    }

    public PaymentEntityFixture withProduct(ProductEntity productEntity) {
        this.productEntity = productEntity;
        return this;
    }
}
