package uk.gov.pay.products.exception;

public class PaymentCreatorDownstreamException extends RuntimeException {

    private final String productExternalId;

    public PaymentCreatorDownstreamException(String productExternalId) {
        this.productExternalId = productExternalId;
    }

    public String getProductExternalId() {
        return productExternalId;
    }
}
