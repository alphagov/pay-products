package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static uk.gov.pay.products.resources.ChargeResource.CHARGES_PATH;

@Path(CHARGES_PATH)
public class ChargeResource {

    private static Logger logger = LoggerFactory.getLogger(ChargeResource.class);

    static final String CHARGES_PATH = "/v1/api/charges";

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response createCharge(JsonNode jsonNode) {
        logger.info("Create a Charge for Product with payload - [ {} ]", jsonNode);

        return Response.status(BAD_REQUEST).build();
    }
}
