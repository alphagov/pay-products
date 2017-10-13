package uk.gov.pay.products.client;

import org.glassfish.jersey.SslConfigurator;
import uk.gov.pay.products.filters.RestClientLoggingFilter;
import uk.gov.pay.products.config.RestClientConfiguration;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import static uk.gov.pay.products.util.TrustStoreLoader.getTrustStore;
import static uk.gov.pay.products.util.TrustStoreLoader.getTrustStorePassword;

public class RestClientFactory {
    public static final String TLSV1_2 = "TLSv1.2";

    public static Client buildClient(RestClientConfiguration clientConfig) {
        Client client;
        if (clientConfig.isDisabledSecureConnection()) {
            client = ClientBuilder.newBuilder().build();
        } else {
            SslConfigurator sslConfig = SslConfigurator.newInstance()
                    .trustStore(getTrustStore())
                    .trustStorePassword(getTrustStorePassword())
                    .securityProtocol(TLSV1_2);

            SSLContext sslContext = sslConfig.createSSLContext();
            client = ClientBuilder.newBuilder().sslContext(sslContext).build();
        }
        client.register(RestClientLoggingFilter.class);
        return client;
    }

    private RestClientFactory() {
    }
}
