package uk.gov.pay.products.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
    private static final String FIELD_SERVICE_NAME_PATH = "service_name_path";
    private static final String FIELD_PRODUCT_NAME_PATH = "product_name_path";
    private static final String FIELD_REFERENCE_ENABLED = "reference_enabled";
    private static final String FIELD_REFERENCE_LABEL = "reference_label";
    private static final String FIELD_REFERENCE_HINT = "reference_hint";

    @JsonProperty(EXTERNAL_ID)
    private final String externalId;
    @JsonProperty(FIELD_NAME)
    private final String name;
    @JsonProperty(DESCRIPTION)
    private final String description;
    @JsonProperty(FIELD_PAY_API_TOKEN)
    private final String payApiToken;
    @JsonProperty(FIELD_PRICE)
    private final Long price;
    @JsonProperty(STATUS)
    private final ProductStatus status;
    @JsonProperty(TYPE)
    private final ProductType type;
    @JsonProperty(FIELD_GATEWAY_ACCOUNT_ID)
    private final Integer gatewayAccountId;
    private List<Link> links = new ArrayList<>();
    @JsonProperty(RETURN_URL)
    private final String returnUrl;
    @JsonProperty(FIELD_SERVICE_NAME_PATH)
    private final String serviceNamePath;
    @JsonProperty(FIELD_PRODUCT_NAME_PATH)
    private final String productNamePath;
    @JsonProperty(FIELD_REFERENCE_ENABLED)
    private final Boolean referenceEnabled;
    @JsonProperty(FIELD_REFERENCE_LABEL)
    private final String referenceLabel;
    @JsonProperty(FIELD_REFERENCE_HINT)
    private final String referenceHint;

    public Product(String externalId, String name, String description, String payApiToken, Long price, 
                   ProductStatus status, Integer gatewayAccountId, ProductType type,
                   String returnUrl, String serviceNamePath, String productNamePath)
    {
        this(externalId, name, description, payApiToken, price, status, gatewayAccountId, type,
                returnUrl, serviceNamePath, productNamePath, false, null, null);
    }

    public Product(
            String externalId,
            String name,
            String description,
            String payApiToken,
            Long price,
            ProductStatus status,
            Integer gatewayAccountId,
            ProductType type,
            String returnUrl,
            String serviceNamePath,
            String productNamePath,
            Boolean referenceEnabled,
            String referenceLabel,
            String referenceHint)
    {
        this.externalId = externalId;
        this.name = name;
        this.description = description;
        this.payApiToken = payApiToken;
        this.price = price;
        this.status = status;
        this.gatewayAccountId = gatewayAccountId;
        this.type = type;
        this.returnUrl = returnUrl;
        this.serviceNamePath = serviceNamePath;
        this.productNamePath = productNamePath;
        this.referenceEnabled = referenceEnabled;
        this.referenceLabel = referenceLabel;
        this.referenceHint = referenceHint;
    }

    public static Product from(JsonNode jsonPayload) {
        String externalId = (jsonPayload.get(EXTERNAL_ID) != null) ? jsonPayload.get(EXTERNAL_ID).asText() : randomUuid();
        String payApiToken = (jsonPayload.get(FIELD_PAY_API_TOKEN) != null) ? jsonPayload.get(FIELD_PAY_API_TOKEN).asText() : null;
        String name = (jsonPayload.get(FIELD_NAME) != null) ? jsonPayload.get(FIELD_NAME).asText() : null;
        Long price = (jsonPayload.get(FIELD_PRICE) != null) ? jsonPayload.get(FIELD_PRICE).asLong() : null;
        Integer gatewayAccountId = (jsonPayload.get(FIELD_GATEWAY_ACCOUNT_ID) != null ? jsonPayload.get(FIELD_GATEWAY_ACCOUNT_ID).asInt() : null);
        String description = (jsonPayload.get(DESCRIPTION) != null) ? jsonPayload.get(DESCRIPTION).asText() : null;
        ProductType type = (jsonPayload.get(TYPE) != null) ? ProductType.valueOf(jsonPayload.get(TYPE).asText()) : null;
        String returnUrl = (jsonPayload.get(RETURN_URL) != null) ? jsonPayload.get(RETURN_URL).asText() : null;
        String serviceNamePath = (jsonPayload.get(FIELD_SERVICE_NAME_PATH) != null) ? jsonPayload.get(FIELD_SERVICE_NAME_PATH).asText() : null;
        String productNamePath = (jsonPayload.get(FIELD_PRODUCT_NAME_PATH) != null) ? jsonPayload.get(FIELD_PRODUCT_NAME_PATH).asText() : null;
        Boolean referenceEnabled = (jsonPayload.get(FIELD_REFERENCE_ENABLED) != null) && jsonPayload.get(FIELD_REFERENCE_ENABLED).asBoolean();
        String referenceLabel = (jsonPayload.get(FIELD_REFERENCE_LABEL) != null) ? jsonPayload.get(FIELD_REFERENCE_LABEL).asText() : null;
        String referenceHint = (jsonPayload.get(FIELD_REFERENCE_HINT) != null) ? jsonPayload.get(FIELD_REFERENCE_HINT).asText() : null;
        
        return new Product(externalId, name, description, payApiToken,
                price, ProductStatus.ACTIVE, gatewayAccountId, type, returnUrl,
                serviceNamePath, productNamePath, referenceEnabled, referenceLabel, referenceHint);
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

    public String getServiceNamePath() { return serviceNamePath; }

    public String getProductNamePath() { return productNamePath; }

    public Boolean getReferenceEnabled() { return referenceEnabled; }
    
    public String getReferenceLabel() { return referenceLabel; }
    
    public String getReferenceHint() { return referenceHint; }

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
                ", serviceNamePath='" + serviceNamePath + '\'' +
                ", productNamePath='" + productNamePath + '\'' +
                ", referenceEnabled='" + referenceEnabled +
                (referenceEnabled ? '\'' + ", referenceLabel='" + referenceLabel : "") +
                (referenceEnabled ? '\'' + ", referenceHint='" + referenceHint : "") +
                '}';
    }
}
