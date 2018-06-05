package uk.gov.pay.products.exception;

public class ConflictingPaymentRequestException extends RuntimeException {
    
    public ConflictingPaymentRequestException(String message) {
        super(message);
    }
}
