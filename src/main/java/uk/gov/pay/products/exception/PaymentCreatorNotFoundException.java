package uk.gov.pay.products.exception;

public class PaymentCreatorNotFoundException extends RuntimeException {

    private final String productExternalId;

    public PaymentCreatorNotFoundException(String productExternalId) {
        this.productExternalId = productExternalId;
    }

    public String getProductExternalId() {
        return productExternalId;
    }
}
