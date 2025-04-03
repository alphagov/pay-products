package uk.gov.pay.products.client;

import uk.gov.pay.products.config.RestClientConfiguration;
import uk.gov.service.payments.logging.RestClientLoggingFilter;

import javax.net.ssl.SSLContext;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static java.lang.String.format;

public class RestClientFactory {
    private static final String TLSV1_2 = "TLSv1.2";

    public static Client buildClient(RestClientConfiguration clientConfig) {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        clientBuilder.connectTimeout(clientConfig.getConnectTimeout().getQuantity(), clientConfig.getConnectTimeout().getUnit());
        clientBuilder.readTimeout(clientConfig.getReadTimeout().getQuantity(), clientConfig.getReadTimeout().getUnit());

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
