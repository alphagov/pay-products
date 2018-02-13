package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import uk.gov.pay.products.util.Errors;

import java.util.List;
import java.util.Optional;

public class ProductRequestValidator {
    private static final String FIELD_GATEWAY_ACCOUNT_ID = "gateway_account_id";
    private static final String FIELD_PAY_API_TOKEN = "pay_api_token";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PRICE = "price";
    private static final String FIELD_RETURN_URL = "return_url";
    private static final String FIELD_SERVICE_NAME = "service_name";
    private static final String FIELD_TYPE = "type";
    private static final int FIELD_SERVICE_NAME_MAX_LENGTH = 50;

    private final RequestValidations requestValidations;

    @Inject
    public ProductRequestValidator(RequestValidations requestValidations) {
        this.requestValidations = requestValidations;
    }

    public Optional<Errors> validateCreateRequest(JsonNode payload) {
        Optional<List<String>> errors = requestValidations.checkIfExistsOrEmpty(
                payload,
                FIELD_GATEWAY_ACCOUNT_ID,
                FIELD_PAY_API_TOKEN,
                FIELD_NAME,
                FIELD_PRICE,
                FIELD_TYPE,
                FIELD_SERVICE_NAME);

        if (!errors.isPresent() && payload.get(FIELD_RETURN_URL) != null) {
            errors = requestValidations.checkIsUrl(payload, FIELD_RETURN_URL);
        }

        if (!errors.isPresent() && payload.get(FIELD_PRICE) != null) {
            errors = requestValidations.checkIsBelowMaxAmount(payload, FIELD_PRICE);
        }

        if (!errors.isPresent() && payload.get(FIELD_TYPE) != null) {
            errors = requestValidations.checkIsProductType(payload, FIELD_TYPE);
        }

        if (!errors.isPresent() && payload.get(FIELD_SERVICE_NAME) != null) {
            errors = requestValidations.checkMaxLength(payload, FIELD_SERVICE_NAME_MAX_LENGTH, FIELD_SERVICE_NAME);
        }

        return errors.map(Errors::from);
    }
}
