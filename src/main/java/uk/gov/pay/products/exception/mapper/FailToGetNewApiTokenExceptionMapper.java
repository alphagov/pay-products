package uk.gov.pay.products.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.exception.FailToGetNewApiTokenException;
import uk.gov.pay.products.util.Errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class FailToGetNewApiTokenExceptionMapper implements ExceptionMapper<FailToGetNewApiTokenException> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Response toResponse(FailToGetNewApiTokenException exception) {
        logger.error("FailToGetNewApiTokenException thrown.", exception);
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Errors.from(exception.getMessage()))
                .build();
    }
}
