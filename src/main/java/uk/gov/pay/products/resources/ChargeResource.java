package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.model.Charge;
import uk.gov.pay.products.service.charge.ChargesFactory;
import uk.gov.pay.products.validations.ChargeRequestValidator;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static uk.gov.pay.products.resources.ChargeResource.CHARGES_PATH;

@Path(CHARGES_PATH)
public class ChargeResource {

    private static Logger logger = LoggerFactory.getLogger(ChargeResource.class);

    static final String CHARGES_PATH = "/v1/api/charges";
    private ChargeRequestValidator requestValidator;
    private ChargesFactory chargesFactory;

    @Inject
    public ChargeResource(ChargeRequestValidator requestValidator, ChargesFactory chargesFactory) {
        this.requestValidator = requestValidator;
        this.chargesFactory = chargesFactory;
    }

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response createCharge(JsonNode jsonNode) {
        logger.info("Create a Charge for Product with payload - [ {} ]", jsonNode);
        return requestValidator.validateCreateRequest(jsonNode)
                .map(errors -> Response.status(BAD_REQUEST).entity(errors).build())
                .orElseGet(() -> chargesFactory.chargesCreator().doCreate(Charge.fromPayload(jsonNode))
                        .map(charge -> Response.status(OK).entity(charge).build())
                        .orElseGet(() -> Response.status(NOT_FOUND).build()));
    }
}
