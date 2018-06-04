package uk.gov.pay.products.exception;

public class BadPaymentRequestException extends RuntimeException {
    
    public BadPaymentRequestException(String message) {
        super(message);
    }
}
