package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.service.PaymentFactory;
import uk.gov.pay.products.validations.PaymentRequestValidator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static net.logstash.logback.argument.StructuredArguments.kv;
import static uk.gov.service.payments.logging.LoggingKeys.PAYMENT_EXTERNAL_ID;

@Path("/")
@Tag(name = "Payments")
public class PaymentResource {

    private static final Logger logger = LoggerFactory.getLogger(PaymentResource.class);

    private final PaymentFactory paymentFactory;
    private final PaymentRequestValidator requestValidator;

    @Inject
    public PaymentResource(PaymentFactory paymentFactory, PaymentRequestValidator requestValidator) {
        this.paymentFactory = paymentFactory;
        this.requestValidator = requestValidator;
    }
    
    @Path("/v1/api/payments/redact-reference/{govukPaymentId}")
    @POST
    public Response redactReference(@PathParam("govukPaymentId") String govukPaymentId) {
        logger.info("Redacting reference for payment.", kv(PAYMENT_EXTERNAL_ID, govukPaymentId));
        paymentFactory.paymentUpdater().redactReference(govukPaymentId);
        return Response.ok().build();
    }

    @Path("/v1/api/payments/{paymentExternalId}")
    @GET
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(
            summary = "Find payment by payment external ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Payment.class))),
                    @ApiResponse(responseCode = "404", description = "Not found"),
            }
    )
    public Response findPaymentByExternalId(@Parameter(example = "h6347634cwb67wii7b6ciueroytw")
                                            @PathParam("paymentExternalId") String paymentExternalId) {
        logger.info("Find a payment with externalId - [ {} ]", paymentExternalId);
        return paymentFactory.paymentFinder().findByExternalId(paymentExternalId)
                .map(payment ->
                        Response.status(OK).entity(payment).build())
                .orElseGet(() ->
                        Response.status(NOT_FOUND).build());
    }

    @Path("/v1/api/products/{productExternalId}/payments")
    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(
            summary = "Creates new payment for a given product",
            responses = {
                    @ApiResponse(responseCode = "201", description = "OK", content = @Content(schema = @Schema(implementation = Payment.class))),
                    @ApiResponse(responseCode = "400", description = "For invalid payload"),
            }
    )
    public Response createPayment(@Parameter(example = "uier837y735n837475y3847534")
                                  @PathParam("productExternalId") String productExternalId,
                                  @RequestBody(content = @Content(schemaProperties =
                                          {
                                                  @SchemaProperty(
                                                          name = "price",
                                                          schema = @Schema(example = "9090", maximum = "10000000", type = "number", minimum = "1",
                                                                  description = "Price override for the payment amount. If not present this will defaults to price of product."))
                                          }))
                                          JsonNode payload) {
        logger.info("Create a payment for product id - [ {} ]", productExternalId);
        return requestValidator.validatePriceOverrideRequest(payload)
                .map(errors -> Response.status(Response.Status.BAD_REQUEST).entity(errors).build())
                .orElseGet(() -> {
                    Payment payment = paymentFactory.paymentCreator()
                            .doCreate(productExternalId, extractAmountIfAvailable(payload), extractReferenceIfAvailable(payload));
                    return Response.status(CREATED).entity(payment).build();
                });
    }

    private Long extractAmountIfAvailable(JsonNode payload) {
        if (payload == null || payload.get("price") == null) {
            return null;
        } else {
            return payload.get("price").asLong();
        }
    }

    private String extractReferenceIfAvailable(JsonNode payload) {
        if (payload == null || payload.get("reference_number") == null) {
            return null;
        }
        return payload.get("reference_number").asText();
    }

    @Path("/v1/api/products/{productExternalId}/payments")
    @GET
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(
            summary = "Find list of payments that belongs to the specified product external ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Payment.class)))),
                    @ApiResponse(responseCode = "404", description = "Not found"),
            }
    )
    public Response findPaymentsByProductExternalId(@Parameter(example = "uier837y735n837475y3847534") @PathParam("productExternalId") String productExternalId) {
        logger.info("Find a list of payments for product id - [ {} ]", productExternalId);
        List<Payment> payments = paymentFactory.paymentFinder().findByProductExternalId(productExternalId);
        return payments.size() > 0 ? Response.status(OK).entity(payments).build() : Response.status(NOT_FOUND).build();
    }

    @Path("/v1/api/payments/{gatewayAccountId}/{referenceNumber}")
    @GET
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(
            summary = "Find payment by gateway account ID and reference number.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Payment.class))),
                    @ApiResponse(responseCode = "404", description = "Not found"),
            }
    )
    public Response findPaymentsByGatewayAccountIdAndReferenceNumber(@Parameter(example = "1") @PathParam("gatewayAccountId") Integer gatewayAccountNumber,
                                                                     @Parameter(example = "RE4R2A6VAP") @PathParam("referenceNumber") String referenceNumber) {
        logger.info(format("Find a payments for gateway account and reference number - [ %s %s ]", gatewayAccountNumber, referenceNumber));
        return paymentFactory.paymentFinder().findByGatewayAccountIdAndReferenceNumber(gatewayAccountNumber, referenceNumber)
                .map(payment ->
                        Response.status(OK).entity(payment).build())
                .orElseGet(() ->
                        Response.status(NOT_FOUND).build());
    }
}
