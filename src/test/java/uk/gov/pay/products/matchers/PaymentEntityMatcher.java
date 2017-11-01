package uk.gov.pay.products.matchers;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import uk.gov.pay.products.persistence.entity.PaymentEntity;

public class PaymentEntityMatcher {
    public static Matcher<PaymentEntity> isSame(final PaymentEntity expectedPaymentEntity) {
        return new BaseMatcher<PaymentEntity>() {
            @Override
            public boolean matches(final Object obj) {
                final PaymentEntity actualPaymentEntity = (PaymentEntity) obj;

                return ((actualPaymentEntity != null) &&
                        (expectedPaymentEntity != null) &&
                        (actualPaymentEntity.getExternalId() != null) &&
                        StringUtils.equals(actualPaymentEntity.getNextUrl(), expectedPaymentEntity.getNextUrl()) &&
                        (actualPaymentEntity.getStatus() == expectedPaymentEntity.getStatus()) &&
                        StringUtils.equals(actualPaymentEntity.getGovukPaymentId(), expectedPaymentEntity.getGovukPaymentId()) &&
                        (actualPaymentEntity.getProductEntity() == expectedPaymentEntity.getProductEntity()) &&
                        (ObjectUtils.equals(actualPaymentEntity.getAmount(), expectedPaymentEntity.getAmount())));
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("PaymentEntity ").appendValue(expectedPaymentEntity);
            }
        };
    }
}

