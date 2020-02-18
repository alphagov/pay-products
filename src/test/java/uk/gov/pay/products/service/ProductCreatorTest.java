package uk.gov.pay.products.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.commons.model.SupportedLanguage;
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
    private static final String PRODUCT_NAME = "Test product name";
    private static final Long PRICE = 1050L;

    @Before
    public void setup() {
        LinksDecorator linksDecorator = new LinksDecorator("http://localhost", "http://localhost/pay", "http://localhost/payments");
        productCreator = new ProductCreator(productDao, linksDecorator);
        gatewayAccountId = randomInt();
        payApiToken = randomUuid();
    }

    @Test
    public void shouldSuccess_whenProvidedAProductWithMinimumRequiredFields() {
        Product basicProduct = new Product(
                null,
                PRODUCT_NAME,
                null,
                payApiToken,
                PRICE,
                null,
                gatewayAccountId,
                ProductType.DEMO,
                null,
                null,
                null,
                SupportedLanguage.ENGLISH,
                null);

        Product product = productCreator.doCreate(basicProduct);
        assertThat(product.getName(), is("Test product name"));
        assertThat(product.getPrice(), is(1050L));
        assertThat(product.getPayApiToken(), is(payApiToken));
        assertThat(product.getGatewayAccountId(), is(gatewayAccountId));

        verify(productDao).persist(persistedProductEntity.capture());
        ProductEntity productEntity = persistedProductEntity.getValue();

        assertThat(productEntity.getName(), is("Test product name"));
        assertThat(productEntity.getPrice(), is(1050L));
        assertThat(productEntity.getPayApiToken(), is(payApiToken));
        assertThat(productEntity.getExternalId(), is(not(emptyOrNullString())));
        assertThat(productEntity.getDateCreated(), is(notNullValue()));
        assertThat(productEntity.getGatewayAccountId(), is(notNullValue()));
        assertThat(productEntity.getGatewayAccountId(), is(gatewayAccountId));
        assertThat(productEntity.getType(), is(notNullValue()));
        assertThat(productEntity.getType(), is(ProductType.DEMO));
        assertThat(productEntity.getLanguage(), is(SupportedLanguage.ENGLISH));
    }

    @Test
    public void shouldSuccess_whenProvidedAllFields() {
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
                ProductType.DEMO,
                returnUrl,
                serviceNamePath,
                productNamePath,
                SupportedLanguage.ENGLISH,
                null);

        Product product = productCreator.doCreate(productRequest);
        assertThat(product.getDescription(), is(description));
        assertThat(product.getReturnUrl(), is(returnUrl));

        verify(productDao).persist(persistedProductEntity.capture());
        ProductEntity productEntityValue = persistedProductEntity.getValue();

        assertThat(productEntityValue.getDescription(), is(description));
        assertThat(productEntityValue.getReturnUrl(), is(returnUrl));
    }

    @Test
    public void doUpdateByGatewayAccountId_referenceEnabled_shouldUpdateProduct() {
        String externalId = "external-id";
        String updatedName = "updated-name";
        String updatedDescription = "updated-description";
        Long updatedPrice = 500L;
        String updatedReferenceLabel = "updated-reference-label";
        String updatedReferenceHint = "updated-reference-hint";

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
                true,
                updatedReferenceLabel,
                updatedReferenceHint,
                SupportedLanguage.ENGLISH,
                null);

        ProductEntity mockedProductEntity = mock(ProductEntity.class);
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.of(mockedProductEntity));
        when(mockedProductEntity.toProduct()).thenReturn(productToUpdate);

        Optional<Product> updatedProduct = productCreator.doUpdateByGatewayAccountId(gatewayAccountId, externalId, productToUpdate);

        verify(mockedProductEntity).setName(updatedName);
        verify(mockedProductEntity).setDescription(updatedDescription);
        verify(mockedProductEntity).setPrice(updatedPrice);
        verify(mockedProductEntity).setReferenceEnabled(true);
        verify(mockedProductEntity).setReferenceLabel(updatedReferenceLabel);
        verify(mockedProductEntity).setReferenceHint(updatedReferenceHint);
        verify(mockedProductEntity).toProduct();
        verifyNoMoreInteractions(mockedProductEntity);

        assertTrue(updatedProduct.isPresent());
        assertThat(productToUpdate, ProductMatcher.isSame(updatedProduct.get()));
    }

    @Test
    public void doUpdateByGatewayAccountId_referenceDisabled_shouldUpdateProduct() {
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
                false,
                null,
                null,
                SupportedLanguage.ENGLISH,
                null);

        ProductEntity mockedProductEntity = mock(ProductEntity.class);
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.of(mockedProductEntity));
        when(mockedProductEntity.toProduct()).thenReturn(productToUpdate);

        Optional<Product> updatedProduct = productCreator.doUpdateByGatewayAccountId(gatewayAccountId, externalId, productToUpdate);

        verify(mockedProductEntity).setName(updatedName);
        verify(mockedProductEntity).setDescription(updatedDescription);
        verify(mockedProductEntity).setPrice(updatedPrice);
        verify(mockedProductEntity).setReferenceEnabled(false);
        verify(mockedProductEntity).setReferenceLabel(null);
        verify(mockedProductEntity).setReferenceHint(null);
        verify(mockedProductEntity).toProduct();
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
                SupportedLanguage.ENGLISH,
                null);

        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.empty());

        Optional<Product> updatedProduct = productCreator.doUpdateByGatewayAccountId(gatewayAccountId, externalId, productToUpdate);

        assertFalse(updatedProduct.isPresent());
    }
}
