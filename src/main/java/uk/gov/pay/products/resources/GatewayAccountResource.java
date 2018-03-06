package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import io.dropwizard.jersey.PATCH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.model.PatchRequest;
import uk.gov.pay.products.service.GatewayAccountFactory;
import uk.gov.pay.products.validations.GatewayAccountRequestValidator;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Path("/v1/api")
public class GatewayAccountResource {

    private static Logger logger = LoggerFactory.getLogger(GatewayAccountResource.class);

    private final GatewayAccountRequestValidator requestValidator;
    private final GatewayAccountFactory gatewayAccountFactory;

    @Inject
    public GatewayAccountResource(GatewayAccountRequestValidator requestValidator, GatewayAccountFactory gatewayAccountFactory) {
        this.requestValidator = requestValidator;
        this.gatewayAccountFactory = gatewayAccountFactory;
    }

    @PATCH
    @Path("/gateway-account/{gatewayAccountId}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response patchGatewayAccount(@PathParam("gatewayAccountId") Integer gatewayAccountId, JsonNode payload) {
        logger.info("Patching gateway account [ {} ]", gatewayAccountId);
        return requestValidator.validatePatchRequest(payload)
                .map(errors -> Response.status(BAD_REQUEST).entity(errors).build())
                .orElseGet(() -> {
                            Boolean success = gatewayAccountFactory.getUpdateService()
                                    .doPatch(gatewayAccountId, PatchRequest.from(payload));
                            if (success) {
                                return Response.ok().build();
                            }
                            return Response.status(ACCEPTED).build();
                        }
                );
    }
}
