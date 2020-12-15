package uk.gov.pay.products.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.products.util.ProductStatus;
import uk.gov.pay.products.util.ProductType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Product {

    public static final String FIELD_PAY_API_TOKEN = "pay_api_token";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_PRICE = "price";
    public static final String FIELD_EXTERNAL_ID = "external_id";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_GATEWAY_ACCOUNT_ID = "gateway_account_id";
    public static final String FIELD_RETURN_URL = "return_url";
    public static final String FIELD_SERVICE_NAME_PATH = "service_name_path";
    public static final String FIELD_PRODUCT_NAME_PATH = "product_name_path";
    public static final String FIELD_REFERENCE_ENABLED = "reference_enabled";
    public static final String FIELD_REFERENCE_LABEL = "reference_label";
    public static final String FIELD_REFERENCE_HINT = "reference_hint";
    public static final String FIELD_LANGUAGE = "language";
    public static final String FIELD_METADATA = "metadata";

    @JsonProperty(FIELD_EXTERNAL_ID)
    private final String externalId;
    @JsonProperty(FIELD_NAME)
    private final String name;
    @JsonProperty(FIELD_DESCRIPTION)
    private final String description;
    @JsonProperty(FIELD_PAY_API_TOKEN)
    private final String payApiToken;
    @JsonProperty(FIELD_PRICE)
    private final Long price;
    @JsonProperty(FIELD_STATUS)
    private final ProductStatus status;
    @JsonProperty(FIELD_TYPE)
    private final ProductType type;
    @JsonProperty(FIELD_GATEWAY_ACCOUNT_ID)
    private final Integer gatewayAccountId;
    private List<Link> links = new ArrayList<>();
    @JsonProperty(FIELD_RETURN_URL)
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
    @JsonProperty(FIELD_LANGUAGE)
    @JsonSerialize(using = ToStringSerializer.class)
    private final SupportedLanguage language;
    @JsonIgnore
    private List<ProductMetadata> metadata;


    public Product(String externalId,
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
                   SupportedLanguage language, 
                   List<ProductMetadata> metadata) {
        this(externalId, name, description, payApiToken, price, status, gatewayAccountId, type, returnUrl,
                serviceNamePath, productNamePath, false, null, null,
                language, metadata);
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
            String referenceHint,
            SupportedLanguage language, 
            List<ProductMetadata> metadata) {
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
        this.language = language;
        this.metadata = metadata;
    }

    public static Product from(JsonNode jsonPayload) {
        String externalId = (jsonPayload.get(FIELD_EXTERNAL_ID) != null) ? jsonPayload.get(FIELD_EXTERNAL_ID).asText() : randomUuid();
        String payApiToken = (jsonPayload.get(FIELD_PAY_API_TOKEN) != null) ? jsonPayload.get(FIELD_PAY_API_TOKEN).asText() : null;
        String name = (jsonPayload.get(FIELD_NAME) != null) ? jsonPayload.get(FIELD_NAME).asText() : null;
        Long price = (jsonPayload.get(FIELD_PRICE) != null) ? jsonPayload.get(FIELD_PRICE).asLong() : null;
        Integer gatewayAccountId = (jsonPayload.get(FIELD_GATEWAY_ACCOUNT_ID) != null ? jsonPayload.get(FIELD_GATEWAY_ACCOUNT_ID).asInt() : null);
        String description = (jsonPayload.get(FIELD_DESCRIPTION) != null) ? jsonPayload.get(FIELD_DESCRIPTION).asText() : null;
        ProductType type = (jsonPayload.get(FIELD_TYPE) != null) ? ProductType.valueOf(jsonPayload.get(FIELD_TYPE).asText()) : null;
        String returnUrl = (jsonPayload.get(FIELD_RETURN_URL) != null) ? jsonPayload.get(FIELD_RETURN_URL).asText() : null;
        String serviceNamePath = (jsonPayload.get(FIELD_SERVICE_NAME_PATH) != null) ? jsonPayload.get(FIELD_SERVICE_NAME_PATH).asText() : null;
        String productNamePath = (jsonPayload.get(FIELD_PRODUCT_NAME_PATH) != null) ? jsonPayload.get(FIELD_PRODUCT_NAME_PATH).asText() : null;
        Boolean referenceEnabled = (jsonPayload.get(FIELD_REFERENCE_ENABLED) != null) && jsonPayload.get(FIELD_REFERENCE_ENABLED).asBoolean();
        String referenceLabel = (jsonPayload.get(FIELD_REFERENCE_LABEL) != null) ? jsonPayload.get(FIELD_REFERENCE_LABEL).asText() : null;
        String referenceHint = (jsonPayload.get(FIELD_REFERENCE_HINT) != null) ? jsonPayload.get(FIELD_REFERENCE_HINT).asText() : null;
        SupportedLanguage language = Optional.ofNullable(jsonPayload.get(FIELD_LANGUAGE))
                .map(JsonNode::asText)
                .map(SupportedLanguage::fromIso639AlphaTwoCode)
                .orElse(SupportedLanguage.ENGLISH);
        List<ProductMetadata> metadataList = extractMetadata(jsonPayload);

        return new Product(externalId, name, description, payApiToken, price, ProductStatus.ACTIVE, gatewayAccountId,
                type, returnUrl, serviceNamePath, productNamePath, referenceEnabled, referenceLabel, referenceHint,
                language, metadataList);
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

    public Integer getGatewayAccountId() {
        return gatewayAccountId;
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

    public ProductType getType() {
        return type;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getServiceNamePath() {
        return serviceNamePath;
    }

    public String getProductNamePath() {
        return productNamePath;
    }

    public Boolean getReferenceEnabled() {
        return referenceEnabled;
    }

    public String getReferenceLabel() {
        return referenceLabel;
    }

    public String getReferenceHint() {
        return referenceHint;
    }

    public List<ProductMetadata> getMetadata() {
        return metadata;
    }

    public SupportedLanguage getLanguage() {
        return language;
    }

    @JsonProperty(FIELD_METADATA)
    public Map<String, String> getMetadataJson() {
        Map<String, String> map = new HashMap<>();
        if (metadata != null) {
            this.metadata.forEach(productMetadata -> map.put( productMetadata.getKey(), productMetadata.getValue()));
            return map.size() > 0 ? map : null;
        }
        return null;
    }

    private static List<ProductMetadata> extractMetadata(JsonNode payload) {
        List<ProductMetadata> metadataList = new ArrayList<>();
        JsonNode metadata = payload.get(FIELD_METADATA);
        if (metadata != null && !metadata.isEmpty()) {
            Iterator<String> fieldNames = metadata.fieldNames();
            while (fieldNames.hasNext()) {
                String key  = fieldNames.next();
                String value = metadata.get(key).textValue();
                metadataList.add(new ProductMetadata(null, key, value));
            }
        }
        return metadataList.isEmpty() ? null : metadataList;
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
                ", serviceNamePath='" + serviceNamePath + '\'' +
                ", productNamePath='" + productNamePath + '\'' +
                ", referenceEnabled='" + referenceEnabled +
                (referenceEnabled ? '\'' + ", referenceLabel='" + referenceLabel : "") +
                (referenceEnabled ? '\'' + ", referenceHint='" + referenceHint : "") +
                ", language='" + language.toString() + '\'' +
                '}';
    }
}
