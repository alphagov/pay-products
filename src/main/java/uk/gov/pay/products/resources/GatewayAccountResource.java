package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import io.dropwizard.jersey.PATCH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.model.GatewayAccountRequest;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.service.GatewayAccountFactory;
import uk.gov.pay.products.validations.GatewayAccountRequestValidator;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/")
public class GatewayAccountResource {

    private static Logger logger = LoggerFactory.getLogger(GatewayAccountResource.class);

    private static final String API_VERSION_PATH = "v1";
    public static final String GATEWAY_ACCOUNT_RESOURCE_PATH = API_VERSION_PATH + "/api/gateway-account";
    public static final String GATEWAY_ACCOUNT_API_PATH = GATEWAY_ACCOUNT_RESOURCE_PATH + "/{gatewayAccountId}";
    private final GatewayAccountRequestValidator requestValidator;
    private final GatewayAccountFactory gatewayAccountFactory;

    @Inject
    public GatewayAccountResource(GatewayAccountRequestValidator requestValidator, GatewayAccountFactory gatewayAccountFactory) {
        this.requestValidator = requestValidator;
        this.gatewayAccountFactory = gatewayAccountFactory;
    }

    @PATCH
    @Path(GATEWAY_ACCOUNT_API_PATH)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @PermitAll
    public Response patchGatewayAccount(@PathParam("gatewayAccountId") Integer gatewayAccountId, JsonNode payload) {
        logger.info("Patching gateway account [ {} ]", gatewayAccountId);
        return requestValidator.validatePatchRequest(payload)
                .map(errors -> Response.status(BAD_REQUEST).entity(errors).build())
                .orElseGet(() -> {
                            List<Product> products = gatewayAccountFactory.getUpdateService()
                                    .doPatch(gatewayAccountId, GatewayAccountRequest.from(payload));
                            if (products.isEmpty()) {
                                return Response.status(NOT_FOUND).build();
                            }
                            return Response.ok().build();
                        }
                );
    }
}
