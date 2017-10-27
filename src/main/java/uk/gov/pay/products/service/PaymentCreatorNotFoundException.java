package uk.gov.pay.products.service;

public class PaymentCreatorNotFoundException extends RuntimeException {

    private final Integer productId;

    public PaymentCreatorNotFoundException(Integer productId) {
        this.productId = productId;
    }

    public Integer getProductId() {
        return productId;
    }
}
