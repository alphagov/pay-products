package uk.gov.pay.products.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

import static uk.gov.pay.products.util.MetadataDeserializer.extractMetadata;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductUpdateRequest {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_PRICE = "price";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_REFERENCE_ENABLED = "reference_enabled";
    public static final String FIELD_REFERENCE_LABEL = "reference_label";
    public static final String FIELD_REFERENCE_HINT = "reference_hint";
    public static final String FIELD_METADATA = "metadata";

    private String name;
    private String description;
    private Long price;
    private Boolean referenceEnabled;
    private String referenceLabel;
    private String referenceHint;
    private List<ProductMetadata> metadata;

    public ProductUpdateRequest(
            String name,
            String description,
            Long price,
            Boolean referenceEnabled,
            String referenceLabel,
            String referenceHint,
            List<ProductMetadata> metadata) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.referenceEnabled = referenceEnabled;
        this.referenceLabel = referenceLabel;
        this.referenceHint = referenceHint;
        this.metadata = metadata;
    }

    public static ProductUpdateRequest from(JsonNode jsonPayload) {
        String name = (jsonPayload.get(FIELD_NAME) != null) ? jsonPayload.get(FIELD_NAME).asText() : null;
        Long price = (jsonPayload.get(FIELD_PRICE) != null) ? jsonPayload.get(FIELD_PRICE).asLong() : null;
        String description = (jsonPayload.get(FIELD_DESCRIPTION) != null) ? jsonPayload.get(FIELD_DESCRIPTION).asText() : null;
        Boolean referenceEnabled = (jsonPayload.get(FIELD_REFERENCE_ENABLED) != null) && jsonPayload.get(FIELD_REFERENCE_ENABLED).asBoolean();
        String referenceLabel = (jsonPayload.get(FIELD_REFERENCE_LABEL) != null) ? jsonPayload.get(FIELD_REFERENCE_LABEL).asText() : null;
        String referenceHint = (jsonPayload.get(FIELD_REFERENCE_HINT) != null) ? jsonPayload.get(FIELD_REFERENCE_HINT).asText() : null;
        List<ProductMetadata> metadataList = extractMetadata(jsonPayload, FIELD_METADATA);

        return new ProductUpdateRequest(name, description, price, referenceEnabled, referenceLabel, referenceHint, metadataList);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getPrice() {
        return price;
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
}
