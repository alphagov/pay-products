package uk.gov.pay.products.auth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.config.ProductsConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenAuthenticatorTest {

    @Mock
    ProductsConfiguration configuration;
    TokenAuthenticator tokenAuthenticator;

    @Before
    public void before() throws Exception {
        when(configuration.getApiToken()).thenReturn("api-token-1");
        when(configuration.getApiToken2()).thenReturn("api-token-2");
        tokenAuthenticator = new TokenAuthenticator(configuration);
    }

    @Test
    public void shouldSuccess_whenAuthenticating_withValidApiKey() throws Exception {
        assertThat(tokenAuthenticator.authenticate("api-token-1").isPresent(), is(true));
        assertThat(tokenAuthenticator.authenticate("api-token-2").isPresent(), is(true));
    }

    @Test
    public void shouldFail_whenAuthenticating_withInvalidApiKey() throws Exception {
        assertThat(tokenAuthenticator.authenticate("invalid-token").isPresent(), is(false));
    }
}
