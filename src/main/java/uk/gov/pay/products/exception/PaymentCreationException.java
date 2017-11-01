package uk.gov.pay.products.exception;

public class PaymentCreationException extends RuntimeException {

    private final String productExternalId;

    public PaymentCreationException(String productExternalId) {
        this.productExternalId = productExternalId;
    }

    public String getProductExternalId() {
        return productExternalId;
    }
}
