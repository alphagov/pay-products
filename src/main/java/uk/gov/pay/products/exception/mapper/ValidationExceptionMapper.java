package uk.gov.pay.products.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.util.Errors;
import uk.gov.service.payments.commons.api.exception.ValidationException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationExceptionMapper.class);

    @Override
    public Response toResponse(ValidationException exception) {
        LOGGER.info("Validation exception thrown: " + String.join("\n", exception.getErrors()));
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(Errors.from(exception.getErrors()))
                .build();
    }
}
