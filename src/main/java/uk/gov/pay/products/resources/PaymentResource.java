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

@Path("/")
public class PaymentResource {

    private static Logger logger = LoggerFactory.getLogger(PaymentResource.class);

    private static final String API_VERSION_PATH = "v1";
    public static final String PAYMENTS_RESOURCE = API_VERSION_PATH + "/api/payments";
    public static final String PAYMENTS_RESOURCE_PAYMENT = PAYMENTS_RESOURCE + "/{paymentExternalId}";
    public static final String PAYMENTS_RESOURCE_PRODUCT_PAYMENTS = API_VERSION_PATH + "/api/products/{productId}/payments";


    private final PaymentFactory paymentFactory;
    private final ProductFactory productFactory;

    @Inject
    public PaymentResource(PaymentFactory paymentFactory, ProductFactory productFactory) {
        this.paymentFactory = paymentFactory;
        this.productFactory = productFactory;
    }

    @Path(PAYMENTS_RESOURCE_PAYMENT)
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

    @Path(PAYMENTS_RESOURCE_PRODUCT_PAYMENTS)
    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response createPaymentByProductExternalId(@PathParam("productId") String productExternalId) {
        logger.info("Create a payment for product id - [ {} ]", productExternalId);
        Payment payment = paymentFactory.paymentCreator().doCreate(productExternalId);
        return Response.status(CREATED).entity(payment).build();
    }

    @Path(PAYMENTS_RESOURCE_PRODUCT_PAYMENTS)
    @GET
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response findPaymentsByProductExternalId(@PathParam("productId") String productExternalId) {
        logger.info("Find a list of payments for product id - [ {} ]", productExternalId);
        Optional<Integer> productMaybe = productFactory.productFinder().findProductIdByExternalId(productExternalId);
        return productMaybe
                .map(product -> {
                    List<Payment> payments = paymentFactory.paymentFinder().findByProductId(product);
                    return payments.size() > 0
                            ? Response.status(OK).entity(payments).build()
                            : Response.status(NOT_FOUND).build();
                })
                .orElseGet(() -> Response.status(NOT_FOUND).build());
    }
}
