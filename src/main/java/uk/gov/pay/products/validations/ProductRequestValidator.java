package uk.gov.pay.products.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.products.config.ProductsConfiguration;
import uk.gov.pay.products.util.Errors;
import uk.gov.pay.products.util.ProductType;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static uk.gov.pay.products.model.Product.FIELD_GATEWAY_ACCOUNT_ID;
import static uk.gov.pay.products.model.Product.FIELD_LANGUAGE;
import static uk.gov.pay.products.model.Product.FIELD_NAME;
import static uk.gov.pay.products.model.Product.FIELD_PAY_API_TOKEN;
import static uk.gov.pay.products.model.Product.FIELD_PRICE;
import static uk.gov.pay.products.model.Product.FIELD_PRODUCT_NAME_PATH;
import static uk.gov.pay.products.model.Product.FIELD_REFERENCE_ENABLED;
import static uk.gov.pay.products.model.Product.FIELD_REFERENCE_LABEL;
import static uk.gov.pay.products.model.Product.FIELD_RETURN_URL;
import static uk.gov.pay.products.model.Product.FIELD_SERVICE_NAME_PATH;
import static uk.gov.pay.products.model.Product.FIELD_TYPE;
import static uk.gov.pay.products.util.ProductType.ADHOC;

public class ProductRequestValidator {
    private final RequestValidations requestValidations;
    private final boolean returnUrlMustBeSecure;

    @Inject
    public ProductRequestValidator(RequestValidations requestValidations, ProductsConfiguration configuration) {
        this.requestValidations = requestValidations;
        this.returnUrlMustBeSecure = configuration.getReturnUrlMustBeSecure();
    }

    public Optional<Errors> validateCreateRequest(JsonNode payload) {
        Optional<List<String>> errors = requestValidations.checkIfExistsOrEmpty(
                payload,
                FIELD_GATEWAY_ACCOUNT_ID,
                FIELD_PAY_API_TOKEN,
                FIELD_NAME,
                FIELD_TYPE);

        if (errors.isEmpty() && payload.get(FIELD_RETURN_URL) != null) {
            errors = returnUrlMustBeSecure
                    ? requestValidations.checkIsHttpsUrl(payload, FIELD_RETURN_URL)
                    : requestValidations.checkIsUrl(payload, FIELD_RETURN_URL);
        }

        if (errors.isEmpty() && payload.get(FIELD_PRICE) != null) {
            errors = requestValidations.checkIsBelowMaxAmount(payload, FIELD_PRICE);
        }

        if (errors.isEmpty() && payload.get(FIELD_TYPE) != null) {
            errors = requestValidations.checkIsValidEnumValue(payload, EnumSet.allOf(ProductType.class), FIELD_TYPE);
        }

        if (errors.isEmpty() && !ADHOC.name().equals(payload.get(FIELD_TYPE).asText())) {
            errors = requestValidations.checkIfExistsOrEmpty(payload, FIELD_PRICE);
        }

        if (errors.isEmpty() && ADHOC.name().equals(payload.get(FIELD_TYPE).asText())) {
            errors = requestValidations.checkIfExistsOrEmpty(payload, FIELD_SERVICE_NAME_PATH, FIELD_PRODUCT_NAME_PATH);
        }
        
        if (errors.isEmpty() && payload.get(FIELD_REFERENCE_ENABLED) != null && payload.get(FIELD_REFERENCE_ENABLED).asBoolean()) {
            errors = requestValidations.checkIfExistsOrEmpty(payload, FIELD_REFERENCE_LABEL);
        }
        
        if (errors.isEmpty() && payload.get(FIELD_LANGUAGE) != null ) {
            errors = requestValidations.checkIsString(payload, FIELD_LANGUAGE);
            if (errors.isEmpty()) {
                errors = requestValidations.checkIsValidEnumValue(payload, EnumSet.allOf(SupportedLanguage.class), FIELD_LANGUAGE);
            }
        }
        
        return errors.map(Errors::from);
    }

    public Optional<Errors> validateUpdateRequest(JsonNode payload) {
        Optional<List<String>> errors = requestValidations.checkIfExistsOrEmpty(
                payload,
                FIELD_NAME);

        if (errors.isEmpty() && payload.get(FIELD_PRICE) != null) {
            errors = requestValidations.checkIsBelowMaxAmount(payload, FIELD_PRICE);
        }

        return errors.map(Errors::from);
    }

}
