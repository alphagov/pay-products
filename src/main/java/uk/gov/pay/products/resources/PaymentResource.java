package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
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

@Path("/v1/api")
public class PaymentResource {

    private static Logger logger = LoggerFactory.getLogger(PaymentResource.class);

    private final PaymentFactory paymentFactory;
    private final PaymentRequestValidator requestValidator;

    @Inject
    public PaymentResource(PaymentFactory paymentFactory, PaymentRequestValidator requestValidator) {
        this.paymentFactory = paymentFactory;
        this.requestValidator = requestValidator;
    }

    @Path("/payments/{paymentExternalId}")
    @GET
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response findPaymentByExternalId(@PathParam("paymentExternalId") String paymentExternalId) {
        logger.info("Find a payment with externalId - [ {} ]", paymentExternalId);
        return paymentFactory.paymentFinder().findByExternalId(paymentExternalId)
                .map(payment ->
                        Response.status(OK).entity(payment).build())
                .orElseGet(() ->
                        Response.status(NOT_FOUND).build());
    }

    @Path("/products/{productExternalId}/payments")
    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response createPayment(@PathParam("productExternalId") String productExternalId, JsonNode payload) {
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

    @Path("/products/{productExternalId}/payments")
    @GET
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response findPaymentsByProductExternalId(@PathParam("productExternalId") String productExternalId) {
        logger.info("Find a list of payments for product id - [ {} ]", productExternalId);
        List<Payment> payments = paymentFactory.paymentFinder().findByProductExternalId(productExternalId);
        return payments.size() > 0 ? Response.status(OK).entity(payments).build() : Response.status(NOT_FOUND).build();
    }

    @Path("/payments/{gatewayAccountId}/{referenceNumber}")
    @GET
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response findPaymentsByGatewayAccountIdAndReferenceNumber(@PathParam("gatewayAccountId") Integer gatewayAccountNumber,  @PathParam("referenceNumber") String referenceNumber) {
        logger.info(format("Find a payments for gateway account and reference number - [ %s %s ]", gatewayAccountNumber, referenceNumber));
        return paymentFactory.paymentFinder().findByGatewayAccountIdAndReferenceNumber(gatewayAccountNumber, referenceNumber)
                .map(payment ->
                        Response.status(OK).entity(payment).build())
                .orElseGet(() ->
                        Response.status(NOT_FOUND).build());
    }
}
