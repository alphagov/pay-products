package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
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
    private static final String EMPTY_PAYLOAD_ERROR_MSG = "Empty payload is not allowed";
    private static final String MORE_THAN_ONE_KEY_VALUE_PAIR_ERROR_MSG = "Only one key-value pair is allowed";
    private static final String DUPLICATE_KEY_ERROR_MSG = "Key [ %s ] already exists, duplicate keys not allowed";
    private static final String NON_EXISTENT_KEY_ERROR_MSG = "Key [ %s ] does not exist";
    private static final String MAX_KEY_FIELD_ERROR_MSG = format("Maximum key field length is [ %s ]", MAX_KEY_FIELD_LENGTH);
    private static final String MAX_VALUE_FILED_ERROR_MSG = format("Maximum value field length is [ %s ]", MAX_VALUE_FIELD_LENGTH);

    public Optional<Errors> validateCreateRequest(JsonNode payload, List<ProductMetadata> existingMetadataList) {
        if (existingMetadataList.size() >= MAX_NUMBER_OF_METADATA_ALLOWED) {
            return Optional.of(Errors.from(MAX_NUMBER_OF_METADATA_ALLOWED_ERROR_MSG, "MAX_METADATA_LENGTH_EXCEEDED"));
        }
        String key = payload.fieldNames().next();
        if (existingMetadataList
                .stream()
                .map(ProductMetadata::getKey)
                .map(String::toLowerCase)
                .anyMatch(isEqual(key.toLowerCase()))) {
            return Optional.of(Errors.from(format(DUPLICATE_KEY_ERROR_MSG, key),
                    "DUPLICATE_METADATA_KEYS"));
        }

        return Optional.empty();
    }

    public Optional<Errors> validateUpdateRequest(JsonNode payload, List<ProductMetadata> existingMetadataList) {
        String key = payload.fieldNames().next();
        if (!existingMetadataList
                .stream()
                .map(ProductMetadata::getKey)
                .map(String::toLowerCase)
                .anyMatch(isEqual(key.toLowerCase()))) {
            return Optional.of(Errors.from(format(NON_EXISTENT_KEY_ERROR_MSG, key)));
        }
        return Optional.empty();
    }

    public Optional<Errors> validateRequest(JsonNode payload) {
        if(payload.isEmpty()) {
            return Optional.of(Errors.from(EMPTY_PAYLOAD_ERROR_MSG));
        }
        if (payload.size() > 1) {
            return Optional.of(Errors.from(MORE_THAN_ONE_KEY_VALUE_PAIR_ERROR_MSG));
        }

        String key = payload.fieldNames().next();

        if (key.length() > MAX_KEY_FIELD_LENGTH) {
            return Optional.of(Errors.from(MAX_KEY_FIELD_ERROR_MSG, "KEY_LENGTH_OVER_MAX_SIZE"));
        }

        if (payload.get(key).asText().length() > MAX_VALUE_FIELD_LENGTH) {
            return Optional.of(Errors.from(MAX_VALUE_FILED_ERROR_MSG, "VALUE_LENGTH_OVER_MAX_SIZE"));
        }

        return Optional.empty();
    }
}
