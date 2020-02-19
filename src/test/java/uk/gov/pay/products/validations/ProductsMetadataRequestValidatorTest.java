package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.model.ProductMetadata;
import uk.gov.pay.products.util.Errors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ProductsMetadataRequestValidatorTest {

    private ProductsMetadataRequestValidator validator;

    @Before
    public void setUp() {
        validator = new ProductsMetadataRequestValidator();
    }

    @Test
    public void shouldErrorWhenExistingMetadataListSizeIs10() {
        List<ProductMetadata> metadataList = new ArrayList<>();
        for (int x = 0; x < 10; x++) {
            metadataList.add(new ProductMetadata(1, "" + x, "" + x));
        }
        JsonNode payload = new ObjectMapper()
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put("key", "value")
                                .build());
        Optional<Errors> errors = validator.validateCreateRequest(payload, metadataList);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Maximum number of allowed metadata [ 10 ] exceeded"), is(true));
    }

    @Test
    public void shouldErrorOnEmptyPayload() {
        JsonNode payload = new ObjectMapper()
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                        .build());
        Optional<Errors> errors = validator.validateRequest(payload);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Empty payload is not allowed"), is(true));
    }

    @Test
    public void shouldErrorOnMoreThanOneKeyValuePairs() {
        JsonNode payload = new ObjectMapper()
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put("key1", "value1")
                                .put("key2", "value2")
                                .build());
        Optional<Errors> errors = validator.validateRequest(payload);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Only one key-value pair is allowed"), is(true));
    }

    @Test
    public void shouldErrorOnDuplicateKey() {
        List<ProductMetadata> metadataList = List.of(new ProductMetadata(1, "Location", "london"),
                new ProductMetadata(1, "city", "London"));
        JsonNode payload = new ObjectMapper()
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put("location", "UK")
                                .build());
        Optional<Errors> errors = validator.validateCreateRequest(payload, metadataList);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Key [ location ] already exists, duplicate keys not allowed"), is(true));
    }

    @Test
    public void shouldErrorOnTooLongKeyField() {
        JsonNode payload = new ObjectMapper()
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put("1234567890123456789012345678901", "value")
                                .build());
        Optional<Errors> errors = validator.validateRequest(payload);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Maximum key field length is [ 30 ]"), is(true));
    }

    @Test
    public void shouldErrorOnTooLongValueField() {
        JsonNode payload = new ObjectMapper()
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put("key", "123456789012345678901234567890123456789012345678901")
                                .build());
        Optional<Errors> errors = validator.validateRequest(payload);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Maximum value field length is [ 50 ]"), is(true));
    }

    @Test
    public void shouldErrorOnNonExistentKeyField() {
        List<ProductMetadata> metadataList = List.of(new ProductMetadata(1, "Location", "london"));
        JsonNode payload = new ObjectMapper()
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put("country", "UK")
                                .build());
        Optional<Errors> errors = validator.validateUpdateRequest(payload, metadataList);
        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors().get(0).contains("Key [ country ] does not exist"), is(true));
    }
}