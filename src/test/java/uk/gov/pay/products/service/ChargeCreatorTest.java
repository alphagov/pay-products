package uk.gov.pay.products.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.model.Charge;
import uk.gov.pay.products.persistence.dao.ChargeDao;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.ChargeEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.service.charge.ChargeCreator;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

@RunWith(MockitoJUnitRunner.class)
public class ChargeCreatorTest {
    @Mock
    private ChargeDao chargeDao;
    @Mock
    private ProductDao productDao;

    @Captor
    private ArgumentCaptor<ChargeEntity> persistedCharge;

    private ChargeCreator chargeCreator;
    private ProductEntity mockProduct;
    private String externalProductId;

    @Before
    public void setup() throws Exception {
        chargeCreator = new ChargeCreator(chargeDao, productDao);
        mockProduct = mock(ProductEntity.class);
        externalProductId = randomUuid();
    }

    @Test
    public void shouldSuccess_whenProvidedProductExists_noPriceOverride() throws Exception {
        String externalProductId = randomUuid();
        Long productPrice = 500L;

        Charge basicCharge = new Charge(externalProductId, null);

        when(mockProduct.getExternalId()).thenReturn(externalProductId);
        when(mockProduct.getPrice()).thenReturn(productPrice);
        Optional<ProductEntity> productOptional = Optional.of(mockProduct);

        when(productDao.findByExternalId(externalProductId)).thenReturn(productOptional);

        Optional<Charge> maybeCharge = chargeCreator.doCreate(basicCharge);
        assertThat(maybeCharge.isPresent(), is(true));
        Charge charge = maybeCharge.get();

        verify(productDao, times(1)).findByExternalId(externalProductId);
        verify(chargeDao, times(1)).persist(persistedCharge.capture());

        assertThat(charge.getProductExternalId(), is(externalProductId));
        assertThat(charge.getPrice(), is(productPrice));
    }

    @Test
    public void shouldSuccess_whenProvidedProductExists_andPriceOverride() throws Exception {
        Long priceOverride = 1050L;
        String productDescription = "Test description";

        Charge basicCharge = new Charge(externalProductId, priceOverride);

        when(mockProduct.getExternalId()).thenReturn(externalProductId);
        when(mockProduct.getDescription()).thenReturn(productDescription);
        Optional<ProductEntity> productOptional = Optional.of(mockProduct);
        when(productDao.findByExternalId(externalProductId)).thenReturn(productOptional);

        Optional<Charge> maybeCharge = chargeCreator.doCreate(basicCharge);
        assertThat(maybeCharge.isPresent(), is(true));
        Charge charge = maybeCharge.get();

        assertThat(charge.getPrice(), is(priceOverride));
        assertThat(charge.getExternalId(), is(not(isEmptyOrNullString())));
        assertThat(charge.getDescription(), is(productDescription));
    }

    @Test
    public void shouldReturnEmpty_whenNoProductExists_forExternalProductId() throws Exception {
        Charge basicCharge = new Charge(externalProductId, null);

        Optional<Charge> maybeCharge = chargeCreator.doCreate(basicCharge);
        assertThat(maybeCharge.isPresent(), is(false));
    }
}
