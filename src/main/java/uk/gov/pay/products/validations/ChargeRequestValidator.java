package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import uk.gov.pay.products.util.Errors;

import java.util.List;
import java.util.Optional;

public class ChargeRequestValidator {
    private static final String EXTERNAL_PRODUCT_ID = "external_product_id";
    private RequestValidations requestValidations;

    @Inject
    public ChargeRequestValidator(RequestValidations requestValidations) {
        this.requestValidations = requestValidations;
    }

    public Optional<Errors> validateCreateRequest(JsonNode payload){
        Optional<List<String>> errors = requestValidations.checkIfExists(
                payload,
                EXTERNAL_PRODUCT_ID
        );

        return errors.map(Errors::from);
    }
}
