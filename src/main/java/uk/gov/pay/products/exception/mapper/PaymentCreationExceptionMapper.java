package uk.gov.pay.products.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.exception.PaymentCreationException;
import uk.gov.pay.products.util.Errors;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static uk.gov.pay.products.util.PublicAPIErrorCodes.ACCOUNT_NOT_LINKED_WITH_PSP_ERROR_CODE;
import static uk.gov.pay.products.util.PublicAPIErrorCodes.CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_ERROR_CODE;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_REJECTED;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.GENERIC;

public class PaymentCreationExceptionMapper implements ExceptionMapper<PaymentCreationException> {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public Response toResponse(PaymentCreationException exception) {
        ErrorIdentifier errorIdentifier = GENERIC;
        if (CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_ERROR_CODE.equals(exception.getErrorCode())) {
            errorIdentifier = CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_REJECTED;
            logger.info(PaymentCreationException.class.getName() + " thrown due to " + CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_ERROR_CODE);
        } else if (ACCOUNT_NOT_LINKED_WITH_PSP_ERROR_CODE.equals(exception.getErrorCode())) {
            logger.warn("PaymentCreationException thrown due to " + ACCOUNT_NOT_LINKED_WITH_PSP_ERROR_CODE + ". The account is not fully configured.");
        } else {
            logger.error("PaymentCreationException thrown.", exception);
        }

        return Response
                .status(getStatus(exception))
                .entity(Errors.from("Downstream system error.", errorIdentifier.toString()))
                .build();
    }

    private Response.Status getStatus(PaymentCreationException exception) {
        if (CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_ERROR_CODE.equals(exception.getErrorCode())) {
            return BAD_REQUEST;
        }

        return exception.getErrorStatusCode() == FORBIDDEN.getStatusCode() ? FORBIDDEN : INTERNAL_SERVER_ERROR;
    }
}
