package uk.gov.pay.products.service;

public class PaymentCreatorDownstreamException extends RuntimeException {

    private final Integer productId;

    public PaymentCreatorDownstreamException(Integer productId) {
        this.productId = productId;
    }

    public Integer getProductId() {
        return productId;
    }
}
