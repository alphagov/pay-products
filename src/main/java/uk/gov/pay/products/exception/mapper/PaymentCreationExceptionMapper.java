package uk.gov.pay.products.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.exception.PaymentCreationException;
import uk.gov.pay.products.util.Errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class PaymentCreationExceptionMapper implements ExceptionMapper<PaymentCreationException> {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public Response toResponse(PaymentCreationException exception) {
        logger.error("PaymentCreationException thrown.", exception);

        var status = exception.getErrorStatusCode() == FORBIDDEN.getStatusCode()? FORBIDDEN : INTERNAL_SERVER_ERROR;
        
        return Response
                .status(status)
                .entity(Errors.from("Downstream system error."))
                .build();
    }
}
