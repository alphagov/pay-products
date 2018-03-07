package uk.gov.pay.products.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductFinderTest {

    @Mock
    private ProductDao productDao;

    private ProductFinder productFinder;
    private LinksDecorator linksDecorator;

    @Before
    public void setup() throws Exception {
        linksDecorator = new LinksDecorator("http://localhost", "http://localhost/pay");
        productFinder = new ProductFinder(productDao, linksDecorator);
    }

    @Test
    public void findByExternalId_shouldReturnProduct_whenFound() throws Exception{
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        when(productDao.findByExternalId(externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByExternalId(externalId);

        assertTrue(productOptional.isPresent());
        assertThat(productOptional.get().getExternalId(), is(externalId));
    }

    @Test
    public void findByExternalId_shouldReturnEmpty_whenNotFound() throws Exception {
        String externalId = "1";
        when(productDao.findByExternalId(externalId)).thenReturn(Optional.empty());

        Optional<Product> productOptional = productFinder.findByExternalId(externalId);

        assertFalse(productOptional.isPresent());
    }

    @Test
    public void findByGatewayAccountIdAndExternalId_shouldReturnProduct_whenFound() throws Exception{
        Integer gatewayAccountId = 1;
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        productEntity.setGatewayAccountId(gatewayAccountId);
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId);

        assertTrue(productOptional.isPresent());
        assertThat(productOptional.get().getExternalId(), is(externalId));
    }

    @Test
    public void findByGatewayAccountIdAndExternalId_shouldReturnEmpty_whenNotFound() throws Exception {
        Integer gatewayAccountId = 1;
        Integer anotherGatewayAccountId = 2;
        String externalId = "1";
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.empty());

        Optional<Product> productOptional = productFinder.findByGatewayAccountIdAndExternalId(anotherGatewayAccountId, externalId);

        assertFalse(productOptional.isPresent());
    }

    @Test
    public void disableProduct_shouldDisableProduct() throws Exception{
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        when(productDao.findByExternalId(externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByExternalId(externalId);
        assertThat(productOptional.isPresent(), is(true));
        assertThat(productOptional.get().getStatus(), is(ProductStatus.ACTIVE));

        productFinder.disableProduct(externalId);
        productOptional = productFinder.findByExternalId(externalId);

        assertThat(productOptional.isPresent(), is(true));
        assertThat(productOptional.get().getStatus(), is(ProductStatus.INACTIVE));
    }
}
