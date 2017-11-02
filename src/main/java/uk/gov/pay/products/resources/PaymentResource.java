package uk.gov.pay.products.resources;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.service.PaymentFactory;
import uk.gov.pay.products.service.ProductFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;
import static uk.gov.pay.products.resources.ProductResource.PRODUCT_RESOURCE_PATH;

@Path("/")
public class PaymentResource {

    private static Logger logger = LoggerFactory.getLogger(PaymentResource.class);

    private static final String API_VERSION_PATH = "v1";
    public static final String PAYMENTS_RESOURCE_PATH = API_VERSION_PATH + "/api/payments";
    public static final String PAYMENT_RESOURCE_PATH = PAYMENTS_RESOURCE_PATH + "/{paymentExternalId}";
    public static final String PRODUCT_PAYMENTS_RESOURCE_PATH = PRODUCT_RESOURCE_PATH + "/payments";

    private final PaymentFactory paymentFactory;
    private final ProductFactory productFactory;

    @Inject
    public PaymentResource(PaymentFactory paymentFactory, ProductFactory productFactory) {
        this.paymentFactory = paymentFactory;
        this.productFactory = productFactory;
    }

    @Path(PAYMENT_RESOURCE_PATH)
    @GET
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response findPaymentByExternalId(@PathParam("paymentExternalId") String paymentExternalId) {
        logger.info("Find a payment with externalId - [ {} ]", paymentExternalId);
        return paymentFactory.paymentFinder().findByExternalId(paymentExternalId)
                .map(payment ->
                        Response.status(OK).entity(payment).build())
                .orElseGet(() ->
                        Response.status(NOT_FOUND).build());
    }

    @Path(PRODUCT_PAYMENTS_RESOURCE_PATH)
    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response createPaymentByProductExternalId(@PathParam("productExternalId") String productExternalId) {
        logger.info("Create a payment for product id - [ {} ]", productExternalId);
        Payment payment = paymentFactory.paymentCreator().doCreate(productExternalId);
        return Response.status(CREATED).entity(payment).build();
    }

    @Path(PRODUCT_PAYMENTS_RESOURCE_PATH)
    @GET
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response findPaymentsByProductExternalId(@PathParam("productExternalId") String productExternalId) {
        logger.info("Find a list of payments for product id - [ {} ]", productExternalId);
        List<Payment> payments = paymentFactory.paymentFinder().findByProductExternalId(productExternalId);
        return payments.size() > 0 ? Response.status(OK).entity(payments).build() : Response.status(NOT_FOUND).build();
    }
}
