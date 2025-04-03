package uk.gov.pay.products.filters;

import org.slf4j.MDC;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;
import java.util.List;

import static uk.gov.pay.products.filters.LoggingMDCRequestFilter.PRODUCT_EXTERNAL_ID;
import static uk.gov.service.payments.logging.LoggingKeys.GATEWAY_ACCOUNT_ID;
import static uk.gov.service.payments.logging.LoggingKeys.PAYMENT_EXTERNAL_ID;
import static uk.gov.service.payments.logging.LoggingKeys.SERVICE_PAYMENT_REFERENCE;

public class LoggingMDCResponseFilter implements ContainerResponseFilter {
    
    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        List.of(GATEWAY_ACCOUNT_ID, PAYMENT_EXTERNAL_ID, PRODUCT_EXTERNAL_ID, SERVICE_PAYMENT_REFERENCE)
                .forEach(MDC::remove);
    }
}
