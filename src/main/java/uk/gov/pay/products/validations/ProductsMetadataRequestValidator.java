package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.pay.commons.model.charge.ExternalMetadata;
import uk.gov.pay.products.model.ProductMetadata;
import uk.gov.pay.products.util.Errors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.function.Predicate.isEqual;

public class ProductsMetadataRequestValidator {

    private static final String METADATA_FIELD_NAME = "metadata";
    private static final String MAX_NUMBER_OF_METADATA_ALLOWED_ERROR_MSG = format("Maximum number of allowed metadata [ %s ] exceeded",
            ExternalMetadata.MAX_KEY_VALUE_PAIRS);
    private static final String EMPTY_PAYLOAD_ERROR_MSG = "Empty payload is not allowed";
    private static final String MORE_THAN_ONE_KEY_VALUE_PAIR_ERROR_MSG = "Only one key-value pair is allowed";
    private static final String DUPLICATE_KEY_ERROR_MSG = "Key [ %s ] already exists, duplicate keys not allowed";
    private static final String NON_EXISTENT_KEY_ERROR_MSG = "Key [ %s ] does not exist";
    private static final String MAX_KEY_FIELD_ERROR_MSG = format("Maximum key field length is [ %s ]", ExternalMetadata.MAX_KEY_LENGTH);
    private static final String MAX_VALUE_FIELD_ERROR_MSG = format("Maximum value field length is [ %s ]", ExternalMetadata.MAX_VALUE_LENGTH);
    private static final String MAX_KEY_FIELD_ERROR_MSG_WITH_KEY = "Field [ metadata ] key [ %s ] exceeds allowed length of [ %s ] characters";
    private static final String MAX_VALUE_FIELD_ERROR_MSG_WITH_VALUE = "Field [ metadata ] value [ %s ] is over maximum field length allowed [ %s ]";
    private static final String EMPTY_METADATA_KEY_ERROR_MESSAGE = "Field [ metadata ] cannot be empty";
    private static final String METADATA_KEY_LENGTH_ERROR_MESSAGE = format("Field [ metadata ] key length must be between 1 and %s characters", ExternalMetadata.MAX_KEY_LENGTH);
    private static final String DUPLICATE_KEY_ERROR_MSG_CREATE_PRODUCT = "Field [ metadata ] with duplicate key [ %s ]";

    public Optional<Errors> validateCreateRequest(JsonNode payload, List<ProductMetadata> existingMetadataList) {
        if (existingMetadataList.size() >= ExternalMetadata.MAX_KEY_VALUE_PAIRS) {
            return Optional.of(Errors.from(MAX_NUMBER_OF_METADATA_ALLOWED_ERROR_MSG, "MAX_METADATA_LENGTH_EXCEEDED"));
        }

        String key = payload.fieldNames().next();
        if (existingMetadataList
                .stream()
                .map(ProductMetadata::getKey)
                .map(String::toLowerCase)
                .anyMatch(isEqual(key.toLowerCase(Locale.ENGLISH)))) {
            return Optional.of(Errors.from(format(DUPLICATE_KEY_ERROR_MSG, key),"DUPLICATE_METADATA_KEYS"));
        }

        return Optional.empty();
    }

    public Optional<Errors> validateUpdateRequest(JsonNode payload, List<ProductMetadata> existingMetadataList) {
        String key = payload.fieldNames().next();
        if (existingMetadataList
                .stream()
                .map(ProductMetadata::getKey)
                .map(String::toLowerCase)
                .noneMatch(isEqual(key.toLowerCase(Locale.ENGLISH)))) {
            return Optional.of(Errors.from(format(NON_EXISTENT_KEY_ERROR_MSG, key)));
        }
        return Optional.empty();
    }

    public Optional<Errors> validateRequest(JsonNode payload) {
        if (payload.isEmpty()) {
            return Optional.of(Errors.from(EMPTY_PAYLOAD_ERROR_MSG));
        }
        if (payload.size() > 1) {
            return Optional.of(Errors.from(MORE_THAN_ONE_KEY_VALUE_PAIR_ERROR_MSG));
        }

        String key = payload.fieldNames().next();

        if (key.length() > ExternalMetadata.MAX_KEY_LENGTH) {
            return Optional.of(Errors.from(MAX_KEY_FIELD_ERROR_MSG, "KEY_LENGTH_OVER_MAX_SIZE"));
        }

        if (payload.get(key).asText().length() > ExternalMetadata.MAX_VALUE_LENGTH) {
            return Optional.of(Errors.from(MAX_VALUE_FIELD_ERROR_MSG, "VALUE_LENGTH_OVER_MAX_SIZE"));
        }

        return Optional.empty();
    }

    public Optional<Errors> validateMetadata(JsonNode payload) {
        JsonNode metadataLoad = payload.get(METADATA_FIELD_NAME);
        if (metadataLoad == null) {
            return Optional.empty();
        }
        if (metadataLoad.isEmpty()) {
            return Optional.of(Errors.from(EMPTY_METADATA_KEY_ERROR_MESSAGE, "EMPTY_METADATA_KEY"));
        }

        if (metadataLoad.size() >= ExternalMetadata.MAX_KEY_VALUE_PAIRS) {
            return Optional.of(Errors.from(MAX_NUMBER_OF_METADATA_ALLOWED_ERROR_MSG, "MAX_METADATA_LENGTH_EXCEEDED"));
        }

        Iterator<String> fieldNames = metadataLoad.fieldNames();
        Set<String> duplicateFieldNames = new HashSet();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (fieldName.length() == 0) {
                return Optional.of(Errors.from(METADATA_KEY_LENGTH_ERROR_MESSAGE, "EMPTY_METADATA_KEY"));
            }
            if (fieldName.length() > ExternalMetadata.MAX_KEY_LENGTH) {
                return Optional.of(Errors.from(format(MAX_KEY_FIELD_ERROR_MSG_WITH_KEY, fieldName, ExternalMetadata.MAX_KEY_LENGTH), "METADATA_KEY_LENGTH_OVER_MAX_SIZE"));
            }
            String value = metadataLoad.get(fieldName).asText();
            if (value.length() > ExternalMetadata.MAX_VALUE_LENGTH) {
                return Optional.of(Errors.from(format(MAX_VALUE_FIELD_ERROR_MSG_WITH_VALUE, value, ExternalMetadata.MAX_VALUE_LENGTH), "METADATA_VALUE_LENGTH_MAX_SIZE"));
            }
            if (!duplicateFieldNames.add(fieldName.toLowerCase())) {
                return Optional.of(Errors.from(format(DUPLICATE_KEY_ERROR_MSG_CREATE_PRODUCT, fieldName), "DUPLICATE_METADATA_KEYS"));
            }
        }

        return Optional.empty();
    }
}
