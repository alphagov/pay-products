package uk.gov.pay.products.validations;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.pay.products.util.Errors;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.pay.products.validations.RequestValidations.MAX_PRICE;

public class ProductRequestValidatorTest {

    private static final String FIELD_GATEWAY_ACCOUNT_ID = "gateway_account_id";
    private static final String FIELD_PAY_API_TOKEN = "pay_api_token";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PRICE = "price";
    private static final String FIELD_SERVICE_NAME = "service_name";
    private static final String RETURN_URL = "return_url";
    private static final String VALID_RETURN_URL = "https://valid.url";

    private static ProductRequestValidator productRequestValidator = new ProductRequestValidator(new RequestValidations());

    @Test
    public void shouldPass_whenAllFieldsPresent(){
        ImmutableMap<Object, Object> map = ImmutableMap.builder()
                .put(FIELD_GATEWAY_ACCOUNT_ID, 1)
                .put(FIELD_PAY_API_TOKEN, "api_token")
                .put(FIELD_NAME, "name")
                .put(FIELD_PRICE, 25.00)
                .put(FIELD_SERVICE_NAME, "Example service")
                .put(RETURN_URL, VALID_RETURN_URL)
                .build();


        JsonNode payload = new ObjectMapper()
                .valueToTree(map);

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(false));
    }

    @Test
    public void shouldPass_whenReturnUrlFieldIsMissing(){
        JsonNode payload = new ObjectMapper()
                .valueToTree(ImmutableMap.of(
                        FIELD_GATEWAY_ACCOUNT_ID, 1,
                        FIELD_PAY_API_TOKEN, "api_token",
                        FIELD_NAME, "name",
                        FIELD_SERVICE_NAME, "Example service",
                        FIELD_PRICE, 25.00));

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(false));
    }

    @Test
    public void shouldPass_whenPriceIsBelowMaxPrice(){
        JsonNode payload = new ObjectMapper()
                .valueToTree(ImmutableMap.of(
                        FIELD_GATEWAY_ACCOUNT_ID, 1,
                        FIELD_PAY_API_TOKEN, "api_token",
                        FIELD_NAME, "name",
                        FIELD_SERVICE_NAME, "Example service",
                        FIELD_PRICE, MAX_PRICE - 1L));

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(false));
    }

    @Test
    public void shouldError_whenPriceFieldIsMissing(){
        JsonNode payload = new ObjectMapper()
                .valueToTree(ImmutableMap.of(
                        FIELD_GATEWAY_ACCOUNT_ID, 1,
                        FIELD_PAY_API_TOKEN, "api_token",
                        FIELD_NAME, "name",
                        FIELD_SERVICE_NAME, "Example service",
                        RETURN_URL, VALID_RETURN_URL));

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [price] is required]"));
    }

    @Test
    public void shouldError_whenPriceFieldEqualsMaxPrice(){
        ImmutableMap<Object, Object> map = ImmutableMap.builder()
                .put(FIELD_GATEWAY_ACCOUNT_ID, 1)
                .put(FIELD_PAY_API_TOKEN, "api_token")
                .put(FIELD_NAME, "name")
                .put(FIELD_PRICE, MAX_PRICE)
                .put(FIELD_SERVICE_NAME, "Example service")
                .put(RETURN_URL, VALID_RETURN_URL)
                .build();

        JsonNode payload = new ObjectMapper()
                .valueToTree(map);

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [price] must be a number below " + MAX_PRICE + "]"));
    }

    @Test
    public void shouldError_whenPriceFieldExceedsMaxPrice(){
        ImmutableMap<Object, Object> map = ImmutableMap.builder()
                .put(FIELD_GATEWAY_ACCOUNT_ID, 1)
                .put(FIELD_PAY_API_TOKEN, "api_token")
                .put(FIELD_NAME, "name")
                .put(FIELD_PRICE, MAX_PRICE + 1)
                .put(FIELD_SERVICE_NAME, "Example service")
                .put(RETURN_URL, VALID_RETURN_URL)
                .build();

        JsonNode payload = new ObjectMapper()
                .valueToTree(map);

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [price] must be a number below " + MAX_PRICE + "]"));
    }

    @Test
    public void shouldError_whenNameFieldIsMissing(){
        JsonNode payload = new ObjectMapper()
                .valueToTree(ImmutableMap.of(
                        FIELD_GATEWAY_ACCOUNT_ID, 1,
                        FIELD_PAY_API_TOKEN, "api_token",
                        FIELD_PRICE, 25.00,
                        FIELD_SERVICE_NAME, "Example service",
                        RETURN_URL, VALID_RETURN_URL));

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [name] is required]"));
    }

    @Test
    public void shouldError_whenApiTokenFieldIsMissing(){
        JsonNode payload = new ObjectMapper()
                .valueToTree(ImmutableMap.of(
                        FIELD_GATEWAY_ACCOUNT_ID, 1,
                        FIELD_NAME, "name",
                        FIELD_PRICE, 25.00,
                        FIELD_SERVICE_NAME, "Example service",
                        RETURN_URL, VALID_RETURN_URL));

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [pay_api_token] is required]"));
    }

    @Test
    public void shouldError_whenGatewayAccountIdFieldIsMissing(){
        JsonNode payload = new ObjectMapper()
                .valueToTree(ImmutableMap.of(
                        FIELD_PAY_API_TOKEN, "api_token",
                        FIELD_NAME, "name",
                        FIELD_SERVICE_NAME, "Example service",
                        FIELD_PRICE, 25.00,
                        RETURN_URL, VALID_RETURN_URL));

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [gateway_account_id] is required]"));
    }

    @Test
    public void shouldError_whenServiceNameFieldIsMissing(){
        ImmutableMap<Object, Object> map = ImmutableMap.builder()
                .put(FIELD_GATEWAY_ACCOUNT_ID, 1)
                .put(FIELD_PAY_API_TOKEN, "api_token")
                .put(FIELD_NAME, "name")
                .put(FIELD_PRICE, 25.00)
                .put(RETURN_URL, VALID_RETURN_URL)
                .build();


        JsonNode payload = new ObjectMapper()
                .valueToTree(map);

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);

        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [service_name] is required]"));
    }

    @Test
    public void shouldError_whenReturnUrlIsInvalid(){
        ImmutableMap<Object, Object> map = ImmutableMap.builder()
                .put(FIELD_GATEWAY_ACCOUNT_ID, 1)
                .put(FIELD_PAY_API_TOKEN, "api_token")
                .put(FIELD_NAME, "name")
                .put(FIELD_PRICE, MAX_PRICE + 1)
                .put(FIELD_SERVICE_NAME, "Example service")
                .put(RETURN_URL, "return-url")
                .build();

        JsonNode payload = new ObjectMapper()
                .valueToTree(map);

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);



        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [return_url] must be a https url]"));
    }

    @Test
    public void shouldError_whenReturnUrlIsNotHttps(){
        ImmutableMap<Object, Object> map = ImmutableMap.builder()
                .put(FIELD_GATEWAY_ACCOUNT_ID, 1)
                .put(FIELD_PAY_API_TOKEN, "api_token")
                .put(FIELD_NAME, "name")
                .put(FIELD_PRICE, MAX_PRICE + 1)
                .put(FIELD_SERVICE_NAME, "Example service")
                .put(RETURN_URL, "http://return.url")
                .build();

        JsonNode payload = new ObjectMapper()
                .valueToTree(map);

        Optional<Errors> errors = productRequestValidator.validateCreateRequest(payload);



        assertThat(errors.isPresent(), is(true));
        assertThat(errors.get().getErrors().toString(), is("[Field [return_url] must be a https url]"));
    }
}
