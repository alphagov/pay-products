package uk.gov.pay.products.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.exception.ProductNotFoundException;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.model.ProductMetadata;
import uk.gov.pay.products.model.ProductUpdateRequest;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.dao.ProductMetadataDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductType;
import uk.gov.service.payments.commons.model.SupportedLanguage;
import uk.gov.service.payments.commons.model.jsonpatch.JsonPatchRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.products.fixtures.ProductEntityFixture.aProductEntity;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

@RunWith(MockitoJUnitRunner.class)
public class ProductCreatorTest {
    
    @Mock
    private ProductDao productDao;
    @Mock
    private ProductMetadataDao productMetadataDao;
    @Captor
    private ArgumentCaptor<ProductEntity> persistedProductEntity;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ProductCreator productCreator;
    private String payApiToken;
    private Integer gatewayAccountId = randomInt();
    private static final String PRODUCT_NAME = "Test product name";
    private static final Long PRICE = 1050L;
    public static final String externalId = "external-id";

    @Before
    public void setup() {
        LinksDecorator linksDecorator = new LinksDecorator("http://localhost", "http://localhost/pay", "http://localhost/payments");
        productCreator = new ProductCreator(productDao, productMetadataDao, linksDecorator);
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
        String updatedName = "updated-name";
        String updatedDescription = "updated-description";
        Long updatedPrice = 500L;
        String updatedReferenceLabel = "updated-reference-label";
        String updatedReferenceHint = "updated-reference-hint";
        String updatedAmountHint = "updated-amount-hint";

        ProductMetadata metadata = new ProductMetadata("key-1", "value-1");
        ProductUpdateRequest productUpdateRequest = new ProductUpdateRequest(
                updatedName,
                updatedDescription,
                updatedPrice,
                true,
                updatedReferenceLabel,
                updatedReferenceHint,
                updatedAmountHint,
                List.of(metadata));
        
        ProductEntity productEntity = aProductEntity().build();
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> maybeUpdatedProduct = productCreator.doUpdateByGatewayAccountId(gatewayAccountId, externalId, productUpdateRequest);

        assertTrue(maybeUpdatedProduct.isPresent());
        Product updatedProduct = maybeUpdatedProduct.get();
        assertThat(updatedProduct.getName(), is(updatedName));
        assertThat(updatedProduct.getDescription(), is(updatedDescription));
        assertThat(updatedProduct.getPrice(), is(updatedPrice));
        assertThat(updatedProduct.getReferenceEnabled(), is(productUpdateRequest.getReferenceEnabled()));
        assertThat(updatedProduct.getReferenceLabel(), is(updatedReferenceLabel));
        assertThat(updatedProduct.getReferenceHint(), is(updatedReferenceHint));
        assertThat(updatedProduct.getAmountHint(), is(updatedAmountHint));
        assertThat(updatedProduct.getMetadata(), contains(metadata));

        verify(productMetadataDao).deleteForProductExternalId("external-id");
    }

    @Test
    public void doUpdateByGatewayAccountId_referenceDisabled_shouldUpdateProduct() {
        String updatedName = "updated-name";
        String updatedDescription = "updated-description";
        Long updatedPrice = 500L;

        ProductUpdateRequest productUpdateRequest = new ProductUpdateRequest(
                updatedName,
                updatedDescription,
                updatedPrice,
                true,
                null,
                null,
                null,
                null);

        ProductEntity productEntity = aProductEntity().build();
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.of(productEntity));

        Optional<Product> maybeUpdatedProduct = productCreator.doUpdateByGatewayAccountId(gatewayAccountId, externalId, productUpdateRequest);
        
        assertTrue(maybeUpdatedProduct.isPresent());
        Product updatedProduct = maybeUpdatedProduct.get();
        assertThat(updatedProduct.getName(), is(updatedName));
        assertThat(updatedProduct.getDescription(), is(updatedDescription));
        assertThat(updatedProduct.getPrice(), is(updatedPrice));
        assertThat(updatedProduct.getReferenceEnabled(), is(productUpdateRequest.getReferenceEnabled()));
        assertThat(updatedProduct.getReferenceLabel(), is(nullValue()));
        assertThat(updatedProduct.getReferenceHint(), is(nullValue()));
        assertThat(updatedProduct.getMetadata(), is(nullValue()));
    }

    @Test
    public void doUpdateByGatewayAccountId_shouldNotUpdateProduct_whenNotFound() {
        String updatedName = "updated-name";
        String updatedDescription = "updated-description";
        Long updatedPrice = 500L;

        ProductUpdateRequest productUpdateRequest = new ProductUpdateRequest(
                updatedName,
                updatedDescription,
                updatedPrice,
                true,
                null,
                null,
                null,
                null);

        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.empty());

        Optional<Product> updatedProduct = productCreator.doUpdateByGatewayAccountId(gatewayAccountId, externalId, productUpdateRequest);

        assertFalse(updatedProduct.isPresent());
    }

    @Test
    public void update_shouldUpdateProduct() {
        ProductEntity productEntity = aProductEntity()
                .withRequireCaptcha(false)
                .build();
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.of(productEntity));

        JsonPatchRequest replaceRequireCaptcha = JsonPatchRequest.from(objectMapper.valueToTree(
                Map.of("path", "require_captcha",
                        "op", "replace",
                        "value", true)
        ));

        Product updatedProduct = productCreator.update(gatewayAccountId, externalId, 
                List.of(replaceRequireCaptcha));
        assertThat(updatedProduct.isRequireCaptcha(), is(true));
    }

    @Test
    public void update_shouldThrowExceptionWhenProductNotFound() {
        JsonPatchRequest patchRequest = JsonPatchRequest.from(objectMapper.valueToTree(
                Map.of("path", "require_captcha",
                        "op", "replace",
                        "value", true)
        ));
        
        when(productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productCreator.update(gatewayAccountId, externalId, Collections.singletonList(patchRequest)));
    }
}
