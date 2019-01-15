package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.pay.products.util.Errors;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;

public class PaymentRequestValidator {

    private static final String FIELD_PRICE = "price";

    private final RequestValidations requestValidations;

    @Inject
    public PaymentRequestValidator(RequestValidations requestValidations) {
        this.requestValidations = requestValidations;
    }

    public Optional<Errors> validatePriceOverrideRequest(JsonNode payload) {
        if (payload == null || payload.isNull() || newArrayList(payload.fieldNames()).isEmpty()) {
            return Optional.empty();
        }
        Optional<List<String>> errors = requestValidations.checkIfExistsOrEmpty(payload, FIELD_PRICE);
        if (errors.isPresent()) {
            return Optional.of(Errors.from(errors.get()));
        }
        errors = requestValidations.checkIsNumeric(payload, FIELD_PRICE);
        if (errors.isPresent()) {
            return Optional.of(Errors.from(errors.get()));
        }
        errors = requestValidations.checkIsBelowMaxAmount(payload, FIELD_PRICE);
        return errors.map(Errors::from);
    }
}
