package uk.gov.pay.products.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.exception.FailToReplaceApiTokenException;
import uk.gov.pay.products.util.Errors;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class FailToReplaceApiTokenExceptionMapper implements ExceptionMapper<FailToReplaceApiTokenException> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Response toResponse(FailToReplaceApiTokenException exception) {
        logger.error("FailToReplaceApiTokenException thrown.", exception);
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Errors.from(exception.getMessage()))
                .build();
    }
}
