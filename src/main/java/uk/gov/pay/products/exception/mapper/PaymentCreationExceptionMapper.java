package uk.gov.pay.products.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.exception.PaymentCreationException;
import uk.gov.pay.products.util.Errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class PaymentCreationExceptionMapper implements ExceptionMapper<PaymentCreationException> {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public Response toResponse(PaymentCreationException exception) {
        logger.error("PaymentCreationException thrown.", exception);

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Errors.from("Downstream system error."))
                .build();
    }
}
