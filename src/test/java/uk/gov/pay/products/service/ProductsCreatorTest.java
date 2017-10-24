package uk.gov.pay.products.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

@RunWith(MockitoJUnitRunner.class)
public class ProductsCreatorTest {

    @Mock
    private ProductDao productDao;
    private ProductsCreator productsCreator;
    @Captor
    private ArgumentCaptor<ProductEntity> persistedProductEntity;
    private String payApiToken;
    private Integer gatewayAccountId = randomInt();
    public static final String PRODUCT_NAME = "Test product name";
    public static final Long PRICE = 1050L;

    @Before
    public void setup() throws Exception {
        LinksDecorator linksDecorator = new LinksDecorator("http://localhost", "http://localhost/pay");
        productsCreator = new ProductsCreator(productDao, linksDecorator);
        gatewayAccountId = randomInt();
        payApiToken = randomUuid();
    }

    @Test
    public void shouldSuccess_whenProvidedAProductWithMinimumRequiredFields() throws Exception {
        Product basicProduct = new Product(
                null,
                PRODUCT_NAME,
                null,
                payApiToken,
                PRICE,
                null,
                gatewayAccountId,
                null
        );

        Product product = productsCreator.doCreate(basicProduct);
        assertThat(product.getName(), is("Test product name"));
        assertThat(product.getPrice(), is(1050L));
        assertThat(product.getPayApiToken(), is(payApiToken));
        assertThat(product.getGatewayAccountId(), is(gatewayAccountId));

        verify(productDao, times(1)).persist(persistedProductEntity.capture());
        ProductEntity productEntity = persistedProductEntity.getValue();

        assertThat(productEntity.getName(), is("Test product name"));
        assertThat(productEntity.getPrice(), is(1050L));
        assertThat(productEntity.getPayApiToken(), is(payApiToken));
        assertThat(productEntity.getExternalId(), is(not(isEmptyOrNullString())));
        assertThat(productEntity.getDateCreated(), is(notNullValue()));
        assertThat(productEntity.getGatewayAccountId(), is(notNullValue()));
        assertThat(productEntity.getGatewayAccountId(), is(gatewayAccountId));
    }

    @Test
    public void shouldSuccess_whenProvidedAllFields() throws Exception {
        String description = "Test description";
        String returnUrl = "http://my-return-url.com";

        Product productRequest = new Product(
                null,
                PRODUCT_NAME,
                description,
                payApiToken,
                PRICE,
                null,
                gatewayAccountId,
                returnUrl
        );

        Product product = productsCreator.doCreate(productRequest);
        assertThat(product.getDescription(), is(description));
        assertThat(product.getReturnUrl(), is(returnUrl));

        verify(productDao, times(1)).persist(persistedProductEntity.capture());
        ProductEntity productEntityValue = persistedProductEntity.getValue();

        assertThat(productEntityValue.getDescription(), is(description));
        assertThat(productEntityValue.getReturnUrl(), is(returnUrl));
    }
}
