package uk.gov.pay.products.persistence.dao;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.PaymentEntityFixture;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.PaymentStatus;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

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
        String externalId = randomUuid();

        PaymentEntity payment = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(externalId)
                .withStatus(PaymentStatus.CREATED)
                .withProduct(productEntity)
                .build();
        databaseHelper.addPayment(payment.toPayment());

        Optional<PaymentEntity> expectedPayment = paymentDao.findByExternalId(externalId);
        assertTrue(expectedPayment.isPresent());

        assertThat(expectedPayment.get().getExternalId(), is(externalId));
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
        PaymentEntity payment_1 = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(randomUuid())
                .withStatus(PaymentStatus.CREATED)
                .withProduct(productEntity)
                .build();

        paymentDao.persist(payment_1);

        PaymentEntity payment_2 = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(randomUuid())
                .withStatus(PaymentStatus.CREATED)
                .withProduct(productEntity)
                .build();

        paymentDao.persist(payment_2);

        List<Payment> expectedList = paymentDao.findByProductId(productEntity.getId());
        assertThat(expectedList.isEmpty(), is(false));
        assertThat(expectedList.size(), is(2));
        assertThat(expectedList.get(0).getProductExternalId(), is(productEntity.getExternalId()));
        assertThat(expectedList.get(1).getProductExternalId(), is(productEntity.getExternalId()));
    }

}
