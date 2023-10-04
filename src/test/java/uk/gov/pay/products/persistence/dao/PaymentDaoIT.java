package uk.gov.pay.products.persistence.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.PaymentEntityFixture;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.PaymentStatus;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.pay.products.util.PaymentStatus.CREATED;
import static uk.gov.pay.products.util.PaymentStatus.ERROR;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;
import static uk.gov.pay.products.utils.TestHelpers.createPaymentEntity;

public class PaymentDaoIT extends DaoTestBase {

    private PaymentDao paymentDao;
    private ProductEntity productEntity;

    @Before
    public void before(){
        paymentDao = env.getInstance(PaymentDao.class);
        ProductDao productDao = env.getInstance(ProductDao.class);
        String productExternalId = randomUuid();
        productEntity = ProductEntityFixture.aProductEntity()
                .withExternalId(productExternalId)
                .build();
        productEntity = productDao.merge(productEntity);
    }
    
    @After
    public void after() {
        databaseHelper.truncateAllData();
    }

    @Test
    public void shouldFindAPaymentByGovukPaymentId() {
        PaymentEntity payment = addPaymentToDB(createPaymentEntity(productEntity, CREATED, "MH2KJY5KPW", randomUuid()));
        Optional<PaymentEntity> paymentEntity = paymentDao.findByGovukPaymentId(payment.getGovukPaymentId());
        assertTrue(paymentEntity.isPresent());

        assertThat(paymentEntity.get().getExternalId(), is(payment.getExternalId()));
        assertThat(paymentEntity.get().getNextUrl(), is(payment.getNextUrl()));
        assertThat(paymentEntity.get().getAmount(), is(payment.getAmount()));
        assertThat(paymentEntity.get().getStatus(), is(payment.getStatus()));
        assertThat(paymentEntity.get().getGovukPaymentId(), is(payment.getGovukPaymentId()));
        assertNotNull(paymentEntity.get().getDateCreated());
    }
    
    @Test
    public void shouldNotFindAPaymentByGovukPaymentId() {
        addPaymentToDB(createPaymentEntity(productEntity, CREATED, "MH2KJY5KPW", randomUuid()));
        Optional<PaymentEntity> paymentEntity = paymentDao.findByGovukPaymentId(randomUuid());
        assertTrue(paymentEntity.isEmpty());
    }
    
    @Test
    public void shouldFindAPaymentByExternalId() {
        PaymentEntity payment = addPaymentToDB(createPaymentEntity(productEntity, CREATED, "MH2KJY5KPW", randomInt()));
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
    public void shouldSucceed_whenSavingAValidPayment() {
        String externalId = randomUuid();

        PaymentEntity payment = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(externalId)
                .withStatus(CREATED)
                .withProduct(productEntity)
                .build();

        paymentDao.persist(payment);

        Optional<PaymentEntity> expectedPayment = paymentDao.findByExternalId(externalId);
        assertThat(expectedPayment.isPresent(), is(true));

        assertThat(expectedPayment.get().getExternalId(), is(externalId));
    }

    @Test
    public void shouldSucceed_whenSearchingForPaymentsByProductId() {
        Integer gatewayAccountId = randomInt();
        PaymentEntity payment1 = addPaymentToDB(createPaymentEntity(productEntity, CREATED, "MH2KJY5KIY", gatewayAccountId));
        PaymentEntity payment2 = addPaymentToDB(createPaymentEntity(productEntity, ERROR, "MH3JY6KIY", gatewayAccountId));

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
    public void shouldFindPayment_whenSearchingByGatewayAccountIdAndReferenceNumber() {
        String referenceNumber = randomUuid().substring(1,10).toUpperCase();
        Integer gatewayAccountId = randomInt();
        addPaymentToDB(createPaymentEntity(productEntity, CREATED, referenceNumber, gatewayAccountId));

        Optional<PaymentEntity> optionalPaymentEntity = paymentDao.findByGatewayAccountIdAndReferenceNumber(gatewayAccountId, referenceNumber);
        assertThat(optionalPaymentEntity.isPresent(), is(true));
    }
    
    @Test
    public void shouldNotFindPayment_whenSearchingByDifferentGatewayAccountIdAndReferenceNumber() {
        String referenceNumber = randomUuid().substring(1,10).toUpperCase();
        Integer gatewayAccountId2 = randomInt();
        addPaymentToDB(createPaymentEntity(productEntity, CREATED, referenceNumber, randomInt()));
        addPaymentToDB(createPaymentEntity(productEntity, ERROR, randomUuid().substring(1,10).toUpperCase(), gatewayAccountId2));
        
        Optional<PaymentEntity> optionalPaymentEntity = paymentDao.findByGatewayAccountIdAndReferenceNumber(gatewayAccountId2, referenceNumber);
        assertThat(optionalPaymentEntity.isPresent(), is(false));
    }
    
    @Test
    public void shouldDeletePayments() {
        ZonedDateTime maxDate = ZonedDateTime.ofInstant(Instant.parse("2022-03-03T10:15:30Z"), UTC);

        PaymentEntity payment1 = addPaymentToDB(createPaymentEntity(productEntity, maxDate, 1));
        PaymentEntity payment2 = addPaymentToDB(createPaymentEntity(productEntity, maxDate, 2));
        PaymentEntity payment3 = addPaymentToDB(createPaymentEntity(productEntity, maxDate, 3));
        PaymentEntity payment4 = addPaymentToDB(createPaymentEntity(productEntity, maxDate, 4));

        int numberOfPaymentsDeleted = paymentDao.deletePayments(maxDate, 2);
        assertThat(numberOfPaymentsDeleted, is(2));
        
        assertThat(paymentDao.findByExternalId(payment1.getExternalId()).isPresent(), is(true));
        assertThat(paymentDao.findByExternalId(payment2.getExternalId()).isPresent(), is(true));
        assertThat(paymentDao.findByExternalId(payment3.getExternalId()).isPresent(), is(false));
        assertThat(paymentDao.findByExternalId(payment4.getExternalId()).isPresent(), is(false));
    }
    
    @Test
    public void shouldDeleteZeroPayments() {
        int numberOfPaymentsDeleted = paymentDao.deletePayments(ZonedDateTime.now(), 10);
        assertThat(numberOfPaymentsDeleted, is(0));
    }
}
