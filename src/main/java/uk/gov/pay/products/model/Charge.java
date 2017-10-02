package uk.gov.pay.products.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class Charge {
    public static final String EXTERNAL_PRODUCT_ID = "external_product_id";
    public static final String PRICE_OVERRIDE = "price_override";
    private String externalProductId;
    private Long price;

    public Charge(@JsonProperty(EXTERNAL_PRODUCT_ID) String externalProductId,
                  @JsonProperty(PRICE_OVERRIDE) Long price) {
        this.externalProductId = externalProductId;
        this.price = price;
    }

    public String getExternalProductId() {
        return externalProductId;
    }

    public Long getPrice() {
        return price;
    }

    public static Charge fromPayload(JsonNode jsonPayload) {
        String externalproductid = (jsonPayload.get(EXTERNAL_PRODUCT_ID) != null) ? jsonPayload.get(EXTERNAL_PRODUCT_ID).asText() : null;
        Long price = (jsonPayload.get(PRICE_OVERRIDE) != null) ? jsonPayload.get(PRICE_OVERRIDE).asLong() : null;

        return new Charge(externalproductid, price);
    }
}
