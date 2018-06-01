package uk.gov.pay.products.matchers;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.util.NumberUtils;

public class ProductMatcher {
    public static Matcher<Product> isSame(final Product expectedProduct) {
        return new BaseMatcher<Product>() {
            @Override
            public boolean matches(final Object obj) {
                final Product actualProduct = (Product) obj;

                return ((actualProduct != null) &&
                        (expectedProduct != null) &&
                        StringUtils.equals(actualProduct.getExternalId(), expectedProduct.getExternalId()) &&
                        StringUtils.equals(actualProduct.getPayApiToken(), expectedProduct.getPayApiToken()) &&
                        StringUtils.equals(actualProduct.getDescription(), expectedProduct.getDescription()) &&
                        StringUtils.equals(actualProduct.getName(), expectedProduct.getName()) &&
                        NumberUtils.equals(actualProduct.getPrice(), expectedProduct.getPrice()) &&
                        actualProduct.getStatus() == expectedProduct.getStatus() &&
                        actualProduct.getType() == expectedProduct.getType() &&
                        NumberUtils.equals(actualProduct.getGatewayAccountId(), expectedProduct.getGatewayAccountId()) &&
                        StringUtils.equals(actualProduct.getReturnUrl(), expectedProduct.getReturnUrl()) &&
                        StringUtils.equals(actualProduct.getServiceName(), expectedProduct.getServiceName()) &&
                        actualProduct.getReferenceEnabled() == expectedProduct.getReferenceEnabled() &&
                        StringUtils.equals(actualProduct.getReferenceLabel(), expectedProduct.getReferenceLabel()) &&
                        StringUtils.equals(actualProduct.getReferenceHint(), expectedProduct.getReferenceHint())
                );
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Product ").appendValue(expectedProduct);
            }
        };
    }
}

