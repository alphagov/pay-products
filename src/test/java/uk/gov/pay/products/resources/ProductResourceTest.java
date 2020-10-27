package uk.gov.pay.products.resources;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.client.publicauth.TokenResponse;
import uk.gov.pay.products.config.ProductsConfiguration;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.service.ProductFactory;
import uk.gov.pay.products.service.ProductFinder;
import uk.gov.pay.products.util.ProductType;
import uk.gov.pay.products.validations.ProductRequestValidator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

@RunWith(MockitoJUnitRunner.class)
public class ProductResourceTest {

    private static final String PUBLICAUTH_URL = "https://publicauth.url";
    private ProductResource productResource;
    
    @Mock
    private ProductRequestValidator productRequestValidator;

    @Mock
    private ProductFactory productFactory;
    
    @Mock
    private Client client;
    
    @Mock
    private ProductsConfiguration productsConfiguration;

    @Before
    public void setUp() {
        when(productsConfiguration.getEmailAddress()).thenReturn("pay-products@gov.uk");
        when(productsConfiguration.getPublicAuthUrl()).thenReturn(PUBLICAUTH_URL);

        productResource = new ProductResource(productRequestValidator, productFactory, client, productsConfiguration);
    }
    
    @Test
    public void shouldReturn500_whenFailingToUpdateApiKeyForAPaymentLink() {
        String externalId = randomUuid();
        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withType(ProductType.ADHOC)
                .build()
                .toProduct();

        ProductFinder productFinder = setUpProductFinderToReturnAProductUsingExternalId(externalId, product);
        Response publicAuthResponse = setUpPublicAuthToReturnSuccessfulResponse();
        TokenResponse tokenResponse = new TokenResponse("New Pay API token");
        when(publicAuthResponse.readEntity(TokenResponse.class)).thenReturn(tokenResponse);
        when(productFinder.updatePayApiTokenByExternalId(externalId, tokenResponse.getToken())).thenReturn(Optional.empty());

        Response response = productResource.regenerateProductApiKey(externalId);

        assertThat(response.getStatus(), is(500));
    }

    private Response setUpPublicAuthToReturnSuccessfulResponse() {
        WebTarget webTarget = mock(WebTarget.class);
        when(client.target(PUBLICAUTH_URL)).thenReturn(webTarget);
        Builder builder = mock(Builder.class);
        when(webTarget.request()).thenReturn(builder);
        Response publicAuthResponse = mock(Response.class);
        when(builder.post(any(Entity.class))).thenReturn(publicAuthResponse);
        return publicAuthResponse;
    }

    @NotNull
    private ProductFinder setUpProductFinderToReturnAProductUsingExternalId(String externalId, Product product) {
        ProductFinder productFinder = mock(ProductFinder.class);
        when(productFactory.productFinder()).thenReturn(productFinder);
        when(productFinder.findByExternalId(externalId)).thenReturn(Optional.of(product));
        return productFinder;
    }
}
