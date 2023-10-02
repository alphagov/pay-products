package uk.gov.pay.products.utils;

import uk.gov.pay.products.fixtures.PaymentEntityFixture;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.RandomIdGenerator;

import java.time.ZonedDateTime;

import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;

public class PaymentTestHelper {

    public static PaymentEntity createPaymentEntity(ProductEntity productEntity, ZonedDateTime date, int daysToMinusFromDate) {
        return PaymentEntityFixture.aPaymentEntity()
                .withReferenceNumber(RandomIdGenerator.randomUserFriendlyReference())
                .withProduct(productEntity)
                .withGatewayAccountId(randomInt())
                .withDateCreated(date.minusDays(daysToMinusFromDate))
                .build();
    }
}
