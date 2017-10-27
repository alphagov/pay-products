package uk.gov.pay.products.resources;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.service.PaymentFactory;
import uk.gov.pay.products.service.ProductFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

@Path("/")
public class PaymentResource {

    private static Logger logger = LoggerFactory.getLogger(PaymentResource.class);

    private static final String API_VERSION_PATH = "v1";
    public static final String PAYMENTS_RESOURCE = API_VERSION_PATH + "/api/payments";
    public static final String PAYMENTS_RESOURCE_GET_PAYMENT = PAYMENTS_RESOURCE + "/{paymentExternalId}";
    public static final String PAYMENTS_RESOURCE_GET_PAYMENTS = API_VERSION_PATH + "/api/products/{productId}/payments";


    private final PaymentFactory paymentFactory;
    private final ProductFactory productFactory;

    @Path(PAYMENTS_RESOURCE_GET_PAYMENT)
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

    @Inject
    public PaymentResource(PaymentFactory paymentFactory, ProductFactory productFactory) {
        this.paymentFactory = paymentFactory;
        this.productFactory = productFactory;
    }

    @Path(PAYMENTS_RESOURCE_GET_PAYMENTS)
    @GET
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response findPaymentsByProductExternalId(@PathParam("productId") String productExternalId) {
        logger.info("Find a list of payments with product id - [ {} ]", productExternalId);
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
