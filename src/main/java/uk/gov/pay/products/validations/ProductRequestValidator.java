package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import uk.gov.pay.products.util.Errors;

import java.util.List;
import java.util.Optional;

public class ProductRequestValidator {
    private final RequestValidations requestValidations;
    private static final String FIELD_GATEWAY_ACCOUNT_ID = "gateway_account_id";
    private static final String FIELD_PAY_API_TOKEN = "pay_api_token";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PRICE = "price";
    private static final String FIELD_RETURN_URL = "return_url";


    @Inject
    public ProductRequestValidator(RequestValidations requestValidations) {
        this.requestValidations = requestValidations;
    }

    public Optional<Errors> validateCreateRequest(JsonNode payload) {
        Optional<List<String>> errors = requestValidations.checkIfExists(
                payload,
                FIELD_GATEWAY_ACCOUNT_ID,
                FIELD_PAY_API_TOKEN,
                FIELD_NAME,
                FIELD_PRICE,
                FIELD_RETURN_URL);

        if(!errors.isPresent()){
            errors = requestValidations.checkIsUrl(payload, FIELD_RETURN_URL);
        }

        return errors.map(Errors::from);
    }
}
