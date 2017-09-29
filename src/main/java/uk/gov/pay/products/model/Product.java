package uk.gov.pay.products.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.products.util.ProductStatus;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Product {

    private static final String FIELD_EXTERNAL_SERVICE_ID = "external_service_id";
    private static final String FIELD_PAY_API_TOKEN = "pay_api_token";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PRICE = "price";
    private static final String EXTERNAL_ID = "external_id";
    private static final String DESCRIPTION = "description";
    private static final String STATUS = "status";
    private static final String CATALOGUE_EXTERNAL_ID = "catalogue_external_id";
    private static final String RETURN_URL = "return_url";

    private String externalId;
    private String externalServiceId;
    private String name;
    private String description;
    private String payApiToken;
    private Long price;
    private ProductStatus status;
    private String catalogueExternalId;
    private List<Link> links = new ArrayList<>();
    private String returnUrl;

    public Product(
            @JsonProperty(EXTERNAL_ID) String externalId,
            @JsonProperty(FIELD_EXTERNAL_SERVICE_ID) String externalServiceId,
            @JsonProperty(FIELD_NAME) String name,
            @JsonProperty(DESCRIPTION) String description,
            @JsonProperty(FIELD_PAY_API_TOKEN) String payApiToken,
            @JsonProperty(FIELD_PRICE) Long price,
            @JsonProperty(STATUS) ProductStatus status,
            @JsonProperty(CATALOGUE_EXTERNAL_ID) String catalogueExternalId,
            @JsonProperty(RETURN_URL) String returnUrl)
    {
        this.externalId = externalId;
        this.externalServiceId = externalServiceId;
        this.name = name;
        this.description = description;
        this.payApiToken = payApiToken;
        this.price = price;
        this.status = status;
        this.catalogueExternalId = catalogueExternalId;
        this.returnUrl = returnUrl;
    }

    public static Product from(JsonNode jsonPayload) {
        String externalServiceId = (jsonPayload.get(FIELD_EXTERNAL_SERVICE_ID) != null) ? jsonPayload.get(FIELD_EXTERNAL_SERVICE_ID).asText() : null;
        String paiApiToken = (jsonPayload.get(FIELD_PAY_API_TOKEN) != null) ? jsonPayload.get(FIELD_PAY_API_TOKEN).asText() : null;
        String name = (jsonPayload.get(FIELD_NAME) != null) ? jsonPayload.get(FIELD_NAME).asText() : null;
        Long price = (jsonPayload.get(FIELD_PRICE) != null) ? jsonPayload.get(FIELD_PRICE).asLong() : null;
        String description = (jsonPayload.get(DESCRIPTION) != null) ? jsonPayload.get(DESCRIPTION).asText() : null;
        String returnUrl = (jsonPayload.get(RETURN_URL) != null) ? jsonPayload.get(RETURN_URL).asText() : null;

        return new Product(randomUuid(), externalServiceId, name, description, paiApiToken,
                price, ProductStatus.ACTIVE, null, returnUrl);
    }

    public String getExternalServiceId() {
        return externalServiceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPayApiToken() {
        return payApiToken;
    }

    public Long getPrice() {
        return price;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public String getCatalogueExternalId() {
        return catalogueExternalId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    @JsonProperty("_links")
    public List<Link> getLinks() {
        return links;
    }

    public String getReturnUrl() {
        return returnUrl;
    }
}
