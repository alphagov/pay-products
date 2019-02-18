package uk.gov.pay.products.client.publicapi;

import com.google.inject.Inject;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.exception.PublicApiResponseErrorException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Optional;

import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

public class PublicApiRestClient {
    private static final Logger logger = LoggerFactory.getLogger(PublicApiRestClient.class);

    private static final String PAYMENTS_PATH = "/v1/payments";
    private static final String PAYMENT_PATH = PAYMENTS_PATH + "/%s";

    private final Client client;
    private String publicApiUrl;

    @Inject
    public PublicApiRestClient(Client client, String publicApiUrl) {
        this.client = client;
        this.publicApiUrl = publicApiUrl;
    }

    public PaymentResponse createPayment(String apiToken, PaymentRequest paymentRequest) {
        logger.info("Public API client requested creation of payment - [ {} ]", paymentRequest);

        Response response = client
                .target(buildAbsoluteUrl(PAYMENTS_PATH))
                .request()
                .header(AUTHORIZATION, constructBearerToken(apiToken))
                .post(Entity.entity(paymentRequest, MediaType.APPLICATION_JSON));

        if (response.getStatus() == HttpStatus.CREATED_201) {
            PaymentResponse paymentResponse = response.readEntity(PaymentResponse.class);
            logger.info("Public API client returned payment created - [ {} ]", paymentResponse);
            return paymentResponse;
        }

        PublicApiResponseErrorException publicApiResponseErrorException = new PublicApiResponseErrorException(response);
        logger.error("Public API client returned an error - [ {} ]", publicApiResponseErrorException.getMessage());
        throw publicApiResponseErrorException;
    }

    public Optional<PaymentResponse> getPayment(String apiToken, String paymentId) {
        logger.info("Public API client requested finding payment - [ {} ]", paymentId);

        Response response = client
                .target(buildAbsoluteUrl(format(PAYMENT_PATH, paymentId)))
                .request()
                .header(AUTHORIZATION, constructBearerToken(apiToken))
                .get();

        if (response.getStatus() == HttpStatus.OK_200) {
            PaymentResponse paymentResponse = response.readEntity(PaymentResponse.class);
            logger.info("Public API client returned payment found - [ {} ]", paymentResponse);
            return Optional.of(paymentResponse);
        }

        if (response.getStatus() == HttpStatus.NOT_FOUND_404) {
            logger.info("Public API client returned payment not found");
            return Optional.empty();
        }

        PublicApiResponseErrorException publicApiResponseErrorException = new PublicApiResponseErrorException(response);
        logger.error("Public API client returned an error - [ {} ]", publicApiResponseErrorException.getMessage());
        throw publicApiResponseErrorException;
    }

    private String buildAbsoluteUrl(String relativeUrl) {
        return UriBuilder
                .fromPath(publicApiUrl)
                .path(relativeUrl)
                .toString();
    }

    private String constructBearerToken(String apiToken) {
        return "Bearer " + apiToken;
    }

}
