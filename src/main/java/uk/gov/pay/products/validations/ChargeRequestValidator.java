package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import uk.gov.pay.products.util.Errors;

import java.util.List;
import java.util.Optional;

import static uk.gov.pay.products.util.ChargeJsonField.PRODUCT_EXTERNAL_ID;

public class ChargeRequestValidator {
    private final RequestValidations requestValidations;

    @Inject
    public ChargeRequestValidator(RequestValidations requestValidations) {
        this.requestValidations = requestValidations;
    }

    public Optional<Errors> validateCreateRequest(JsonNode payload){
        Optional<List<String>> errors = requestValidations.checkIfExists(
                payload,
                PRODUCT_EXTERNAL_ID
        );

        return errors.map(Errors::from);
    }
}
