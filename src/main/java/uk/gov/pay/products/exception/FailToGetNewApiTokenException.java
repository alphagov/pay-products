package uk.gov.pay.products.exception;

public class FailToGetNewApiTokenException extends RuntimeException {

    public FailToGetNewApiTokenException(String message) {
        super(message);
    }
}
