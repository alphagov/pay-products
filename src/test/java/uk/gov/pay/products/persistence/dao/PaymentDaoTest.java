package uk.gov.pay.products.persistence.dao;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.PaymentEntityFixture;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.PaymentStatus;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.pay.commons.utils.RandomIdGenerator.randomInt;
import static uk.gov.pay.commons.utils.RandomIdGenerator.randomUuid;

public class PaymentDaoTest extends DaoTestBase {

    private PaymentDao paymentDao;
    private ProductEntity productEntity;
    private ProductDao productDao;
    private String productExternalId;

    @Before
    public void before(){
        paymentDao = env.getInstance(PaymentDao.class);
        productDao = env.getInstance(ProductDao.class);
        productExternalId = randomUuid();
        productEntity = ProductEntityFixture.aProductEntity()
                .withExternalId(productExternalId).build();
        productEntity = productDao.merge(productEntity);
    }

    @Test
    public void shouldSuccess_whenFindingAValidPayment() throws Exception {
        PaymentEntity payment = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(randomUuid())
                .withStatus(PaymentStatus.CREATED)
                .withProduct(productEntity)
                .withReferenceNumber("MH2KJY5KPW")
                .build();
        databaseHelper.addPayment(payment.toPayment(), 1);

        Optional<PaymentEntity> paymentEntity = paymentDao.findByExternalId(payment.getExternalId());
        assertTrue(paymentEntity.isPresent());

        assertThat(paymentEntity.get().getExternalId(), is(payment.getExternalId()));
        assertThat(paymentEntity.get().getNextUrl(), is(payment.getNextUrl()));
        assertThat(paymentEntity.get().getAmount(), is(payment.getAmount()));
        assertThat(paymentEntity.get().getStatus(), is(payment.getStatus()));
        assertThat(paymentEntity.get().getGovukPaymentId(), is(payment.getGovukPaymentId()));
        assertNotNull(paymentEntity.get().getDateCreated());
    }

    @Test
    public void shouldSuccess_whenSavingAValidPayment() throws Exception {
        String externalId = randomUuid();

        PaymentEntity payment = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(externalId)
                .withStatus(PaymentStatus.CREATED)
                .withProduct(productEntity)
                .build();

        paymentDao.persist(payment);

        Optional<PaymentEntity> expectedPayment = paymentDao.findByExternalId(externalId);
        assertThat(expectedPayment.isPresent(), is(true));

        assertThat(expectedPayment.get().getExternalId(), is(externalId));
    }

    @Test
    public void shouldSuccess_whenSearchingForPaymentsByProductId() throws Exception{
        Integer gatewayAccountId = randomInt();
        PaymentEntity payment1 = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(randomUuid())
                .withStatus(PaymentStatus.CREATED)
                .withProduct(productEntity)
                .withReferenceNumber("MH2KJY5KIY")
                .build();
        databaseHelper.addPayment(payment1.toPayment(), gatewayAccountId);

        PaymentEntity payment2 = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(randomUuid())
                .withStatus(PaymentStatus.ERROR)
                .withProduct(productEntity)
                .withReferenceNumber("MH3JY6KIY")
                .build();
        databaseHelper.addPayment(payment2.toPayment(), gatewayAccountId);

        List<PaymentEntity> paymentEntities = paymentDao.findByProductExternalId(productEntity.getExternalId());
        assertFalse(paymentEntities.isEmpty());
        assertThat(paymentEntities.size(), is(2));

        PaymentEntity paymentEntity1 = paymentEntities.get(0);
        assertThat(paymentEntity1.getProductEntity().getExternalId(), is(productEntity.getExternalId()));
        assertThat(paymentEntity1.getExternalId(), is(payment1.getExternalId()));
        assertThat(paymentEntity1.getNextUrl(), is(payment1.getNextUrl()));
        assertThat(paymentEntity1.getAmount(), is(payment1.getAmount()));
        assertThat(paymentEntity1.getStatus(), is(payment1.getStatus()));
        assertThat(paymentEntity1.getGovukPaymentId(), is(payment1.getGovukPaymentId()));
        assertNotNull(paymentEntity1.getDateCreated());

        PaymentEntity paymentEntity2 = paymentEntities.get(1);
        assertThat(paymentEntity2.getProductEntity().getExternalId(), is(productEntity.getExternalId()));
        assertThat(paymentEntity2.getExternalId(), is(payment2.getExternalId()));
        assertThat(paymentEntity2.getNextUrl(), is(payment2.getNextUrl()));
        assertThat(paymentEntity2.getAmount(), is(payment2.getAmount()));
        assertThat(paymentEntity2.getStatus(), is(payment2.getStatus()));
        assertThat(paymentEntity2.getGovukPaymentId(), is(payment2.getGovukPaymentId()));
        assertNotNull(paymentEntity2.getDateCreated());
    }

    @Test
    public void shouldThrowExceptionOnMerge_whenSameGatewayAndReferenceNumber() {
        String referenceNumber = randomUuid().substring(1,10).toUpperCase();
        Integer gatewayAccountId = randomInt();
        PaymentEntity payment1 = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(randomUuid())
                .withStatus(PaymentStatus.CREATED)
                .withProduct(productEntity)
                .withReferenceNumber(referenceNumber)
                .withGatewayAccountId(gatewayAccountId)
                .build();
        databaseHelper.addPayment(payment1.toPayment(), gatewayAccountId);

        PaymentEntity payment2 = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(randomUuid())
                .withStatus(PaymentStatus.ERROR)
                .withProduct(productEntity)
                .withReferenceNumber(referenceNumber)
                .withGatewayAccountId(gatewayAccountId)
                .build();

        Exception exception = null;
        try {
            paymentDao.merge(payment2);
        } catch (Exception ex) {
            exception = ex;
        }
        assertThat(isNull(exception), is(false));
        assertThat(exception instanceof javax.persistence.RollbackException, is(true));
        assertThat(exception.getMessage().contains("payments_gateway_account_id_reference_number_key"), is(true));
        assertThat(exception.getMessage().contains("duplicate key value violates unique constraint"), is(true));
    }
}
