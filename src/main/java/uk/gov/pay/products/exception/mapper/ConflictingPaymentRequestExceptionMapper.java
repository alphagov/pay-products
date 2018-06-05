package uk.gov.pay.products.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.exception.ConflictingPaymentRequestException;
import uk.gov.pay.products.util.Errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ConflictingPaymentRequestExceptionMapper implements ExceptionMapper<ConflictingPaymentRequestException> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Response toResponse(ConflictingPaymentRequestException exception) {
        logger.error("ConflictingPaymentRequestException thrown.", exception);
        return Response
                .status(Response.Status.CONFLICT)
                .entity(Errors.from(exception.getMessage()))
                .build();
    }
}
