package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.pay.products.util.Errors;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static uk.gov.pay.products.model.PatchRequest.FIELD_OPERATION;
import static uk.gov.pay.products.model.PatchRequest.FIELD_OPERATION_PATH;
import static uk.gov.pay.products.model.PatchRequest.FIELD_VALUE;

public class GatewayAccountRequestValidator {

    private final RequestValidations requestValidations;

    private static final Map<String, List<String>> VALID_ATTRIBUTE_UPDATE_OPERATIONS = new HashMap<String, List<String>>() {{
        put(FIELD_OPERATION, singletonList("replace"));
    }};
    public static final String FIELD_SERVICE_NAME = "service_name";

    @Inject
    public GatewayAccountRequestValidator(RequestValidations requestValidations) {
        this.requestValidations = requestValidations;
    }

    public Optional<Errors> validatePatchRequest(JsonNode payload) {
        Optional<List<String>> errors = requestValidations.checkIfExistsOrEmpty(
                payload,
                FIELD_OPERATION,
                FIELD_OPERATION_PATH,
                FIELD_VALUE);
        if(errors.isPresent()) {
            return errors.map(Errors::from);
        }

        return validateServiceNameRequest(payload).map(Errors::from);
    }

    private Optional<List<String>> validateServiceNameRequest(JsonNode payload){
        if(!payload.findValue(FIELD_OPERATION_PATH).asText().equals(FIELD_SERVICE_NAME)) {
            return  Optional.of(singletonList(format("Path %s not supported / invalid",
                    payload.findValue(FIELD_OPERATION_PATH).asText())));
        }
        String op = payload.get(FIELD_OPERATION).asText();
        if (!VALID_ATTRIBUTE_UPDATE_OPERATIONS.get(FIELD_OPERATION).contains(op)) {
            return Optional.of(singletonList(format("Operation [%s] is not valid for path [%s]", op, FIELD_OPERATION)));
        }

        return Optional.empty();
    }
}
