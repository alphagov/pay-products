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
import static uk.gov.pay.products.util.PublicAPIErrorCodes.CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_ERROR_CODE;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_REJECTED;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.GENERIC;

public class PaymentCreationExceptionMapper implements ExceptionMapper<PaymentCreationException> {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public Response toResponse(PaymentCreationException exception) {
        logger.error("PaymentCreationException thrown.", exception);

        var status = getStatus(exception);

        ErrorIdentifier errorIdentifier = CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_ERROR_CODE.equals(exception.getErrorCode()) ?
                CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_REJECTED : GENERIC;

        return Response
                .status(status)
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
