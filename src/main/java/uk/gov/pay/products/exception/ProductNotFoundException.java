package uk.gov.pay.products.exception;

public class ProductNotFoundException extends RuntimeException {

    private String productExternalId;

    public ProductNotFoundException(String message, String productExternalId) {
        super(message);
        this.productExternalId = productExternalId;
    }

    public String getProductExternalId() {
        return productExternalId;
    }
}
