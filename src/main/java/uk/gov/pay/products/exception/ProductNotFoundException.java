package uk.gov.pay.products.exception;

public class ProductNotFoundException extends RuntimeException {

    private String productExternalId;

    public ProductNotFoundException(String productExternalId) {
        this.productExternalId = productExternalId;
    }

    public String getProductExternalId() {
        return productExternalId;
    }
}
