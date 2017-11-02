package uk.gov.pay.products.stubs.publicapi;

import org.mockserver.client.server.ForwardChainExpectation;

import javax.json.JsonObject;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.mockserver.model.HttpResponse.response;

public class PublicApiStubExpectation {

    private ForwardChainExpectation expectation;

    public PublicApiStubExpectation(ForwardChainExpectation expectation) {
        this.expectation = expectation;
    }

    public void respondCreatedWithBody(JsonObject body) {
        expectation.respond(response()
                .withStatusCode(CREATED_201)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(body.toString()));
    }

    public void respondOkWithBody(JsonObject body) {
        expectation.respond(response()
                .withStatusCode(OK_200)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(body.toString()));
    }

    public void respondBadRequestWithBody(JsonObject body) {
        expectation.respond(response()
                .withStatusCode(BAD_REQUEST_400)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(body.toString()));
    }

    public void respondNotFound() {
        expectation.respond(response()
                .withStatusCode(NOT_FOUND_404)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON));
    }
}
