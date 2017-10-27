package uk.gov.pay.products.matchers;


import org.apache.commons.lang.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import uk.gov.pay.products.client.publicapi.PaymentRequest;

public class PaymentRequestMatcher {
    public static Matcher<PaymentRequest> isSame(final PaymentRequest expectedPaymentRequest) {
        return new BaseMatcher<PaymentRequest>() {
            @Override
            public boolean matches(final Object obj) {
                final PaymentRequest actualPaymentRequest = (PaymentRequest) obj;

                return ((actualPaymentRequest != null) &&
                        (expectedPaymentRequest != null) &&
                        (actualPaymentRequest.getAmount() == expectedPaymentRequest.getAmount()) &&
                        StringUtils.equals(actualPaymentRequest.getReference(), expectedPaymentRequest.getReference()) &&
                        StringUtils.equals(actualPaymentRequest.getDescription(), expectedPaymentRequest.getDescription()) &&
                        StringUtils.equals(actualPaymentRequest.getReturnUrl(), expectedPaymentRequest.getReturnUrl()));
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("PaymentRequest ").appendValue(expectedPaymentRequest);
            }
        };
    }
}

