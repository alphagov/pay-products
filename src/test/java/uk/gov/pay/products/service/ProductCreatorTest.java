package uk.gov.pay.products.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.matchers.ProductMatcher;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductType;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

@RunWith(MockitoJUnitRunner.class)
public class ProductCreatorTest {

    @Mock
    private ProductDao productDao;
    private ProductCreator productCreator;
    @Captor
    private ArgumentCaptor<ProductEntity> persistedProductEntity;
    private String payApiToken;
    private Integer gatewayAccountId = randomInt();
    public static final String PRODUCT_NAME = "Test product name";
    public static final Long PRICE = 1050L;
    private static final String SERVICE_NAME = "Example Service";

    @Before
    public void setup() throws Exception {
        LinksDecorator linksDecorator = new LinksDecorator("http://localhost", "http://localhost/pay", "http://localhost/payments");
        productCreator = new ProductCreator(productDao, linksDecorator);
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
                null,
                ProductType.DEMO,
                null,
                null,
                null
        );

        Product product = productCreator.doCreate(basicProduct);
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
        assertThat(productEntity.getType(), is(notNullValue()));
        assertThat(productEntity.getType(), is(ProductType.DEMO));
    }

    @Test
    public void shouldSuccess_whenProvidedAllFields() throws Exception {
        String description = "Test description";
        String returnUrl = "http://my-return-url.com";
        String serviceNamePath = "service-name-path";
        String productNamePath = "product-name-path";

        Product productRequest = new Product(
                null,
                PRODUCT_NAME,
                description,
                payApiToken,
                PRICE,
                null,
                gatewayAccountId,
                SERVICE_NAME,
                ProductType.DEMO,
                returnUrl,
                serviceNamePath,
                productNamePath
        );

        Product product = productCreator.doCreate(productRequest);
        assertThat(product.getDescription(), is(description));
        assertThat(product.getReturnUrl(), is(returnUrl));
        assertThat(product.getServiceName(), is(SERVICE_NAME));

        verify(productDao, times(1)).persist(persistedProductEntity.capture());
        ProductEntity productEntityValue = persistedProductEntity.getValue();

        assertThat(productEntityValue.getDescription(), is(description));
        assertThat(productEntityValue.getReturnUrl(), is(returnUrl));
        assertThat(productEntityValue.getServiceName(), is(SERVICE_NAME));
    }

    @Test
    public void doUpdateByGatewayAccountId_shouldUpdateProduct() {
        String externalId = "external-id";
        String updatedName = "updated-name";
        String updatedDescription = "updated-description";
        Long updatedPrice = 500L;

        Product productToUpdate = new Product(
                "auto-generated-id",
                updatedName,
                updatedDescription,
                null,
                updatedPrice,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        ProductEntity mockedProductEntity = mock(ProductEntity.class);
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.of(mockedProductEntity));
        when(mockedProductEntity.toProduct()).thenReturn(productToUpdate);

        Optional<Product> updatedProduct = productCreator.doUpdateByGatewayAccountId(gatewayAccountId, externalId, productToUpdate);

        verify(mockedProductEntity,  times(1)).setName(updatedName);
        verify(mockedProductEntity,  times(1)).setDescription(updatedDescription);
        verify(mockedProductEntity,  times(1)).setPrice(updatedPrice);
        verify(mockedProductEntity,  times(1)).toProduct();
        verifyNoMoreInteractions(mockedProductEntity);


        assertTrue(updatedProduct.isPresent());
        assertThat(productToUpdate, ProductMatcher.isSame(updatedProduct.get()));
    }

    @Test
    public void doUpdateByGatewayAccountId_shouldNotUpdateProduct_whenNotFound() {
        String externalId = "external-id";
        String updatedName = "updated-name";
        String updatedDescription = "updated-description";
        Long updatedPrice = 500L;

        Product productToUpdate = new Product(
                "auto-generated-id",
                updatedName,
                updatedDescription,
                null,
                updatedPrice,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.empty());

        Optional<Product> updatedProduct = productCreator.doUpdateByGatewayAccountId(gatewayAccountId, externalId, productToUpdate);

        assertFalse(updatedProduct.isPresent());
    }
}
