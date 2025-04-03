package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.pay.products.extension.ProductsAppWithPostgresExtension;
import uk.gov.pay.products.persistence.dao.PaymentDao;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.products.fixtures.ProductEntityFixture.aProductEntity;
import static uk.gov.pay.products.util.PaymentStatus.ERROR;
import static uk.gov.pay.products.util.PublicAPIErrorCodes.CREATE_PAYMENT_VALIDATION_ERROR;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

@ExtendWith(DropwizardExtensionsSupport.class)
public class CreatePaymentResourceIT {

    @RegisterExtension
    public static ProductsAppWithPostgresExtension app = new ProductsAppWithPostgresExtension();
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private ProductDao productDao;
    private PaymentDao paymentDao;
    private ProductEntity productEntity;

    @BeforeEach
    void setUp() {
        productDao = app.getInstanceFromGuiceContainer(ProductDao.class);
        paymentDao = app.getInstanceFromGuiceContainer(PaymentDao.class);
        productEntity = aProductEntity()
                .withExternalId(randomUuid())
                .withGatewayAccountId(0)
                .withReferenceEnabled(true)
                .build();
        productDao.persist(productEntity);
    }

    @ParameterizedTest
    @CsvSource({
            "AMOUNT_BELOW_MINIMUM, \"Invalid attribute value: amount. Must be greater than or equal to 30. Refer to https://docs.payments.service.gov.uk/making_payments/#amount\"", 
            "ZERO_AMOUNT_NOT_ALLOWED, \"Invalid attribute value: amount. Must be greater than or equal to 1. Refer to https://docs.payments.service.gov.uk/making_payments/#amount\""
    })
    void create_payment_should_fail_when_publicapi_returns_P0102_error_code(String expectedIdentifier, String publicApiErrorDescription) throws Exception {
        stubPublicApiCreatePaymentResponse(publicApiErrorDescription);
        
        given().port(app.getPort())
                .contentType(JSON)
                .accept(APPLICATION_JSON)
                .body(Map.of("reference_number", "a ref", "price", 29))
                .post(format("/v1/api/products/%s/payments", productEntity.getExternalId()))
                .then()
                .statusCode(422)
                .body("errors", hasSize(1))
                .body("error_identifier", is(expectedIdentifier))
                .body("errors[0]", is("Upstream system error."));

        List<PaymentEntity> paymentEntities = paymentDao.findByProductExternalId(productEntity.getExternalId());
        assertThat(paymentEntities, hasSize(1));
        PaymentEntity paymentEntity = paymentEntities.getFirst();
        assertThat(paymentEntity.getStatus(), is(ERROR));
        assertThat(paymentEntity.getAmount(), nullValue());
        assertThat(paymentEntity.getReferenceNumber(), is("a ref"));
        assertThat(paymentEntity.getGovukPaymentId(), nullValue());
    }
    
    private void stubPublicApiCreatePaymentResponse(String description) throws Exception {
        Map<String, String> response = Map.of(
                "field", "amount",
                "code", CREATE_PAYMENT_VALIDATION_ERROR,
                "description", description);
        app.publicApi.stubFor(post(urlPathEqualTo("/v1/payments"))
                .withHeader(AUTHORIZATION, matching("Bearer " + productEntity.getPayApiToken()))
                .withHeader(CONTENT_TYPE, matching(APPLICATION_JSON))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withStatus(422)
                        .withBody(objectMapper.writeValueAsString(response))));
    }
}
