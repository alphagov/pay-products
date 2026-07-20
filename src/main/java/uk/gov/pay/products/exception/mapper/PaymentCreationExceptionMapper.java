package uk.gov.pay.products.exception.mapper;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.exception.PaymentCreationException;
import uk.gov.pay.products.util.Errors;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import static java.lang.String.format;
import static uk.gov.pay.products.util.PublicAPIErrorCodes.ACCOUNT_NOT_LINKED_WITH_PSP;
import static uk.gov.pay.products.util.PublicAPIErrorCodes.CREATE_PAYMENT_CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_ERROR;
import static uk.gov.pay.products.util.PublicAPIErrorCodes.CREATE_PAYMENT_VALIDATION_ERROR;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.AMOUNT_BELOW_MINIMUM;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_REJECTED;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.GENERIC;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.ZERO_AMOUNT_NOT_ALLOWED;

public class PaymentCreationExceptionMapper implements ExceptionMapper<PaymentCreationException> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public Response toResponse(PaymentCreationException exception) {
        ErrorIdentifier errorIdentifier = GENERIC;
        
        switch (exception.getErrorCode()) {
            case ACCOUNT_NOT_LINKED_WITH_PSP -> 
                logger.warn(format("PaymentCreationException thrown due to %s. The account is not fully configured.", 
                        ACCOUNT_NOT_LINKED_WITH_PSP));
            case CREATE_PAYMENT_CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_ERROR -> {
                errorIdentifier = CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_REJECTED;
                logger.info(format("%s thrown due to %s. Reason: %s", PaymentCreationException.class.getName(), 
                        CREATE_PAYMENT_CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_ERROR, exception.getMessage()));
            }
            case CREATE_PAYMENT_VALIDATION_ERROR -> {
                /* 
                The following is rather dirty, but differentiating between the two cases requires deeper thinking
                about amending the response from publicapi 
                */
                if (exception.getMessage().contains("Must be greater than or equal to 30")) {
                    errorIdentifier = AMOUNT_BELOW_MINIMUM;
                } else {
                    errorIdentifier = ZERO_AMOUNT_NOT_ALLOWED;    
                }
                logger.info(format("%s thrown due to %s. Reason: %s", PaymentCreationException.class.getName(), 
                        CREATE_PAYMENT_VALIDATION_ERROR, exception.getMessage()));
            }
            default -> logger.error("PaymentCreationException thrown.", exception);
        }

        return Response
                .status(getStatus(exception))
                .entity(Errors.from("Upstream system error.", errorIdentifier.toString()))
                .build();
    }

    private int getStatus(PaymentCreationException exception) {
        return switch (exception.getErrorCode()) {
            case CREATE_PAYMENT_CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_ERROR -> HttpStatus.SC_BAD_REQUEST;
            case CREATE_PAYMENT_VALIDATION_ERROR -> HttpStatus.SC_UNPROCESSABLE_ENTITY;
            default -> exception.getErrorStatusCode() == HttpStatus.SC_FORBIDDEN ? HttpStatus.SC_FORBIDDEN : HttpStatus.SC_INTERNAL_SERVER_ERROR;
        };
    }
}
