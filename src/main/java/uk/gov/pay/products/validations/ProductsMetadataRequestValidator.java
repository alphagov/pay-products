package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import uk.gov.pay.products.model.ProductMetadata;
import uk.gov.pay.products.util.Errors;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.function.Predicate.isEqual;

public class ProductsMetadataRequestValidator {

    private static final int MAX_NUMBER_OF_METADATA_ALLOWED = 10;
    private static final int MAX_KEY_FIELD_LENGTH = 30;
    private static final int MAX_VALUE_FIELD_LENGTH = 50;
    private static final String MAX_NUMBER_OF_METADATA_ALLOWED_ERROR_MSG = format("Maximum number of allowed metadata [ %s ] exceeded", MAX_NUMBER_OF_METADATA_ALLOWED);

    @Inject
    public ProductsMetadataRequestValidator() {
    }

    public Optional<Errors> validateCreateRequest(JsonNode payload, List<ProductMetadata> existingMetadataList) {
        if (existingMetadataList.size() >= MAX_NUMBER_OF_METADATA_ALLOWED) {
            return Optional.of(Errors.from(MAX_NUMBER_OF_METADATA_ALLOWED_ERROR_MSG));
        }
        if(payload.isEmpty()) {
            return Optional.of(Errors.from("Empty payload is not allowed"));
        }
        if (payload.size() > 1) {
            return Optional.of(Errors.from("Only one key-value pair is allowed"));
        }

        String key = payload.fieldNames().next();

        if (existingMetadataList
                .stream()
                .map(ProductMetadata::getKey)
                .map(String::toLowerCase)
                .anyMatch(isEqual(key.toLowerCase()))) {
            return Optional.of(Errors.from(format("Key [ %s ] already exists, duplicate keys not allowed", key)));
        }

        if (key.length() > MAX_KEY_FIELD_LENGTH) {
            return Optional.of(Errors.from(format("Maximum key field length is [ %s ]", MAX_KEY_FIELD_LENGTH)));
        }

        if (payload.get(key).asText().length() > MAX_VALUE_FIELD_LENGTH) {
            return Optional.of(Errors.from(format("Maximum value field length is [ %s ]", MAX_VALUE_FIELD_LENGTH)));
        }

        return Optional.empty();
    }
}
