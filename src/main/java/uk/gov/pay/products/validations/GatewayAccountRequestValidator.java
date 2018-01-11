package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.pay.products.util.Errors;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static uk.gov.pay.products.model.GatewayAccountRequest.FIELD_OPERATION;
import static uk.gov.pay.products.model.GatewayAccountRequest.FIELD_OPERATION_PATH;
import static uk.gov.pay.products.model.GatewayAccountRequest.FIELD_VALUE;

public class GatewayAccountRequestValidator {

    private final RequestValidations requestValidations;

    @Inject
    public GatewayAccountRequestValidator(RequestValidations requestValidations) {
        this.requestValidations = requestValidations;
    }

    public Optional<Errors> validatePatchRequest(JsonNode payload) {
        Optional<List<String>> errors = requestValidations.checkIfExists(
                payload,
                FIELD_OPERATION,
                FIELD_OPERATION_PATH,
                FIELD_VALUE);

        return errors.map(Errors::from);
    }
}
