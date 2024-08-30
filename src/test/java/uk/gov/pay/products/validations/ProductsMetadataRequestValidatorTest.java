package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.model.ProductMetadata;
import uk.gov.pay.products.util.Errors;
import uk.gov.service.payments.commons.model.charge.ExternalMetadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ProductsMetadataRequestValidatorTest {

    private static final  String TOO_LONG_KEY = IntStream.rangeClosed(1, ExternalMetadata.MAX_KEY_LENGTH + 1).mapToObj(i -> "k").collect(joining());
    private static final String TOO_LONG_VALUE = IntStream.rangeClosed(1, ExternalMetadata.MAX_VALUE_LENGTH + 1).mapToObj(i -> "v").collect(joining());

    private ProductsMetadataRequestValidator validator;

    @Before
    public void setUp() {
        validator = new ProductsMetadataRequestValidator();
    }

    @Test
    public void shouldNotErrorOnMissingMetadataObjectForProductCreate() {
        JsonNode payload = new ObjectMapper().valueToTree(Map.of());
        Optional<Errors> errors = validator.validateMetadata(payload);
        assertThat(errors.isPresent(), is(false));
    }

    @Test
    public void shouldErrorOnTooManyKeyValuePairsForProductCreate() {
        Map<String, String> mapToJson = new HashMap<>();
        for (int count = 1; count < 17; count++) {
            mapToJson.put("key" + count, "value" + count);
        }
        JsonNode payload = new ObjectMapper().valueToTree(Map.of("metadata", mapToJson));
        Optional<Errors> errors = validator.validateMetadata(payload);
        assertThat(errors.isPresent(), is(true));
        List<String> errorList = errors.get().getErrors();
        assertThat(errorList.size(), is(1));
        assertThat(errorList.get(0), is("Maximum number of allowed metadata [ " + ExternalMetadata.MAX_KEY_VALUE_PAIRS + " ] exceeded"));
        JsonNode errorNode = new ObjectMapper().valueToTree(errors.get());
        assertThat(errorNode.get("error_identifier").asText(), is("MAX_METADATA_LENGTH_EXCEEDED"));
    }

    @Test
    public void shouldErrorOnTooLongKeyFieldForProductCreate() {
        JsonNode payload = new ObjectMapper().valueToTree(Map.of("metadata", Map.of(TOO_LONG_KEY, "value")));
        Optional<Errors> errors = validator.validateMetadata(payload);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0), is("Field [ metadata ] key [ " + TOO_LONG_KEY + " ] exceeds allowed length of [ 30 ] characters"));
        JsonNode errorNode = new ObjectMapper().valueToTree(errors.get());
        assertThat(errorNode.get("error_identifier").asText(), is("METADATA_KEY_LENGTH_OVER_MAX_SIZE"));
    }

    @Test
    public void shouldErrorOnDuplicateKeysForProductCreate() {
        JsonNode payload = new ObjectMapper().valueToTree(Map.of("metadata", Map.of("key", "value", "Key", "anotherValue")));
        Optional<Errors> errors = validator.validateMetadata(payload);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Field [ metadata ] with duplicate key"), is(true));
        JsonNode errorNode = new ObjectMapper().valueToTree(errors.get());
        assertThat(errorNode.get("error_identifier").asText(), is("DUPLICATE_METADATA_KEYS"));
    }

    @Test
    public void shouldErrorOnTooLongValueFieldForProductCreate() {
        JsonNode payload = new ObjectMapper().valueToTree(Map.of("metadata", Map.of("key", TOO_LONG_VALUE)));
        Optional<Errors> errors = validator.validateMetadata(payload);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0), is("Field [ metadata ] value [ " + TOO_LONG_VALUE + " ] is over maximum field length allowed [ " + ExternalMetadata.MAX_VALUE_LENGTH + " ]"));
        JsonNode errorNode = new ObjectMapper().valueToTree(errors.get());
        assertThat(errorNode.get("error_identifier").asText(), is("METADATA_VALUE_LENGTH_MAX_SIZE"));
    }

    @Test
    public void shouldErrorOnEmptyKeyForProductCreate() {
        JsonNode payload = new ObjectMapper().valueToTree(Map.of("metadata", Map.of("", TOO_LONG_VALUE)));
        Optional<Errors> errors = validator.validateMetadata(payload);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0), is("Field [ metadata ] key length must be between 1 and 30 characters"));
        JsonNode errorNode = new ObjectMapper().valueToTree(errors.get());
        assertThat(errorNode.get("error_identifier").asText(), is("EMPTY_METADATA_KEY"));
    }
}
