package uk.gov.pay.products.exception;

public class MetadataNotFoundException extends RuntimeException {

    private final String productExternalId;
    private final String key;

    public MetadataNotFoundException(String productExternalId, String key) {
        this.productExternalId = productExternalId;
        this.key = key;
    }

    public String getProductExternalId() {
        return productExternalId;
    }

    public String getKey() {
        return key;
    }
}
