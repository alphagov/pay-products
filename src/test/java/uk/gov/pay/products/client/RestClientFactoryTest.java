package uk.gov.pay.products.client;

import io.dropwizard.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.pay.products.config.RestClientConfiguration;

import javax.net.ssl.SSLContext;
import jakarta.ws.rs.client.Client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RestClientFactoryTest {
    private static final Duration FIFTY_SECONDS = Duration.seconds(50);

    @Mock
    private RestClientConfiguration clientConfiguration;

    @BeforeEach
    public void setUp() {
        when(clientConfiguration.getConnectTimeout()).thenReturn(FIFTY_SECONDS);
        when(clientConfiguration.getReadTimeout()).thenReturn(FIFTY_SECONDS);
    }

    @Test
    public void jerseyClient_shouldUseSSLWhenSecureInternalCommunicationIsOn() {
        when(clientConfiguration.isDisabledSecureConnection()).thenReturn(false);

        Client client = RestClientFactory.buildClient(clientConfiguration);

        SSLContext sslContext = client.getSslContext();
        assertThat(sslContext.getProtocol(), is("TLSv1.2"));
    }

    @Test
    public void jerseyClient_shouldNotUseSSLWhenSecureInternalCommunicationIsOff() {
        when(clientConfiguration.isDisabledSecureConnection()).thenReturn(true);

        Client client = RestClientFactory.buildClient(clientConfiguration);

        assertThat(client.getSslContext().getProtocol(), is(not("TLSv1.2")));
    }

    @Test
    public void jerseyClient_shouldHaveConnectTimeoutSetTo10s() {
        Duration connectTimeout = Duration.seconds(10L);
        when(clientConfiguration.getConnectTimeout()).thenReturn(connectTimeout);

        Client client = RestClientFactory.buildClient(clientConfiguration);

        assertThat(client.getConfiguration().getProperty("jersey.config.client.connectTimeout"),
                is(Math.toIntExact(connectTimeout.getUnit().toMillis(connectTimeout.getQuantity()))));
    }

    @Test
    public void jerseyClient_shouldHaveReadTimeoutSetTo15s() {
        Duration readTimeout = Duration.seconds(15L);
        when(clientConfiguration.getReadTimeout()).thenReturn(readTimeout);

        Client client = RestClientFactory.buildClient(clientConfiguration);

        assertThat(client.getConfiguration().getProperty("jersey.config.client.readTimeout"),
                is(Math.toIntExact(readTimeout.getUnit().toMillis(readTimeout.getQuantity()))));
    }
}
