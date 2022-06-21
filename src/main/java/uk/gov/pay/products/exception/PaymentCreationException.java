package uk.gov.pay.products.exception;

public class PaymentCreationException extends RuntimeException {

    private final String productExternalId;
    private final int errorStatusCode;

    public PaymentCreationException(String productExternalId, int errorStatusCode) {
        this.productExternalId = productExternalId;
        this.errorStatusCode = errorStatusCode;
    }

    public String getProductExternalId() {
        return productExternalId;
    }

    public int getErrorStatusCode() {
        return errorStatusCode;
    }
}
