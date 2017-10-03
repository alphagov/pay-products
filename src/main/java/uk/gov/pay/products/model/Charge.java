package uk.gov.pay.products.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class Charge {

    private static final String PRODUCT_EXTERNAL_ID = "product_external_id";
    private static final String PRICE_OVERRIDE = "price_override";
    private static final String CHARGE_EXTERNAL_ID = "charge_external_id";
    private static final String DESCRIPTION = "description";

    private String externalId;
    private String productExternalId;
    private Long price;
    private String description;

    public Charge(
            @JsonProperty(CHARGE_EXTERNAL_ID) String externalId,
            @JsonProperty(PRODUCT_EXTERNAL_ID) String productExternalId,
            @JsonProperty(PRICE_OVERRIDE) Long price,
            @JsonProperty(DESCRIPTION) String description) {
        this.externalId = externalId;
        this.productExternalId = productExternalId;
        this.price = price;
        this.description = description;
    }

    public Charge(@JsonProperty(PRODUCT_EXTERNAL_ID) String productExternalId,
                  @JsonProperty(PRICE_OVERRIDE) Long price) {
        this(null,
                productExternalId,
                price,
                null);
    }

    public String getProductExternalId() {
        return productExternalId;
    }

    public Long getPrice() {
        return price;
    }

    public static Charge fromPayload(JsonNode jsonPayload) {
        String productExternalId = (jsonPayload.get(PRODUCT_EXTERNAL_ID) != null) ?
                jsonPayload.get(PRODUCT_EXTERNAL_ID).asText() : null;
        Long priceOverride = (jsonPayload.get(PRICE_OVERRIDE) != null) ?
                jsonPayload.get(PRICE_OVERRIDE).asLong() : null;

        return new Charge(productExternalId, priceOverride);
    }

    public String getExternalId() {
        return externalId;
    }

    public String getDescription() {
        return description;
    }
}
