package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import uk.gov.pay.products.util.Errors;

import java.util.List;
import java.util.Optional;

public class ProductRequestValidator {
    private final RequestValidations requestValidations;
    private static final String FIELD_EXTERNAL_SERVICE_ID = "external_service_id";
    private static final String FIELD_PAY_API_TOKEN = "pay_api_token";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PRICE = "price";


    @Inject
    public ProductRequestValidator(RequestValidations requestValidations) {
        this.requestValidations = requestValidations;
    }

    public Optional<Errors> validateCreateRequest(JsonNode payload) {
        Optional<List<String>> errors = requestValidations.checkIfExists(
                payload,
                FIELD_EXTERNAL_SERVICE_ID,
                FIELD_PAY_API_TOKEN,
                FIELD_NAME,
                FIELD_PRICE);

        return errors.map(Errors::from);
    }
}
