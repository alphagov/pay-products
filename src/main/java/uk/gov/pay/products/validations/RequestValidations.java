package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;

import javax.json.Json;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.math.NumberUtils.isDigits;

public class RequestValidations {

    protected static final Long MAX_PRICE = 10000000L;

    public Optional<List<String>> checkIsNumeric(JsonNode payload, String... fieldNames) {
        return applyCheck(payload, isNotNumeric(), fieldNames, "Field [%s] must be a number");
    }

    public Optional<List<String>> checkIsUrl(JsonNode payload, String... fieldNames) {
        return applyCheck(payload, isNotUrl(), fieldNames, "Field [%s] must be a https url");
    }

    public Optional<List<String>> checkIfExists(JsonNode payload, String... fieldNames) {
        return applyCheck(payload, notExist(), fieldNames, "Field [%s] is required");
    }

    public Optional<List<String>> checkMaxLength(JsonNode payload, int maxLength, String... fieldNames) {
        return applyCheck(payload, exceedsMaxLength(maxLength), fieldNames, "Field [%s] must have a maximum length of " + maxLength + " characters");
    }

    private Function<JsonNode, Boolean> exceedsMaxLength(int maxLength) {
        return jsonNode -> jsonNode.asText().length() > maxLength;
    }

    public Optional<List<String>> applyCheck(JsonNode payload, Function<JsonNode, Boolean> check, String[] fieldNames, String errorMessage) {
        List<String> errors = newArrayList();
        for (String fieldName : fieldNames) {
            if (check.apply(payload.get(fieldName))) {
                errors.add(format(errorMessage, fieldName));
            }
        }
        return errors.size() > 0 ? Optional.of(errors) : Optional.empty();
    }

    public Function<JsonNode, Boolean> notExist() {
        return (jsonElement) -> {
            if (jsonElement instanceof ArrayNode) {
                return notExistOrEmptyArray().apply(jsonElement);
            } else {
                return notExistText().apply(jsonElement);
            }
        };
    }

    public Function<JsonNode, Boolean> notExistOrEmptyArray() {
        return jsonElement -> (
                jsonElement == null ||
                        ((jsonElement instanceof ArrayNode) && (jsonElement.size() == 0))
        );
    }

    private static Function<JsonNode, Boolean> notExistText() {
        return jsonElement -> (
                jsonElement == null ||
                        isBlank(jsonElement.asText())
        );
    }

    public static Function<JsonNode, Boolean> isNotNumeric() {
        return jsonNode -> !isDigits(jsonNode.asText());
    }

    public static Function<JsonNode, Boolean> isBelowMax() {
        return jsonNode -> isDigits(jsonNode.asText()) && jsonNode.asLong() >= MAX_PRICE;
    }

    public static Function<JsonNode, Boolean> isNotUrl() {
        return jsonNode -> {
            if(jsonNode == null || isBlank(jsonNode.asText()) || !jsonNode.asText().startsWith("https")) {
                return true;
            }

            try{
                new URL(jsonNode.asText());
            } catch (MalformedURLException e) {
                return true;
            }
            return false;
        };
    }

    public static Function<JsonNode, Boolean> isNotBoolean() {
        return jsonNode -> !ImmutableList.of("true", "false").contains(jsonNode.asText().toLowerCase());
    }

    public Optional<List<String>> checkIsBelowMaxAmount(JsonNode payload, String... fieldNames) {
        return applyCheck(payload, isBelowMax(), fieldNames, "Field [%s] must be a number below " + MAX_PRICE);
    }
}
