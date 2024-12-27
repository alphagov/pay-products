package uk.gov.pay.products.validations;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.pay.products.config.ProductsConfiguration;
import uk.gov.pay.products.util.Errors;
import uk.gov.pay.products.util.ProductType;
import uk.gov.service.payments.commons.api.exception.ValidationException;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.pay.products.validations.RequestValidations.MAX_PRICE;

public class ProductRequestValidatorTest {

    private static final String FIELD_GATEWAY_ACCOUNT_ID = "gateway_account_id";
    private static final String FIELD_PAY_API_TOKEN = "pay_api_token";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PRICE = "price";
    private static final String FIELD_SERVICE_NAME = "service_name";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_RETURN_URL = "return_url";
    private static final String VALID_RETURN_URL = "https://valid.url";
    private static final String FIELD_SERVICE_NAME_PATH = "service_name_path";
    private static final String FIELD_PRODUCT_NAME_PATH = "product_name_path";
    private static final String FIELD_REFERENCE_ENABLED = "reference_enabled";
    private static final String FIELD_REFERENCE_LABEL = "reference_label";
    private static final String FIELD_REFERENCE_HINT = "reference_hint";
    private static final String FIELD_LANGUAGE = "language";

    private static final ProductRequestValidator productRequestValidator = new ProductRequestValidator(new RequestValidations(), new ProductsConfiguration(), new ProductsMetadataRequestValidator());

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    public void shouldPass_whenAllFieldsPresent() {
        JsonNode payload = objectMapper
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                                .put(FIELD_PAY_API_TOKEN, "api_token")
                                .put(FIELD_NAME, "name")
                                .put(FIELD_PRICE, "25.00")
                                .put(FIELD_SERVICE_NAME, "Example service")
                                .put(FIELD_TYPE, ProductType.ADHOC.toString())
                                .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                                .put(FIELD_SERVICE_NAME_PATH, "service-name-path")
                                .put(FIELD_PRODUCT_NAME_PATH, "prodcut-name-path")
                                .put(FIELD_REFERENCE_ENABLED, Boolean.TRUE.toString())
                                .put(FIELD_REFERENCE_LABEL, "A reference label")
                                .put(FIELD_REFERENCE_HINT, "A hint")
                                .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(false));
    }

    @Test
    public void shouldPass_whenReturnUrlFieldIsMissing() {
        JsonNode payload = objectMapper
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                                .put(FIELD_PAY_API_TOKEN, "api_token")
                                .put(FIELD_NAME, "name")
                                .put(FIELD_SERVICE_NAME, "Example service")
                                .put(FIELD_PRICE, "25.00")
                                .put(FIELD_TYPE, ProductType.DEMO.name())
                                .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                                .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(false));
    }

    @Test
    public void shouldPass_whenPriceIsBelowMaxPrice() {
        JsonNode payload = objectMapper
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                                .put(FIELD_PAY_API_TOKEN, "api_token")
                                .put(FIELD_NAME, "name")
                                .put(FIELD_SERVICE_NAME, "Example service")
                                .put(FIELD_PRICE, String.valueOf(MAX_PRICE - 1))
                                .put(FIELD_TYPE, ProductType.DEMO.toString())
                                .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                                .build());


        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(false));
    }

    @Test
    public void shouldError_whenPriceFieldIsMissing_forNonAdhocAndNonAgentIntiatedMotoProduct() {
        JsonNode payload = objectMapper
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                        .put(FIELD_PAY_API_TOKEN, "api_token")
                        .put(FIELD_NAME, "name")
                        .put(FIELD_SERVICE_NAME, "Example service")
                        .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                        .put(FIELD_TYPE, ProductType.DEMO.toString())
                        .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                        .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [price] is required]"));
    }

    @Test
    public void shouldNotError_whenPriceFieldIsMissing_forAdhocProduct() {
        JsonNode payload = objectMapper
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                        .put(FIELD_PAY_API_TOKEN, "api_token")
                        .put(FIELD_NAME, "name")
                        .put(FIELD_SERVICE_NAME, "Example service")
                        .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                        .put(FIELD_TYPE, ProductType.ADHOC.toString())
                        .put(FIELD_SERVICE_NAME_PATH, "service-name-path")
                        .put(FIELD_PRODUCT_NAME_PATH, "product-name-path")
                        .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                        .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(false));
    }

    @Test
    public void shouldNotError_whenPriceFieldServiceNamePathAndProductNamePathAreMissing_forAgentInitiatedMotoProduct() {
        JsonNode payload = objectMapper
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                        .put(FIELD_PAY_API_TOKEN, "api_token")
                        .put(FIELD_NAME, "name")
                        .put(FIELD_SERVICE_NAME, "Example service")
                        .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                        .put(FIELD_TYPE, ProductType.AGENT_INITIATED_MOTO.toString())
                        .put(FIELD_REFERENCE_ENABLED, Boolean.TRUE.toString())
                        .put(FIELD_REFERENCE_LABEL, "A reference label")
                        .put(FIELD_REFERENCE_HINT, "A hint")
                        .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(false));
    }
    
    @Test
    public void shouldError_whenPriceFieldExceedsMaxPrice() {
        JsonNode payload = objectMapper
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                                .put(FIELD_PAY_API_TOKEN, "api_token")
                                .put(FIELD_NAME, "name")
                                .put(FIELD_PRICE, String.valueOf(MAX_PRICE + 1))
                                .put(FIELD_SERVICE_NAME, "Example service")
                                .put(FIELD_TYPE, ProductType.DEMO.toString())
                                .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                                .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                                .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [price] must be a number below " + MAX_PRICE + "]"));
    }

    @Test
    public void shouldError_whenNameFieldIsMissing() {
        JsonNode payload = objectMapper
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                        .put(FIELD_PAY_API_TOKEN, "api_token")
                        .put(FIELD_PRICE, "25.00")
                        .put(FIELD_SERVICE_NAME, "Example service")
                        .put(FIELD_TYPE, ProductType.DEMO.toString())
                        .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                        .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                        .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [name] is required]"));
    }

    @Test
    public void shouldError_whenApiTokenFieldIsMissing() {
        JsonNode payload = objectMapper
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                        .put(FIELD_NAME, "name")
                        .put(FIELD_PRICE, "25.00")
                        .put(FIELD_SERVICE_NAME, "Example service")
                        .put(FIELD_TYPE, ProductType.DEMO.toString())
                        .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                        .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                        .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [pay_api_token] is required]"));
    }

    @Test
    public void shouldError_whenGatewayAccountIdFieldIsMissing() {
        JsonNode payload = objectMapper
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_PAY_API_TOKEN, "api_token")
                        .put(FIELD_NAME, "name")
                        .put(FIELD_SERVICE_NAME, "Example service")
                        .put(FIELD_PRICE, "25.00")
                        .put(FIELD_TYPE, ProductType.DEMO.toString())
                        .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                        .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                        .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [gateway_account_id] is required]"));
    }

    @Test
    public void shouldError_whenTypeIsMissing() {
        JsonNode payload = objectMapper
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                        .put(FIELD_PAY_API_TOKEN, "api_token")
                        .put(FIELD_NAME, "name")
                        .put(FIELD_PRICE, "25.00")
                        .put(FIELD_SERVICE_NAME, "Example service")
                        .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                        .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                        .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [type] is required]"));
    }

    @Test
    public void shouldError_whenTypeIsUnknown() {
        JsonNode payload = objectMapper
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                                .put(FIELD_PAY_API_TOKEN, "api_token")
                                .put(FIELD_NAME, "name")
                                .put(FIELD_PRICE, "25.0")
                                .put(FIELD_SERVICE_NAME, "Example service")
                                .put(FIELD_TYPE, "UNKNOWN")
                                .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                                .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                                .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [type] must be one of [DEMO, PROTOTYPE, ADHOC, AGENT_INITIATED_MOTO]]"));
    }

    @Test
    public void shouldError_whenReturnUrlIsInvalid() {
        JsonNode payload = objectMapper
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                                .put(FIELD_PAY_API_TOKEN, "api_token")
                                .put(FIELD_NAME, "name")
                                .put(FIELD_PRICE, "25.0")
                                .put(FIELD_SERVICE_NAME, "Example service")
                                .put(FIELD_TYPE, ProductType.DEMO.toString())
                                .put(FIELD_RETURN_URL, "return_url")
                                .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                                .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);


        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [return_url] must be a https URL]"));
    }

    @Test
    public void shouldError_whenReturnUrlIsNotHttps() {
        JsonNode payload = objectMapper
                .valueToTree(

                        ImmutableMap.<String, String>builder()
                                .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                                .put(FIELD_PAY_API_TOKEN, "api_token")
                                .put(FIELD_NAME, "name")
                                .put(FIELD_PRICE, "25.0")
                                .put(FIELD_SERVICE_NAME, randomAlphanumeric(50))
                                .put(FIELD_TYPE, ProductType.DEMO.toString())
                                .put(FIELD_RETURN_URL, "http://return.url")
                                .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                                .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);


        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [return_url] must be a https URL]"));
    }

    @Test
    public void shouldError_whenTypeIsAdhocAndNoProductPathIsPresent() {
        JsonNode payload = objectMapper
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                        .put(FIELD_PAY_API_TOKEN, "api_token")
                        .put(FIELD_NAME, "name")
                        .put(FIELD_PRICE, "25.0")
                        .put(FIELD_SERVICE_NAME, randomAlphanumeric(50))
                        .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                        .put(FIELD_TYPE, ProductType.ADHOC.toString())
                        .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                        .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [service_name_path] is required, Field [product_name_path] is required]"));
    }

    @Test
    public void shouldError_whenTypeIsAdhocAndProductNamePathIsEmpty() {
        JsonNode payload = objectMapper
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                        .put(FIELD_PAY_API_TOKEN, "api_token")
                        .put(FIELD_NAME, "name")
                        .put(FIELD_PRICE, "25.0")
                        .put(FIELD_SERVICE_NAME, randomAlphanumeric(50))
                        .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                        .put(FIELD_TYPE, ProductType.ADHOC.toString())
                        .put(FIELD_SERVICE_NAME_PATH, "service-name-path")
                        .put(FIELD_PRODUCT_NAME_PATH, "")
                        .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                        .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [product_name_path] is required]"));
    }

    @Test
    public void shouldError_whenReferenceEnabledIsTrueAndReferenceLabelIsEmpty() {
        JsonNode payload = objectMapper
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                        .put(FIELD_PAY_API_TOKEN, "api_token")
                        .put(FIELD_NAME, "name")
                        .put(FIELD_PRICE, "25.0")
                        .put(FIELD_SERVICE_NAME, randomAlphanumeric(50))
                        .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                        .put(FIELD_TYPE, ProductType.ADHOC.toString())
                        .put(FIELD_SERVICE_NAME_PATH, "service-name-path")
                        .put(FIELD_PRODUCT_NAME_PATH, "product-name-path")
                        .put(FIELD_REFERENCE_ENABLED, Boolean.TRUE.toString())
                        .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [reference_label] is required]"));
    }

    @Test
    public void shouldError_whenLanguageIsPresentAndEmpty() {
        JsonNode payload = objectMapper
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                        .put(FIELD_PAY_API_TOKEN, "api_token")
                        .put(FIELD_NAME, "name")
                        .put(FIELD_PRICE, "25.0")
                        .put(FIELD_SERVICE_NAME, randomAlphanumeric(50))
                        .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                        .put(FIELD_TYPE, ProductType.ADHOC.toString())
                        .put(FIELD_SERVICE_NAME_PATH, "service-name-path")
                        .put(FIELD_PRODUCT_NAME_PATH, "product-name-path")
                        .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                        .put(FIELD_LANGUAGE, "")
                        .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [language] must be one of [en, cy]]"));
    }

    @Test
    public void shouldError_whenLanguageIsPresentAndNotSupportedLanguage() {
        JsonNode payload = objectMapper
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                        .put(FIELD_PAY_API_TOKEN, "api_token")
                        .put(FIELD_NAME, "name")
                        .put(FIELD_PRICE, "25.0")
                        .put(FIELD_SERVICE_NAME, randomAlphanumeric(50))
                        .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                        .put(FIELD_TYPE, ProductType.ADHOC.toString())
                        .put(FIELD_SERVICE_NAME_PATH, "service-name-path")
                        .put(FIELD_PRODUCT_NAME_PATH, "product-name-path")
                        .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                        .put(FIELD_LANGUAGE, "ie")
                        .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [language] must be one of [en, cy]]"));
    }

    @Test
    public void shouldNotError_whenLanguageIsNotPresent() {
        JsonNode payload = objectMapper
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                        .put(FIELD_PAY_API_TOKEN, "api_token")
                        .put(FIELD_NAME, "name")
                        .put(FIELD_PRICE, "25.0")
                        .put(FIELD_SERVICE_NAME, randomAlphanumeric(50))
                        .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                        .put(FIELD_TYPE, ProductType.ADHOC.toString())
                        .put(FIELD_SERVICE_NAME_PATH, "service-name-path")
                        .put(FIELD_PRODUCT_NAME_PATH, "product-name-path")
                        .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                        .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(false));
    }

    @Test
    public void shouldNotError_whenLanguageIsSupportedLanguage() {
        JsonNode payload = objectMapper
                .valueToTree(ImmutableMap.<String, String>builder()
                        .put(FIELD_GATEWAY_ACCOUNT_ID, "1")
                        .put(FIELD_PAY_API_TOKEN, "api_token")
                        .put(FIELD_NAME, "name")
                        .put(FIELD_PRICE, "25.0")
                        .put(FIELD_SERVICE_NAME, randomAlphanumeric(50))
                        .put(FIELD_RETURN_URL, VALID_RETURN_URL)
                        .put(FIELD_TYPE, ProductType.ADHOC.toString())
                        .put(FIELD_SERVICE_NAME_PATH, "service-name-path")
                        .put(FIELD_PRODUCT_NAME_PATH, "product-name-path")
                        .put(FIELD_REFERENCE_ENABLED, Boolean.FALSE.toString())
                        .put(FIELD_LANGUAGE, "cy")
                        .build());

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(false));
    }

    @Test
    public void shouldError_whenTypeIsInvalid() {
        Optional<Errors> errors = productRequestValidator.validateProductType("THIS_IS_NOT_A_PRODUCT_TYPE");

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [type] must be one of [DEMO, PROTOTYPE, ADHOC, AGENT_INITIATED_MOTO]]"));
    }

    @Test
    public void shouldThrowWhenPatchRequestInvalid() {
        JsonNode request = objectMapper.valueToTree(
                Collections.singletonList(Map.of("path", "require_captcha",
                        "op", "add",
                        "value", true)));
        var thrown = assertThrows(ValidationException.class, () -> productRequestValidator.validateJsonPatch(request));
        assertThat(thrown.getErrors().get(0), is("Operation [add] not supported for path [require_captcha]"));
    }

    @Test
    public void shouldThrowWhenRequireCaptchaNotBoolean() {
        JsonNode request = objectMapper.valueToTree(
                Collections.singletonList(Map.of("path", "require_captcha",
                        "op", "replace",
                        "value", "not-boolean")));
        var thrown = assertThrows(ValidationException.class, () -> productRequestValidator.validateJsonPatch(request));
        assertThat(thrown.getErrors().get(0), is("Value for path [require_captcha] must be a boolean"));
    }

    @Test
    public void shouldNotThrowWhenPatchRequestValid() {
        JsonNode request = objectMapper.valueToTree(
                Collections.singletonList(Map.of("path", "require_captcha",
                        "op", "replace",
                        "value", true)));
        assertDoesNotThrow(() -> productRequestValidator.validateJsonPatch(request));
    }
}
