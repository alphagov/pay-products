package uk.gov.pay.products.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Optional;

import static uk.gov.service.payments.logging.LoggingKeys.GATEWAY_ACCOUNT_ID;
import static uk.gov.service.payments.logging.LoggingKeys.PAYMENT_EXTERNAL_ID;
import static uk.gov.service.payments.logging.LoggingKeys.SERVICE_PAYMENT_REFERENCE;

public class LoggingMDCRequestFilter implements ContainerRequestFilter {
    
    public static final String PRODUCT_EXTERNAL_ID= "product_external_id";
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        getPathParameterFromRequest("gatewayAccountId", requestContext)
                .ifPresent(gatewayAccountId -> MDC.put(GATEWAY_ACCOUNT_ID, gatewayAccountId));

        getPathParameterFromRequest("paymentExternalId", requestContext)
                .ifPresent(paymentExternalId -> MDC.put(PAYMENT_EXTERNAL_ID, paymentExternalId));

        getPathParameterFromRequest("productExternalId", requestContext)
                .ifPresent(productExternalId -> MDC.put(PRODUCT_EXTERNAL_ID, productExternalId));

        getPathParameterFromRequest("referenceNumber", requestContext)
                .ifPresent(referenceNumber -> MDC.put(SERVICE_PAYMENT_REFERENCE, referenceNumber));
    }

    private Optional<String> getPathParameterFromRequest(String parameterName, ContainerRequestContext requestContext) {
        return Optional.ofNullable(requestContext.getUriInfo().getPathParameters().getFirst(parameterName));
    }
}
