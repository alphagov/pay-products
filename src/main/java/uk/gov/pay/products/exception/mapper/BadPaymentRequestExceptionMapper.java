package uk.gov.pay.products.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.exception.BadPaymentRequestException;
import uk.gov.pay.products.util.Errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class BadPaymentRequestExceptionMapper implements ExceptionMapper<BadPaymentRequestException> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Response toResponse(BadPaymentRequestException exception) {
        logger.info("BadPaymentRequestException thrown.", exception);
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(Errors.from(exception.getMessage()))
                .build();
    }
}
