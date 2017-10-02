package uk.gov.pay.products.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
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
    public void shouldReturnProduct_whenFoundByExternalId() throws Exception{
        String externalId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setExternalId(externalId);
        when(productDao.findByExternalId(externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> productOptional = productFinder.findByExternalId(externalId);

        assertThat(productOptional.isPresent(), is(true));
        assertThat(productOptional.get().getExternalId(), is(externalId));
    }

    @Test
    public void shouldReturnEmpty_whenNoProductFound() throws Exception {
        String externalId = "1";
        when(productDao.findByExternalId(externalId)).thenReturn(Optional.empty());

        Optional<Product> productOptional = productFinder.findByExternalId(externalId);

        assertThat(productOptional.isPresent(), is(false));
    }
}
