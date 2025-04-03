package uk.gov.pay.products.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.exception.MetadataNotFoundException;
import uk.gov.pay.products.util.Errors;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import static java.lang.String.format;

public class MetadataNotFoundExceptionMapper implements ExceptionMapper<MetadataNotFoundException> {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public Response toResponse(MetadataNotFoundException exception) {
        logger.info("PaymentCreationException thrown.", exception);

        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(Errors.from(format("Metadata for id [ %s ] and key [ %s ] not found", exception.getProductExternalId(), exception.getKey())))
                .build();
    }
}
