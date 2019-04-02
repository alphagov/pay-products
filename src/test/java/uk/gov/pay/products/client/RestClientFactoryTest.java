package uk.gov.pay.products.client;

import org.junit.Test;
import uk.gov.pay.products.config.RestClientConfiguration;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestClientFactoryTest {

    @Test
    public void jerseyClient_shouldUseSSLWhenSecureInternalCommunicationIsOn() {
        //given
        RestClientConfiguration clientConfiguration = mock(RestClientConfiguration.class);
        when(clientConfiguration.isDisabledSecureConnection()).thenReturn(false);

        //when
        Client client = RestClientFactory.buildClient(clientConfiguration);

        //then
        SSLContext sslContext = client.getSslContext();
        assertThat(sslContext.getProtocol(), is("TLSv1.2"));

    }

    @Test
    public void jerseyClient_shouldNotUseSSLWhenSecureInternalCommunicationIsOff() {
        //given
        RestClientConfiguration clientConfiguration = mock(RestClientConfiguration.class);
        when(clientConfiguration.isDisabledSecureConnection()).thenReturn(true);

        //when
        Client client = RestClientFactory.buildClient(clientConfiguration);

        //then
        assertThat(client.getSslContext().getProtocol(), is(not("TLSv1.2")));
    }
}
