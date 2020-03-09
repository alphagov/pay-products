package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.model.ProductMetadata;
import uk.gov.pay.products.service.ProductsMetadataFactory;
import uk.gov.pay.products.validations.ProductsMetadataRequestValidator;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/v1/api/products/{productExternalId}/metadata")
@Produces(APPLICATION_JSON)
public class ProductMetadataResource {

    private static final Logger logger = LoggerFactory.getLogger(ProductMetadataResource.class);
    private final ProductsMetadataFactory metadataFactory;
    private final ProductsMetadataRequestValidator requestValidator;

    @Inject
    public ProductMetadataResource(ProductsMetadataFactory metadataFactory,
                                   ProductsMetadataRequestValidator requestValidator) {
        this.metadataFactory = metadataFactory;
        this.requestValidator = requestValidator;
    }

    @POST
    @Consumes(APPLICATION_JSON)
    public Response createMetadata(@PathParam("productExternalId") String productExternalId, JsonNode payload) {
        logger.info("Create product metadata for id - [ {} ]", productExternalId);

        List<ProductMetadata> metadataList = metadataFactory.metadataFinder().findMetadataByProductExternalId(productExternalId);

        return requestValidator.validateRequest(payload)
                .map(errors -> Response.status(Response.Status.BAD_REQUEST).entity(errors).build())
                .orElseGet(() ->
                    requestValidator.validateCreateRequest(payload, metadataList)
                            .map(errors -> Response.status(Response.Status.BAD_REQUEST).entity(errors).build())
                            .orElseGet(() -> {
                                metadataFactory.metadataCreator().createProductMetadata(payload, productExternalId);
                                return Response.status(Response.Status.CREATED).build();
                            }));
    }

    @PATCH
    @Consumes(APPLICATION_JSON)
    public Response updateMetadata(@PathParam("productExternalId") String productExternalId, JsonNode payload) {
        logger.info("Update product metadata for id - [ {} ]", productExternalId);

        List<ProductMetadata> metadataList = metadataFactory.metadataFinder().findMetadataByProductExternalId(productExternalId);

        return requestValidator.validateRequest(payload)
                .map(errors -> Response.status(Response.Status.BAD_REQUEST).entity(errors).build())
                .orElseGet(() -> requestValidator.validateUpdateRequest(payload, metadataList)
                       .map(errors -> Response.status(Response.Status.NOT_FOUND).entity(errors).build())
                       .orElseGet(() -> {
                           metadataFactory.metadataUpdater().updateMetadata(payload, productExternalId);
                           return Response.status(Response.Status.OK).build();
                       }));
    }

    @DELETE
    @Path("/{metadataKey}")
    public Response deleteMetadata(@PathParam("productExternalId") String productExternalId, @PathParam("metadataKey") String metadataKey) {
        logger.info("Delete product metadata for id - [ {} ] with key id [ {} ]", productExternalId, metadataKey);

        metadataFactory.metadataDeleter().deleteMetadata(productExternalId, metadataKey);

        return Response.status(Response.Status.OK).build();
    }
}
