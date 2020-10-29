package uk.gov.pay.products.client;

import uk.gov.pay.logging.RestClientLoggingFilter;
import uk.gov.pay.products.config.RestClientConfiguration;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static java.lang.String.format;

public class RestClientFactory {
    private static final String TLSV1_2 = "TLSv1.2";

    public static Client buildClient(RestClientConfiguration clientConfig) {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();

        if (!clientConfig.isDisabledSecureConnection()) {
            try {
                SSLContext sslContext = SSLContext.getInstance(TLSV1_2);
                sslContext.init(null, null, null);
                clientBuilder = clientBuilder.sslContext(sslContext);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(format("Unable to create an SSL context for %s", TLSV1_2), e);
            }
        }

        Client client = clientBuilder.build();
        client.register(RestClientLoggingFilter.class);
        return client;
    }

    private RestClientFactory() {
    }
}
