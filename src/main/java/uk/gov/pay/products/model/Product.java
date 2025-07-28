package uk.gov.pay.products.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.products.util.ProductStatus;
import uk.gov.pay.products.util.ProductType;
import uk.gov.service.payments.commons.api.json.IsoInstantMillisecondSerializer;
import uk.gov.service.payments.commons.model.SupportedLanguage;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.pay.products.util.MetadataDeserializer.extractMetadata;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
    public static final String FIELD_REQUIRE_CAPTCHA = "require_captcha";
    public static final String FIELD_AMOUNT_HINT = "amount_hint";

    @JsonProperty(FIELD_EXTERNAL_ID)
    @Schema(example = "874h5c87834659q345698495")
    private final String externalId;
    @Schema(example = "A name for the product")
    @JsonProperty(FIELD_NAME)
    private final String name;
    @Schema(example = "Description of the product")
    @JsonProperty(FIELD_DESCRIPTION)
    private final String description;
    @JsonProperty(FIELD_PAY_API_TOKEN)
    private final String payApiToken;
    @Schema(example = "1050")
    @JsonProperty(FIELD_PRICE)
    private final Long price;
    @Schema(example = "ACTIVE")
    @JsonProperty(FIELD_STATUS)
    private final ProductStatus status;
    @Schema(example = "DEMO")
    @JsonProperty(FIELD_TYPE)
    private final ProductType type;
    @Schema(example = "1")
    @JsonProperty(FIELD_GATEWAY_ACCOUNT_ID)
    private final Integer gatewayAccountId;
    private List<Link> links = new ArrayList<>();
    @Schema(example = "https://some.valid.url/")
    @JsonProperty(FIELD_RETURN_URL)
    private final String returnUrl;
    @Schema(example = "some-awesome-government-service")
    @JsonProperty(FIELD_SERVICE_NAME_PATH)
    private final String serviceNamePath;
    @Schema(example = "name-for-product")
    @JsonProperty(FIELD_PRODUCT_NAME_PATH)
    private final String productNamePath;
    @Schema(example = "true")
    @JsonProperty(FIELD_REFERENCE_ENABLED)
    private final Boolean referenceEnabled;
    @Schema(example = "Amount for your licence")
    @JsonProperty(FIELD_REFERENCE_LABEL)
    private final String referenceLabel;
    @Schema(example = "This can be found on your letter")
    @JsonProperty(FIELD_REFERENCE_HINT)
    private final String referenceHint;
    @Schema(example = "Enter an amount in multiples of £2 for the number of permits required")
    @JsonProperty(FIELD_AMOUNT_HINT)
    private final String amountHint;
    @Schema(example = "en")
    @JsonProperty(FIELD_LANGUAGE)
    @JsonSerialize(using = ToStringSerializer.class)
    private final SupportedLanguage language;
    @Schema(example = "false")
    @JsonProperty(FIELD_REQUIRE_CAPTCHA)
    private final Boolean requireCaptcha;
    @JsonIgnore
    private List<ProductMetadata> metadata;
    @JsonSerialize(using = IsoInstantMillisecondSerializer.class)
    private Instant dateCreated;

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
                null, language, false, metadata, null);
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
            String amountHint,
            SupportedLanguage language,
            Boolean requireCaptcha,
            List<ProductMetadata> metadata,
            Instant dateCreated) {
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
        this.amountHint = amountHint;
        this.language = language;
        this.requireCaptcha = requireCaptcha;
        this.metadata = metadata;
        this.dateCreated = dateCreated;
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
        String amountHint = (jsonPayload.get(FIELD_AMOUNT_HINT) != null) ? jsonPayload.get(FIELD_AMOUNT_HINT).asText() : null;
        SupportedLanguage language = Optional.ofNullable(jsonPayload.get(FIELD_LANGUAGE))
                .map(JsonNode::asText)
                .map(SupportedLanguage::fromIso639AlphaTwoCode)
                .orElse(SupportedLanguage.ENGLISH);
        List<ProductMetadata> metadataList = extractMetadata(jsonPayload, FIELD_METADATA);

        return new Product(externalId, name, description, payApiToken, price, ProductStatus.ACTIVE, gatewayAccountId,
                type, returnUrl, serviceNamePath, productNamePath, referenceEnabled, referenceLabel, referenceHint,
                amountHint, language, false, metadataList, null);
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
    @Schema(name = "_links")
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

    public String getAmountHint() {
        return amountHint;
    }

    public List<ProductMetadata> getMetadata() {
        return metadata;
    }

    public SupportedLanguage getLanguage() {
        return language;
    }

    public Boolean isRequireCaptcha() {
        return requireCaptcha;
    }

    @JsonProperty(FIELD_METADATA)
    public Map<String, String> getMetadataJson() {
        Map<String, String> map = new HashMap<>();
        if (metadata != null) {
            this.metadata.forEach(productMetadata -> map.put(productMetadata.getKey(), productMetadata.getValue()));
            return map.size() > 0 ? map : null;
        }
        return null;
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
                ", referenceEnabled=" + referenceEnabled +
                (referenceEnabled ? '\'' + ", referenceLabel='" + referenceLabel : "") +
                (referenceEnabled ? '\'' + ", referenceHint='" + referenceHint : "") +
                (price == null ? "" : '\'' + ", amountHint='" + amountHint) +
                ", language='" + language.toString() + '\'' +
                ", requireCaptcha=" + requireCaptcha +
                '}';
    }
}
