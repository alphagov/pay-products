package uk.gov.pay.products.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;
import uk.gov.pay.products.util.ProductType;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Product {

    private static final String FIELD_PAY_API_TOKEN = "pay_api_token";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PRICE = "price";
    private static final String EXTERNAL_ID = "external_id";
    private static final String DESCRIPTION = "description";
    private static final String STATUS = "status";
    private static final String TYPE = "type";
    private static final String FIELD_GATEWAY_ACCOUNT_ID = "gateway_account_id";
    private static final String RETURN_URL = "return_url";
    private static final String FIELD_SERVICE_NAME = "service_name";

    private String externalId;
    private String name;
    private String description;
    private String payApiToken;
    private Long price;
    private ProductStatus status;
    private ProductType type;
    private Integer gatewayAccountId;
    private List<Link> links = new ArrayList<>();
    private String returnUrl;
    private String serviceName;

    public Product(
            @JsonProperty(EXTERNAL_ID) String externalId,
            @JsonProperty(FIELD_NAME) String name,
            @JsonProperty(DESCRIPTION) String description,
            @JsonProperty(FIELD_PAY_API_TOKEN) String payApiToken,
            @JsonProperty(FIELD_PRICE) Long price,
            @JsonProperty(STATUS) ProductStatus status,
            @JsonProperty(FIELD_GATEWAY_ACCOUNT_ID) Integer gatewayAccountId,
            @JsonProperty(FIELD_SERVICE_NAME) String serviceName,
            @JsonProperty(TYPE) ProductType type,
            @JsonProperty(RETURN_URL) String returnUrl)
    {
        this.externalId = externalId;
        this.name = name;
        this.description = description;
        this.payApiToken = payApiToken;
        this.price = price;
        this.status = status;
        this.gatewayAccountId = gatewayAccountId;
        this.serviceName = serviceName;
        this.type = type;
        this.returnUrl = returnUrl;
    }

    public static Product from(JsonNode jsonPayload) {
        String payApiToken = (jsonPayload.get(FIELD_PAY_API_TOKEN) != null) ? jsonPayload.get(FIELD_PAY_API_TOKEN).asText() : null;
        String name = (jsonPayload.get(FIELD_NAME) != null) ? jsonPayload.get(FIELD_NAME).asText() : null;
        Long price = (jsonPayload.get(FIELD_PRICE) != null) ? jsonPayload.get(FIELD_PRICE).asLong() : null;
        Integer gatewayAccountId = (jsonPayload.get(FIELD_GATEWAY_ACCOUNT_ID) != null ? jsonPayload.get(FIELD_GATEWAY_ACCOUNT_ID).asInt() : null);
        String description = (jsonPayload.get(DESCRIPTION) != null) ? jsonPayload.get(DESCRIPTION).asText() : null;
        ProductType type = (jsonPayload.get(TYPE) != null) ? ProductType.valueOf(jsonPayload.get(TYPE).asText()) : null;
        String returnUrl = (jsonPayload.get(RETURN_URL) != null) ? jsonPayload.get(RETURN_URL).asText() : null;
        String serviceName = (jsonPayload.get(FIELD_SERVICE_NAME) != null) ? jsonPayload.get(FIELD_SERVICE_NAME).asText() : null;

        return new Product(randomUuid(), name, description, payApiToken,
                price, ProductStatus.ACTIVE, gatewayAccountId, serviceName, type, returnUrl);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @JsonIgnore
    public String getPayApiToken() {
        return payApiToken;
    }

    public Long getPrice() {
        return price;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public Integer getGatewayAccountId() { return gatewayAccountId; }

    public String getExternalId() {
        return externalId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    @JsonProperty("_links")
    public List<Link> getLinks() {
        return links;
    }

    public ProductType getType() {
        return type;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    @Override
    public String toString() {
        return "Product{" +
                "externalId='" + externalId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", type=" + type +
                ", gatewayAccountId=" + gatewayAccountId +
                ", links=" + links +
                ", returnUrl='" + returnUrl + '\'' +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }
}
