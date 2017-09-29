package uk.gov.pay.products.auth;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import uk.gov.pay.products.config.ProductsConfiguration;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class TokenAuthenticator implements Authenticator<String, Token> {

    private final List<String> validOAuthTokens;

    public TokenAuthenticator(ProductsConfiguration configuration) {
        ImmutableList.Builder<String> listBuilder = ImmutableList.builder();
        addIfNotNull(listBuilder, configuration.getApiToken());
        addIfNotNull(listBuilder, configuration.getApiToken2());
        validOAuthTokens = listBuilder.build();
    }

    @Override
    public Optional<Token> authenticate(String bearerToken) throws AuthenticationException {
        if (!isBlank(bearerToken)) {
            if (validOAuthTokens.contains(bearerToken.trim())) {
                return Optional.of(new Token("authenticated"));
            }
        }
        return Optional.empty();
    }

    private void addIfNotNull(ImmutableList.Builder<String> listBuilder, String apiToken) {
        if (!isBlank(apiToken)) {
            listBuilder.add(apiToken);
        }
    }
}
