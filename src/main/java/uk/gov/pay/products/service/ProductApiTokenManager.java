package uk.gov.pay.products.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.client.publicauth.model.CreateApiTokenRequest;
import uk.gov.pay.products.client.publicauth.model.NewApiTokenFromPublicAuthResponse;
import uk.gov.pay.products.config.ProductsConfiguration;
import uk.gov.pay.products.exception.FailToGetNewApiTokenException;
import uk.gov.pay.products.exception.FailToReplaceApiTokenException;
import uk.gov.pay.products.model.Product;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static uk.gov.pay.commons.model.TokenPaymentType.CARD;
import static uk.gov.pay.products.client.publicauth.model.TokenSource.PRODUCTS;

public class ProductApiTokenManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductApiTokenManager.class);
    public static final String NEW_API_TOKEN_PATH = "/v1/frontend/auth";

    private final Client client;
    private final String publicAuthUrl;
    private final String emailAddressForReplacingApiTokens;
    private final ProductFactory productFactory;

    @Inject
    public ProductApiTokenManager(Client client, ProductsConfiguration configuration, ProductFactory productFactory) {
        this.client = client;
        this.emailAddressForReplacingApiTokens = configuration.getEmailAddressForReplacingApiTokens();
        this.publicAuthUrl = configuration.getPublicAuthUrl();
        this.productFactory = productFactory;
    }

    public String getNewApiTokenFromPublicAuth(Product product) {
        CreateApiTokenRequest tokenRequest = createApiTokenRequest(product);

        Response response = client.target(publicAuthUrl + NEW_API_TOKEN_PATH)
                .request()
                .post(Entity.entity(tokenRequest, MediaType.APPLICATION_JSON));

        return Optional.ofNullable(response.readEntity(NewApiTokenFromPublicAuthResponse.class))
                .map(NewApiTokenFromPublicAuthResponse::getToken)
                .orElseThrow(() -> new FailToGetNewApiTokenException(
                        String.format("Failed to get a new API token for product %s of type %s",
                                product.getExternalId(),
                                product.getType())));
    }

    public void replaceApiTokenForAProduct(Product product, String newApiToken) {
        Product modifiedProduct = productFactory.productFinder().updatePayApiTokenByExternalId(product.getExternalId(), newApiToken)
                .orElseThrow(() -> new FailToReplaceApiTokenException(
                        String.format("Failed to replace API token for product %s of type %s",
                                product.getExternalId(), product.getType())));

        LOGGER.info(String.format("Regenerated API token for product %s of type %s",
                modifiedProduct.getExternalId(), modifiedProduct.getType()));
    }

    private CreateApiTokenRequest createApiTokenRequest(Product product) {
        return new CreateApiTokenRequest(
                String.valueOf(product.getGatewayAccountId()),
                getDescription(product),
                emailAddressForReplacingApiTokens,
                CARD,
                PRODUCTS);
    }

    private String getDescription(Product product) {
        switch (product.getType()) {
            case ADHOC:
                return String.format("Token for \"%s\" payment link", product.getName());
            case PROTOTYPE:
                return String.format("Token for Prototype: %s", product.getName());
            case DEMO:
                return "Token for Demo Payment";
            default:
                throw new IllegalArgumentException(
                        String.format("Product %s has an invalid product type [%s]",
                                product.getExternalId(), product.getType().toString()));
        }
    }
}
