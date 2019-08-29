package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.util.Errors;

import java.net.URL;
import java.util.Optional;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PaymentRequestValidatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PaymentRequestValidator requestValidator;

    @Before
    public void before() {
        requestValidator = new PaymentRequestValidator(new RequestValidations());
    }

    @Test
    public void shouldSuccess_onCreatePayment_ifJsonPayloadIsNull() throws Exception {
        Optional<Errors> errors = requestValidator.validatePriceOverrideRequest(null);
        assertFalse(errors.isPresent());
    }

    @Test
    public void shouldSuccess_onCreatePayment_ifJsonPayloadIsEmpty() throws Exception {
        Optional<Errors> errors = requestValidator.validatePriceOverrideRequest(objectMapper.readTree("{}"));
        assertFalse(errors.isPresent());
    }

    @Test
    public void shouldSuccess_onCreatePayment_ifJsonPayloadHasValidPrice() {
        Optional<Errors> errors = requestValidator.validatePriceOverrideRequest(objectMapper.valueToTree(ImmutableMap.of("price", "1900")));
        assertFalse(errors.isPresent());
    }

    @Test
    public void shouldError_onCreatePayment_ifJsonPayloadHasInvalidField() {
        Optional<Errors> errors = requestValidator.validatePriceOverrideRequest(objectMapper.valueToTree(ImmutableMap.of("blah", "1900")));
        assertTrue(errors.isPresent());

        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors(), hasItem("Field [price] is required"));
    }

    @Test
    public void shouldError_onCreatePayment_ifJsonPayloadHasNonNumeric() {
        Optional<Errors> errors = requestValidator.validatePriceOverrideRequest(objectMapper.valueToTree(ImmutableMap.of("price", "blah")));
        assertTrue(errors.isPresent());

        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors(), hasItem("Field [price] must be a number"));
    }

    @Test
    public void shouldError_onCreatePayment_ifJsonPayloadBiggerThanMaxAmount() {
        Optional<Errors> errors = requestValidator.validatePriceOverrideRequest(objectMapper.valueToTree(ImmutableMap.of("price", "100000001")));
        assertTrue(errors.isPresent());

        assertThat(errors.get().getErrors().size(), is(1));
        assertThat(errors.get().getErrors(), hasItem("Field [price] must be a number below 10000000"));
    }
}
