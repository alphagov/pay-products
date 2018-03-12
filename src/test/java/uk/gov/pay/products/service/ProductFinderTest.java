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

import java.net.URLEncoder;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
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
        when(productDao.findByFriendlyUrlOrExternalId(externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByFriendlyUrlOrExternalId(externalId);

        assertThat(productOptional.isPresent(), is(true));
        assertThat(productOptional.get().getExternalId(), is(externalId));
    }

    @Test
    public void findByExternalId_shouldReturnEmpty_whenNotFound() throws Exception {
        String externalId = "1";
        when(productDao.findByFriendlyUrlOrExternalId(externalId)).thenReturn(Optional.empty());

        Optional<Product> productOptional = productFinder.findByFriendlyUrlOrExternalId(externalId);

        assertThat(productOptional.isPresent(), is(false));
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
        String externalId = "1";
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.empty());

        Optional<Product> productOptional = productFinder.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId);

        assertThat(productOptional.isPresent(), is(false));
    }

    @Test
    public void disableByExternalId_shouldDisableProduct_whenFound() throws Exception{
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        when(productDao.findByFriendlyUrlOrExternalId(externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByFriendlyUrlOrExternalId(externalId);
        assertThat(productOptional.isPresent(), is(true));
        assertThat(productOptional.get().getStatus(), is(ProductStatus.ACTIVE));

        Optional<Product> disabledProduct = productFinder.disableByExternalId(externalId);

        assertThat(disabledProduct.isPresent(), is(true));
        assertThat(disabledProduct.get().getStatus(), is(ProductStatus.INACTIVE));
    }

    @Test
    public void disableByExternalId_shouldReturnEmpty_whenNotFound() throws Exception{
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        when(productDao.findByFriendlyUrlOrExternalId(externalId)).thenReturn(Optional.empty());

        Optional<Product> disabledProduct = productFinder.disableByExternalId(externalId);

        assertThat(disabledProduct.isPresent(), is(false));
    }

    @Test
    public void disableByGatewayAccountIdAndExternalId_shouldDisableProduct_whenFound() throws Exception{
        Integer gatewayAccountId = 1;
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        productEntity.setGatewayAccountId(gatewayAccountId);
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId);
        assertThat(productOptional.isPresent(), is(true));
        assertThat(productOptional.get().getStatus(), is(ProductStatus.ACTIVE));

        Optional<Product> disabledProduct = productFinder.disableByGatewayAccountIdAndExternalId(gatewayAccountId, externalId);

        assertThat(disabledProduct.isPresent(), is(true));
        assertThat(disabledProduct.get().getStatus(), is(ProductStatus.INACTIVE));
    }

    @Test
    public void disableByGatewayAccountIdAndExternalId_shouldReturnEmpty_whenNotFound() throws Exception{
        Integer gatewayAccountId = 1;
        String externalId = "1";
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.empty());

        Optional<Product> disabledProduct = productFinder.disableByGatewayAccountIdAndExternalId(gatewayAccountId, externalId);

        assertThat(disabledProduct.isPresent(), is(false));
    }

    @Test
    public void findByFriendlyUrl_shouldReturnProduct_whenFound() throws Exception{
        String externalId = "1";
        String friendlyUrl = URLEncoder.encode("kent-council/pay-for-your-parking-permit", "UTF-8");
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        productEntity.setFriendlyUrl(friendlyUrl);
        when(productDao.findByFriendlyUrlOrExternalId(friendlyUrl)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByFriendlyUrlOrExternalId(friendlyUrl);

        assertThat(productOptional.isPresent(), is(true));
        assertThat(productOptional.get().getExternalId(), is(externalId));
        assertThat(productOptional.get().getFriendlyUrl(), is(friendlyUrl));
    }

    @Test
    public void findByFriendlyUrl_shouldReturnEmpty_whenNotFound() throws Exception {
        String friendlyUrl = URLEncoder.encode("kent-council/pay-for-your-parking-permit", "UTF-8");
        when(productDao.findByFriendlyUrlOrExternalId(friendlyUrl)).thenReturn(Optional.empty());

        Optional<Product> productOptional = productFinder.findByFriendlyUrlOrExternalId(friendlyUrl);

        assertThat(productOptional.isPresent(),is(false));
    }
}
