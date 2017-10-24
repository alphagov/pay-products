package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import io.dropwizard.jersey.PATCH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.service.ProductsFactory;
import uk.gov.pay.products.validations.ProductRequestValidator;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;

@Path("/")
public class ProductResource {
    private static Logger logger = LoggerFactory.getLogger(ProductResource.class);

    public static final String PRODUCTS_RESOURCE = "/v1/api/products";
    private static final String PRODUCTS_RESOURCE_GET = PRODUCTS_RESOURCE + "/{productExternalId}";
    private static final String PRODUCTS_RESOURCE_DISABLE = PRODUCTS_RESOURCE + "/{productExternalId}/disable";

    private final ProductRequestValidator requestValidator;
    private final ProductsFactory productsFactory;


    @Inject
    public ProductResource(ProductRequestValidator requestValidator, ProductsFactory productsFactory) {
        this.requestValidator = requestValidator;
        this.productsFactory = productsFactory;
    }

    @POST
    @Path(PRODUCTS_RESOURCE)
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response createProduct(JsonNode payload) {
        logger.info("Create Service POST request - [ {} ]", payload);
        return requestValidator.validateCreateRequest(payload)
                .map(errors -> Response.status(Status.BAD_REQUEST).entity(errors).build())
                .orElseGet(() -> {
                    Product product = productsFactory.productsCreator().doCreate(Product.from(payload));
                    return Response.status(Status.CREATED).entity(product).build();
                });

    }

    @GET
    @Path(PRODUCTS_RESOURCE_GET)
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response findProduct(@PathParam("productExternalId") String productExternalId) {
        logger.info("Find a product with externalId - [ {} ]", productExternalId);
        return productsFactory.productsFinder().findByExternalId(productExternalId)
                .map(product ->
                        Response.status(OK).entity(product).build())
                .orElseGet(() ->
                        Response.status(NOT_FOUND).build());
    }

    @PATCH
    @Path(PRODUCTS_RESOURCE_DISABLE)
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response disableProduct(@PathParam("productExternalId") String productExternalId) {
        logger.info("Disabling a product with externalId - [ {} ]", productExternalId);
        return productsFactory.productsFinder().disableProduct(productExternalId)
                .map(product -> Response.status(NO_CONTENT).build())
                .orElseGet(() -> Response.status(NOT_FOUND).build());
    }

    @GET
    @Path(PRODUCTS_RESOURCE)
    @Produces(APPLICATION_JSON)
    @PermitAll
    public Response findProducts(@QueryParam("gatewayAccountId") Integer gatewayAccountId) {
        logger.info("Searching for products with gatewayAccountId - [ {} ]", gatewayAccountId);
        List<Product> products = productsFactory.productsFinder().findByGatewayAccountId(gatewayAccountId);
        return products.size() > 0 ? Response.status(OK).entity(products).build() : Response.status(NOT_FOUND).build();
    }
}
