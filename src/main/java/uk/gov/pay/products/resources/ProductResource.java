package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import io.dropwizard.jersey.PATCH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.service.ProductFactory;
import uk.gov.pay.products.validations.ProductRequestValidator;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;

@Path("/v1/api")
public class ProductResource {
    private static Logger logger = LoggerFactory.getLogger(ProductResource.class);

    private final ProductRequestValidator requestValidator;
    private final ProductFactory productFactory;

    @Inject
    public ProductResource(ProductRequestValidator requestValidator, ProductFactory productFactory) {
        this.requestValidator = requestValidator;
        this.productFactory = productFactory;
    }

    @POST
    @Path("/products")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response createProduct(JsonNode payload) {
        logger.info("Create Service POST request - [ {} ]", payload);
        return requestValidator.validateCreateRequest(payload)
                .map(errors -> Response.status(Status.BAD_REQUEST).entity(errors).build())
                .orElseGet(() -> {
                    Product product = productFactory.productCreator().doCreate(Product.from(payload));
                    return Response.status(Status.CREATED).entity(product).build();
                });

    }

    @GET
    @Path("/products/{productExternalId}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response findProductByExternalId(@PathParam("productExternalId") String productExternalId) {
        logger.info("Find a product with externalId - [ {} ]", productExternalId);
        return productFactory.productFinder().findByExternalId(productExternalId)
                .map(product ->
                        Response.status(OK).entity(product).build())
                .orElseGet(() ->
                        Response.status(NOT_FOUND).build());
    }

    @GET
    @Path("/gateway-account/{gatewayAccountId}/products/{productExternalId}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response findProductByGatewayAccountIdAndExternalId(@PathParam("gatewayAccountId") Integer gatewayAccountId, @PathParam("productExternalId") String productExternalId) {
        logger.info("Find a product with externalId - [ {} ]", productExternalId);
        return productFactory.productFinder().findByGatewayAccountIdAndExternalId(gatewayAccountId, productExternalId)
                .map(product ->
                        Response.status(OK).entity(product).build())
                .orElseGet(() ->
                        Response.status(NOT_FOUND).build());
    }

    @PATCH
    @Path("/products/{productExternalId}/disable")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response disableProductByExternalId(@PathParam("productExternalId") String productExternalId) {
        logger.info("Disabling a product with externalId - [ {} ]", productExternalId);
        return productFactory.productFinder().disableByExternalId(productExternalId)
                .map(product -> Response.status(NO_CONTENT).build())
                .orElseGet(() -> Response.status(NOT_FOUND).build());
    }

    @PATCH
    @Path("/gateway-account/{gatewayAccountId}/products/{productExternalId}/disable")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response disableProductByGatewayAccountIdAndExternalId(@PathParam("gatewayAccountId") Integer gatewayAccountId, @PathParam("productExternalId") String productExternalId) {
        logger.info("Disabling a product with externalId - [ {} ]", productExternalId);
        return productFactory.productFinder().disableByGatewayAccountIdAndExternalId(gatewayAccountId, productExternalId)
                .map(product -> Response.status(NO_CONTENT).build())
                .orElseGet(() -> Response.status(NOT_FOUND).build());
    }

    @GET
    @Path("/gateway-account/{gatewayAccountId}/products")
    @Produces(APPLICATION_JSON)
    public Response findProductsByGatewayAccountId(@PathParam("gatewayAccountId") Integer gatewayAccountId) {
        logger.info("Searching for products with gatewayAccountId - [ {} ]", gatewayAccountId);
        List<Product> products = productFactory.productFinder().findByGatewayAccountId(gatewayAccountId);
        return Response.status(OK).entity(products).build();
    }

    @GET
    @Path("/payments")
    @Produces(APPLICATION_JSON)
    public Response findProductByProductPath(
            @QueryParam("serviceNamePath") String serviceNamePath,
            @QueryParam("productNamePath") String productNamePath ) {
        logger.info(format("Searching for product with product path - [ serviceNamePath=%s productNamePath=%s ]", serviceNamePath, productNamePath));
        return productFactory.productFinder().findByProductPath(serviceNamePath, productNamePath)
                    .map(product -> Response.status(OK).entity(product).build())
                            .orElseGet(() -> Response.status(NOT_FOUND).build());
    }
}
