package uk.gov.pay.products.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.client.publicauth.model.CreateApiTokenRequest;
import uk.gov.pay.products.client.publicauth.model.NewApiTokenFromPublicAuthResponse;
import uk.gov.pay.products.client.publicauth.model.TokenSource;
import uk.gov.pay.products.config.ProductsConfiguration;
import uk.gov.pay.products.exception.FailToGetNewApiTokenException;
import uk.gov.pay.products.exception.FailToReplaceApiTokenException;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.util.ProductType;
import uk.gov.service.payments.commons.model.TokenPaymentType;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.pay.products.service.ProductApiTokenManager.NEW_API_TOKEN_PATH;
import static uk.gov.pay.products.utils.TestHelpers.verifyLog;

@ExtendWith(MockitoExtension.class)
public class ProductApiTokenManagerTest {
    private static final String PUBLICAUTH_URL = "https://publicauth.url";
    private static final String EMAIL_ADDRESS_FOR_REPLACING_API_TOKENS = "pay-products@gov.uk";
    private static final String NEW_API_TOKEN_FOR_DEMO_PAYMENT = "New API token for demo payment";
    private static final String DEMO_PAYMENT_DESCRIPTION = "Token for Demo Payment";
    private static final Product DEMO_PAYMENT = ProductEntityFixture.aProductEntity().build().toProduct();

    private ProductApiTokenManager productApiTokenManager;

    @Mock
    private Client client;

    @Mock
    private ProductsConfiguration productsConfiguration;

    @Mock
    private ProductFactory productFactory;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor;

    @BeforeEach
    public void setUp() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
        logger.setLevel(Level.INFO);

        when(productsConfiguration.getEmailAddressForReplacingApiTokens()).thenReturn(EMAIL_ADDRESS_FOR_REPLACING_API_TOKENS);
        when(productsConfiguration.getPublicAuthUrl()).thenReturn(PUBLICAUTH_URL);

        productApiTokenManager = new ProductApiTokenManager(client, productsConfiguration, productFactory);
    }

    @Test
    public void shouldReturnANewApiTokenForDemoPaymentWhenPublicAuthGeneratesTheToken() {
        setUpPublicAuthToReturnResponse(DEMO_PAYMENT, NEW_API_TOKEN_FOR_DEMO_PAYMENT, DEMO_PAYMENT_DESCRIPTION);

        String apiToken = productApiTokenManager.getNewApiTokenFromPublicAuth(DEMO_PAYMENT);

        assertThat(apiToken, is(NEW_API_TOKEN_FOR_DEMO_PAYMENT));
    }

    @Test
    public void shouldReturnANewApiTokenForPaymentLinkWhenPublicAuthGeneratesTheToken() {
        final String newApiToken = "New API token for payment link";
        Product paymentLink = ProductEntityFixture.aProductEntity()
                .withType(ProductType.ADHOC)
                .build()
                .toProduct();
        setUpPublicAuthToReturnResponse(paymentLink, newApiToken, String.format("Token for \"%s\" payment link", paymentLink.getName()));

        String apiToken = productApiTokenManager.getNewApiTokenFromPublicAuth(paymentLink);

        assertThat(apiToken, is(newApiToken));
    }

    @Test
    public void shouldReturnANewApiTokenForPrototypeLinkWhenPublicAuthGeneratesTheToken() {
        final String newApiToken = "New API token for prototype link";
        Product prototypeLink = ProductEntityFixture.aProductEntity()
                .withType(ProductType.PROTOTYPE)
                .build()
                .toProduct();
        setUpPublicAuthToReturnResponse(prototypeLink, newApiToken, String.format("Token for Prototype: %s", prototypeLink.getName()));

        String apiToken = productApiTokenManager.getNewApiTokenFromPublicAuth(prototypeLink);

        assertThat(apiToken, is(newApiToken));
    }

    @Test
    public void shouldThrowExceptionWhenPublicAuthFailsToGenerateAnApiToken() {
        setUpPublicAuthToReturnResponse(DEMO_PAYMENT, null, DEMO_PAYMENT_DESCRIPTION);

        FailToGetNewApiTokenException e = assertThrows(FailToGetNewApiTokenException.class,
                () -> productApiTokenManager.getNewApiTokenFromPublicAuth(DEMO_PAYMENT));

        assertThat(e.getMessage(),
                is(String.format("Failed to get a new API token for product %s of type %s", DEMO_PAYMENT.getExternalId(), DEMO_PAYMENT.getType())));
    }

    @Test
    public void shouldReplaceApiTokenForAProductWhichExists() {
        Product modifiedDemoPayment = ProductEntityFixture.aProductEntity().withApiToken(NEW_API_TOKEN_FOR_DEMO_PAYMENT).build().toProduct();
        ProductFinder productFinder = mock(ProductFinder.class);
        when(productFactory.productFinder()).thenReturn(productFinder);
        when(productFinder.updatePayApiTokenByExternalId(DEMO_PAYMENT.getExternalId(), NEW_API_TOKEN_FOR_DEMO_PAYMENT)).thenReturn(Optional.of(modifiedDemoPayment));

        productApiTokenManager.replaceApiTokenForAProduct(DEMO_PAYMENT, NEW_API_TOKEN_FOR_DEMO_PAYMENT);

        verifyLog(mockAppender, loggingEventArgumentCaptor, 1,
                String.format("Regenerated API token for product %s of type %s", modifiedDemoPayment.getExternalId(), modifiedDemoPayment.getType()));
    }

    @Test
    public void shouldThrowExceptionWhenReplacingApiTokenForAProductWhichDoesNotExist() {
        ProductFinder productFinder = mock(ProductFinder.class);
        when(productFactory.productFinder()).thenReturn(productFinder);
        when(productFinder.updatePayApiTokenByExternalId(DEMO_PAYMENT.getExternalId(), NEW_API_TOKEN_FOR_DEMO_PAYMENT)).thenReturn(Optional.empty());

        FailToReplaceApiTokenException e = assertThrows(FailToReplaceApiTokenException.class,
                () -> productApiTokenManager.replaceApiTokenForAProduct(DEMO_PAYMENT, NEW_API_TOKEN_FOR_DEMO_PAYMENT));

        assertThat(e.getMessage(),
                is(String.format("Failed to replace API token for product %s of type %s", DEMO_PAYMENT.getExternalId(), DEMO_PAYMENT.getType())));
    }

    private void setUpPublicAuthToReturnResponse(Product product,
                                                 String expectedNewApiToken,
                                                 String description) {
        WebTarget webTarget = mock(WebTarget.class);
        when(client.target(PUBLICAUTH_URL + NEW_API_TOKEN_PATH)).thenReturn(webTarget);
        Invocation.Builder builder = mock(Invocation.Builder.class);
        when(webTarget.request()).thenReturn(builder);
        Response publicAuthResponse = mock(Response.class);
        CreateApiTokenRequest request = new CreateApiTokenRequest(
                product.getGatewayAccountId().toString(),
                description,
                EMAIL_ADDRESS_FOR_REPLACING_API_TOKENS,
                TokenPaymentType.CARD,
                TokenSource.PRODUCTS);
        when(builder.post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))).thenReturn(publicAuthResponse);
        NewApiTokenFromPublicAuthResponse newApiTokenFromPublicAuthResponse = new NewApiTokenFromPublicAuthResponse(expectedNewApiToken);
        when(publicAuthResponse.readEntity(NewApiTokenFromPublicAuthResponse.class)).thenReturn(newApiTokenFromPublicAuthResponse);
    }
}
