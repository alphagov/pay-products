package uk.gov.pay.products.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import static uk.gov.pay.products.util.ChargeJsonField.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Charge {

    private static final String PRICE_OVERRIDE = "price_override";

    @JsonProperty(CHARGE_EXTERNAL_ID)
    private String externalId;
    @JsonProperty(PRODUCT_EXTERNAL_ID)
    private String productExternalId;
    @JsonProperty(AMOUNT)
    private Long price;
    @JsonProperty(DESCRIPTION)
    private String description;

    public Charge(String externalId, String productExternalId, Long price, String description) {
        this.externalId = externalId;
        this.productExternalId = productExternalId;
        this.price = price;
        this.description = description;
    }

    public Charge(String productExternalId, Long price) {
        this(null, productExternalId, price, null);
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
