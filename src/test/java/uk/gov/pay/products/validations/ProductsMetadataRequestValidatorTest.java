package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.commons.model.charge.ExternalMetadata;
import uk.gov.pay.products.model.ProductMetadata;
import uk.gov.pay.products.util.Errors;

import java.util.Collections;
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
    public void shouldErrorWhenExistingMetadataListSizeIsAlreadyAtMaxNumberOfKeyValuePairs() {
        List<ProductMetadata> metadataListWithMaxNumberOfKeyValuePairs = IntStream.rangeClosed(1, ExternalMetadata.MAX_KEY_VALUE_PAIRS)
                .mapToObj(i -> new ProductMetadata(1, "key " + i, "value " + i))
                .collect(toUnmodifiableList());
        JsonNode payload = new ObjectMapper().valueToTree(Map.of("key", "value"));
        Optional<Errors> errors = validator.validateCreateRequest(payload, metadataListWithMaxNumberOfKeyValuePairs);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Maximum number of allowed metadata [ " + ExternalMetadata.MAX_KEY_VALUE_PAIRS + " ] exceeded"),
                is(true));
    }

    @Test
    public void shouldErrorOnEmptyPayload() {
        JsonNode payload = new ObjectMapper().valueToTree(Collections.emptyMap());
        Optional<Errors> errors = validator.validateRequest(payload);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Empty payload is not allowed"), is(true));
    }

    @Test
    public void shouldErrorOnMoreThanOneKeyValuePairs() {
        JsonNode payload = new ObjectMapper().valueToTree(Map.of("key1", "value1", "key2", "value2"));
        Optional<Errors> errors = validator.validateRequest(payload);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Only one key-value pair is allowed"), is(true));
    }

    @Test
    public void shouldErrorOnDuplicateKey() {
        List<ProductMetadata> metadataList = List.of(new ProductMetadata(1, "Location", "london"),
                new ProductMetadata(1, "city", "London"));
        JsonNode payload = new ObjectMapper().valueToTree(Map.of("location", "UK"));
        Optional<Errors> errors = validator.validateCreateRequest(payload, metadataList);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Key [ location ] already exists, duplicate keys not allowed"), is(true));
    }

    @Test
    public void shouldErrorOnTooLongKeyField() {
        JsonNode payload = new ObjectMapper().valueToTree(Map.of(TOO_LONG_KEY, "value"));
        Optional<Errors> errors = validator.validateRequest(payload);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Maximum key field length is [ " + ExternalMetadata.MAX_KEY_LENGTH + " ]"), is(true));
    }

    @Test
    public void shouldErrorOnTooLongValueField() {
        JsonNode payload = new ObjectMapper().valueToTree(Map.of("key", TOO_LONG_VALUE));
        Optional<Errors> errors = validator.validateRequest(payload);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Maximum value field length is [ " + ExternalMetadata.MAX_VALUE_LENGTH + " ]"), is(true));
    }

    @Test
    public void shouldErrorOnNonExistentKeyField() {
        List<ProductMetadata> metadataList = List.of(new ProductMetadata(1, "Location", "london"));
        JsonNode payload = new ObjectMapper().valueToTree(Map.of("country", "UK"));
        Optional<Errors> errors = validator.validateUpdateRequest(payload, metadataList);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Key [ country ] does not exist"), is(true));
    }

}
