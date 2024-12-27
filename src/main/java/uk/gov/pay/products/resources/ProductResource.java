package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import io.dropwizard.jersey.PATCH;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.model.ProductUpdateRequest;
import uk.gov.pay.products.model.ProductUsageStat;
import uk.gov.pay.products.model.product.CreateProductRequest;
import uk.gov.pay.products.service.ProductApiTokenManager;
import uk.gov.pay.products.service.ProductFactory;
import uk.gov.pay.products.util.ProductType;
import uk.gov.pay.products.validations.ProductRequestValidator;
import uk.gov.service.payments.commons.model.jsonpatch.JsonPatchRequest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static net.logstash.logback.argument.StructuredArguments.kv;
import static uk.gov.pay.products.model.Product.FIELD_GATEWAY_ACCOUNT_ID;
import static uk.gov.pay.products.model.Product.FIELD_NAME;
import static uk.gov.pay.products.model.Product.FIELD_TYPE;
import static uk.gov.service.payments.logging.LoggingKeys.GATEWAY_ACCOUNT_ID;

@Path("/")
public class ProductResource {
    private static final Logger logger = LoggerFactory.getLogger(ProductResource.class);

    private final ProductRequestValidator requestValidator;
    private final ProductFactory productFactory;
    private final ProductApiTokenManager productApiTokenManager;

    @Inject
    public ProductResource(ProductRequestValidator requestValidator,
                           ProductFactory productFactory,
                           ProductApiTokenManager productApiTokenManager) {
        this.requestValidator = requestValidator;
        this.productFactory = productFactory;
        this.productApiTokenManager = productApiTokenManager;
    }

    @POST
    @Path("/v1/api/products")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(
            tags = {"Products"},
            summary = "creates a new product",
            operationId = "search transactions",
            responses = {
                    @ApiResponse(responseCode = "201", description = "OK",
                            content = @Content(schema = @Schema(implementation = Product.class))),
                    @ApiResponse(responseCode = "409", description = "A product with product_name_path already exists", content = @Content(schema = @Schema())),
                    @ApiResponse(responseCode = "400", description = "Invalid payload")
            }
    )
    public Response createProduct(@RequestBody(content = @Content(schema = @Schema(implementation = CreateProductRequest.class)))
                                          JsonNode payload) {
        logger.info(
                "Create Service POST request",
                kv(GATEWAY_ACCOUNT_ID, payload.get(FIELD_GATEWAY_ACCOUNT_ID)),
                kv(FIELD_TYPE, payload.get(FIELD_TYPE)),
                kv(FIELD_NAME, payload.get(FIELD_NAME))
        );
        return requestValidator.validateCreateRequest(payload)
                .map(errors -> Response.status(Status.BAD_REQUEST).entity(errors).build())
                .orElseGet(() -> {
                    try {
                        Product product = productFactory.productCreator().doCreate(Product.from(payload));
                        return Response.status(Status.CREATED).entity(product).build();
                    } catch (jakarta.persistence.RollbackException ex) {
                        logger.info("Conflict error while persisting product, product path already exists. " + ex.getLocalizedMessage());
                        return Response.status(Status.CONFLICT).build();
                    }
                });
    }

    @GET
    @Path("/v1/api/products/{productExternalId}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(
            tags = {"Products"},
            summary = "Get product by product external ID",
            operationId = "getProductByExternalId",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = Product.class))),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    public Response findProductByExternalId(@PathParam("productExternalId") @Parameter(example = "874h5c87834659q345698495") String productExternalId) {
        return productFactory.productFinder().findByExternalId(productExternalId)
                .map(product ->
                        Response.status(OK).entity(product).build())
                .orElseGet(() ->
                        Response.status(NOT_FOUND).build());
    }

    @GET
    @Path("/v1/api/gateway-account/{gatewayAccountId}/products/{productExternalId}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(
            tags = {"Products"},
            summary = "Find product by gateway account ID and product external ID",
            operationId = "getProductByGatewayAccountIdAndExternalId",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = Product.class))),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    public Response findProductByGatewayAccountIdAndExternalId(@Parameter(example = "1") @PathParam("gatewayAccountId") Integer gatewayAccountId,
                                                               @Parameter(example = "874h5c87834659q345698495") @PathParam("productExternalId") String productExternalId) {
        return productFactory.productFinder().findByGatewayAccountIdAndExternalId(gatewayAccountId, productExternalId)
                .map(product ->
                        Response.status(OK).entity(product).build())
                .orElseGet(() ->
                        Response.status(NOT_FOUND).build());
    }

    @Deprecated
    @PATCH
    @Path("/v1/api/products/{productExternalId}/disable")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(tags = {"Deprecated"})
    public Response disableProductByExternalId(@PathParam("productExternalId") String productExternalId) {
        logger.info("Disabling a product with externalId - [ {} ]", productExternalId);
        return productFactory.productFinder().disableByExternalId(productExternalId)
                .map(product -> Response.status(NO_CONTENT).build())
                .orElseGet(() -> Response.status(NOT_FOUND).build());
    }

    @DELETE
    @Path("/v1/api/products/{productExternalId}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(
            tags = {"Products"},
            summary = "Delete product with the specified external product id",
            operationId = "deleteProductByExternalId",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No content"),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    public Response deleteProductByExternalId(@Parameter(example = "874h5c87834659q345698495")
                                              @PathParam("productExternalId") String productExternalId) {
        logger.info("Deleting a product with externalId - [ {} ]", productExternalId);
        Boolean success = productFactory.productFinder().deleteByExternalId(productExternalId);
        return success ? Response.status(NO_CONTENT).build() : Response.status(NOT_FOUND).build();
    }

    @Deprecated
    @PATCH
    @Path("/v1/api/gateway-account/{gatewayAccountId}/products/{productExternalId}/disable")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(
            tags = {"Deprecated"}
    )
    public Response disableProductByGatewayAccountIdAndExternalId(@PathParam("gatewayAccountId") Integer gatewayAccountId, @PathParam("productExternalId") String productExternalId) {
        logger.info("Disabling a product with externalId - [ {} ]", productExternalId);
        return productFactory.productFinder().disableByGatewayAccountIdAndExternalId(gatewayAccountId, productExternalId)
                .map(product -> Response.status(NO_CONTENT).build())
                .orElseGet(() -> Response.status(NOT_FOUND).build());
    }

    @PATCH
    @Path("/v1/api/gateway-account/{gatewayAccountId}/products/{productExternalId}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(
            tags = {"Products"},
            summary = "Patch product by gateway account ID and product external ID",
            operationId = "updateProduct",
            responses = {
                    @ApiResponse(responseCode = "200", description = "No content"),
                    @ApiResponse(responseCode = "400", description = "For invalid payload"),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    public Response updateProduct(@Parameter(example = "1") @PathParam("gatewayAccountId") Integer gatewayAccountId,
                                  @Parameter(example = "874h5c87834659q345698495") @PathParam("productExternalId") String productExternalId,
                                  @RequestBody(content = @Content(schema = @Schema(implementation = ProductUpdateRequest.class)))
                                          JsonNode payload) {
        logger.info(
                "Updating a product with externalId - [ {} ]",
                productExternalId,
                kv("product_external_id", productExternalId)
        );

        return requestValidator.validateUpdateRequest(payload)
                .map(errors -> Response.status(Status.BAD_REQUEST).entity(errors).build())
                .orElseGet(() ->
                        productFactory.productCreator().doUpdateByGatewayAccountId(gatewayAccountId, productExternalId, ProductUpdateRequest.from(payload))
                                .map(product -> Response.status(OK).entity(product).build())
                                .orElseGet(() -> Response.status(NOT_FOUND).build()));
    }

    /**
     * Update product using JSON patch.
     * <p>
     * TODO: Updates from /v1/api/gateway-account/{gatewayAccountId}/products/{productExternalId}
     * should be moved over to this endpoint and that API deprecated. This was not done at the time due to time constraints.
     */
    @PATCH
    @Path("/v2/api/gateway-account/{gatewayAccountId}/products/{productExternalId}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(
            tags = {"Products"},
            summary = "Patch product by gateway account ID and product external ID",
            description = "Only supports patching `require_captcha` field. Use /v1/ to patch other fields",
            operationId = "updateProductV2",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Product.class))),
                    @ApiResponse(responseCode = "400", description = "For invalid payload"),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    public Product updateProductJsonPatch(@Parameter(example = "1") @PathParam("gatewayAccountId") Integer gatewayAccountId,
                                          @Parameter(example = "874h5c87834659q345698495")
                                          @PathParam("productExternalId") String productExternalId,
                                          @RequestBody(content = @Content(schema = @Schema(
                                                  example = "[" +
                                                          "  {" +
                                                          "    \"op\": \"replace\"," +
                                                          "    \"path\": \"require_captcha\"," +
                                                          "    \"value\":true" +
                                                          "  }" +
                                                          "]")))
                                                  JsonNode payload) {
        requestValidator.validateJsonPatch(payload);
        List<JsonPatchRequest> patchRequests = StreamSupport.stream(payload.spliterator(), false)
                .map(JsonPatchRequest::from)
                .collect(Collectors.toList());
        return productFactory.productCreator().update(gatewayAccountId, productExternalId, patchRequests);
    }

    @DELETE
    @Path("/v1/api/gateway-account/{gatewayAccountId}/products/{productExternalId}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(
            tags = {"Products"},
            summary = "Deletes product with a specified gateway account ID and product external ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No content"),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    public Response deleteProductByGatewayAccountIdAndExternalId(@Parameter(example = "1") @PathParam("gatewayAccountId") Integer gatewayAccountId,
                                                                 @Parameter(example = "874h5c87834659q345698495") @PathParam("productExternalId") String productExternalId) {
        logger.info("Deleting a product with externalId - [ {} ]", productExternalId);
        Boolean success = productFactory.productFinder().deleteByGatewayAccountIdAndExternalId(gatewayAccountId, productExternalId);
        return success ? Response.status(NO_CONTENT).build() : Response.status(NOT_FOUND).build();
    }

    @GET
    @Path("/v1/api/gateway-account/{gatewayAccountId}/products")
    @Produces(APPLICATION_JSON)
    @Operation(
            tags = {"Products"},
            summary = "Find products by gateway account ID and type",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Product.class)))),
                    @ApiResponse(responseCode = "400", description = "Invalid request params")
            }
    )
    public Response findProductsByGatewayAccountId(@Parameter(example = "1") @PathParam("gatewayAccountId") Integer gatewayAccountId,
                                                   @Parameter(example = "DEMO", schema = @Schema(implementation = ProductType.class)) @QueryParam("type") String type) {
        if (type != null) {
            return requestValidator.validateProductType(type)
                    .map(errors -> Response.status(Response.Status.BAD_REQUEST).entity(errors).build())
                    .orElseGet(() -> {
                        logger.info("Searching for products with gatewayAccountId and type - [ {}, {} ]", gatewayAccountId, type);
                        List<Product> products = productFactory.productFinder().findByGatewayAccountIdAndType(gatewayAccountId, ProductType.valueOf(type));
                        return Response.status(OK).entity(products).build();
                    });
        }

        logger.info("Searching for products with gatewayAccountId - [ {} ]", gatewayAccountId);
        List<Product> products = productFactory.productFinder().findByGatewayAccountId(gatewayAccountId);
        return Response.status(OK).entity(products).build();
    }

    @GET
    @Path("/v1/api/products")
    @Produces(APPLICATION_JSON)
    @Operation(
            tags = {"Products"},
            summary = "Find product by service name path and product name path",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Product.class))),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    public Response findProductByProductPath(
            @Parameter(example = "some-awesome-government-service") @QueryParam("serviceNamePath") String serviceNamePath,
            @Parameter(example = "name-for-product") @QueryParam("productNamePath") String productNamePath) {
        return productFactory.productFinder().findByProductPath(serviceNamePath, productNamePath)
                .map(product -> Response.status(OK).entity(product).build())
                .orElseGet(() -> Response.status(NOT_FOUND).build());
    }

    @GET
    @Path("/v1/api/stats/products")
    @Produces(APPLICATION_JSON)
    @Operation(
            tags = {"Products"},
            summary = "Get usage stats of non-prototype payment links for a gateway account",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductUsageStat.class))))
            }
    )
    public Response findProductsAndStats(@Parameter(example = "1") @QueryParam("gatewayAccountId") Integer gatewayAccountId, 
            @Parameter(example = "false") @QueryParam("used") Boolean used) {
        logger.info(
                format("Listing usage stats on all non-prototype payment links for gateway account [%s=%s]", GATEWAY_ACCOUNT_ID, gatewayAccountId),
                kv(GATEWAY_ACCOUNT_ID, gatewayAccountId)
        );
        List<ProductUsageStat> usageStats = used == null || used ?
                productFactory.productFinder().findProductsAndUsage(gatewayAccountId): 
                productFactory.productFinder().findUnusedProducts(gatewayAccountId);
                        
        return Response.status(OK).entity(usageStats).build();
    }

    @POST
    @Path("/v1/api/products/{productExternalId}/regenerate-api-token")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(
            tags = {"Products"},
            description = "Gets a new API token from Public Auth application and replaces the old API token with the new token if the product exists",
            summary = "Regenerate API token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Not found"),
            }
    )
    public Response regenerateProductApiToken(@Parameter(example = "874h5c87834659q345698495") @PathParam("productExternalId") String productExternalId) {
        return productFactory.productFinder().findByExternalId(productExternalId).map((product) -> {
            productApiTokenManager.replaceApiTokenForAProduct(product, productApiTokenManager.getNewApiTokenFromPublicAuth(product));
            return Response.ok().build();
        }).orElseGet(() -> Response.status(NOT_FOUND).build());
    }
}
