package uk.gov.pay.products.exception;

public class PaymentCreationException extends RuntimeException {

    private final String productExternalId;
    private final int errorStatusCode;
    private final String errorCode;

    public PaymentCreationException(String productExternalId, int errorStatusCode, String errorCode) {
        this.productExternalId = productExternalId;
        this.errorStatusCode = errorStatusCode;
        this.errorCode = errorCode;
    }

    public String getProductExternalId() {
        return productExternalId;
    }

    public int getErrorStatusCode() {
        return errorStatusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
