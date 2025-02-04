package uk.gov.pay.products.exception;

public class PaymentCreationException extends RuntimeException {

    private final String productExternalId;
    private final int errorStatusCode;
    private final String errorCode;

    public PaymentCreationException(String productExternalId, int errorStatusCode, String errorCode, String errorDescription) {
        super(errorDescription);
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

    @Override
    public String toString() {
        return "PaymentCreationException{" +
                "productExternalId='" + productExternalId + '\'' +
                ", errorStatusCode=" + errorStatusCode +
                ", errorCode='" + errorCode + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
