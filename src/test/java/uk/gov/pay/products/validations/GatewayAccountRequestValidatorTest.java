package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.pay.products.util.Errors;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GatewayAccountRequestValidatorTest {

    private static final String FIELD_OP = "op";
    private static final String FIELD_PATH = "path";
    private static final String FIELD_VALUE = "value";

    private final GatewayAccountRequestValidator requestValidator = new GatewayAccountRequestValidator(new RequestValidations());

    @Test
    public void shouldPass_whenAllFieldsPresent() {
        JsonNode payload = new ObjectMapper()
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put(FIELD_OP, "replace")
                                .put(FIELD_PATH, "service_name")
                                .put(FIELD_VALUE, "A New Name")
                                .build());

        Optional<Errors> errors = requestValidator.validatePatchRequest(payload);

        assertThat(errors.isPresent(), is(false));
    }

    @Test
    public void shouldError_whenPathIsInvalid() {
        JsonNode payload = new ObjectMapper()
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_OP, "replace")
                        .put(FIELD_PATH, "gateway_id")
                        .put(FIELD_VALUE, "A New Name")
                        .build());

        Optional<Errors> errors = requestValidator.validatePatchRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Path gateway_id not supported / invalid]"));
    }

    @Test
    public void shouldError_whenValueFieldIsMissing() {
        JsonNode payload = new ObjectMapper()
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_OP, "replace")
                        .put(FIELD_PATH, "service_name")
                        .build());

        Optional<Errors> errors = requestValidator.validatePatchRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [value] is required]"));
    }

    @Test
    public void shouldError_whenOperationFieldIsMissing() {
        JsonNode payload = new ObjectMapper()
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_PATH, "service_name")
                        .put(FIELD_VALUE, "A New Name")
                        .build());

        Optional<Errors> errors = requestValidator.validatePatchRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [op] is required]"));
    }

    @Test
    public void shouldError_whenPathFieldIsMissing() {
        JsonNode payload = new ObjectMapper()
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_OP, "replace")
                        .put(FIELD_VALUE, "service_name")
                        .build());

        Optional<Errors> errors = requestValidator.validatePatchRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [path] is required]"));
    }

    @Test
    public void shouldError_whenEmptyFields() {
        JsonNode payload = new ObjectMapper()
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put(FIELD_OP, "replace")
                                .put(FIELD_PATH, "")
                                .put(FIELD_VALUE, "")
                                .build());

        Optional<Errors> errors = requestValidator.validatePatchRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [path] is required, Field [value] is required]"));
    }

    @Test
    public void shouldError_whenInvalidOperation() {
        JsonNode payload = new ObjectMapper()
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put(FIELD_OP, "update")
                                .put(FIELD_PATH, "gateway_id")
                                .put(FIELD_VALUE, "A New Name")
                                .build());

        Optional<Errors> errors = requestValidator.validatePatchRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Path gateway_id not supported / invalid]"));
    }
}
