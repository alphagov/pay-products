package uk.gov.pay.products.resources;

import io.restassured.response.ValidatableResponse;
import org.junit.Test;
import uk.gov.pay.products.fixtures.PaymentEntityFixture;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import javax.ws.rs.HttpMethod;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class PaymentResourceTest extends IntegrationTest {

    @Test
    public void givenAnExistingExternalPaymentId_shouldFindAndReturnPayment() throws Exception {
        int gatewayAccountId = randomInt();
        String productExternalId = randomUuid();

        ProductEntity productEntity = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withExternalId(productExternalId)
                .build();

        Product product = productEntity.toProduct();

        databaseHelper.addProduct(product);

        Integer productId = databaseHelper.findProductId(productExternalId);
        productEntity.setId(productId);

        String externalId = randomUuid();
        String nextUrl = "www.gov.uk/pay";

        PaymentEntity payment = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(externalId)
                .withProduct(productEntity)
                .withNextUrl(nextUrl)
                .build();

        databaseHelper.addPayment(payment.toPayment());

        ValidatableResponse response = givenAuthenticatedSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/payments/%s", externalId))
                .then()
                .statusCode(200);

        String paymentsUrl = "http://localhost:8080/v1/api/payments/";
        String productsUIUrl = "http://localhost:3000/pay/";
        response
                .body("_links", hasSize(2))
                .body("_links[0].href", matchesPattern(paymentsUrl + externalId))
                .body("_links[0].method", is(HttpMethod.GET))
                .body("_links[0].rel", is("self"))
                .body("_links[1].href", matchesPattern(productsUIUrl + externalId))
                .body("_links[1].method", is(HttpMethod.POST))
                .body("_links[1].rel", is("pay"));

    }

    @Test
    public void givenANonExistingExternalProductId_shouldReturn404() throws Exception {
        givenAuthenticatedSetup()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/payments/%s", randomUuid()))
                .then()
                .statusCode(404);
    }

    @Test
    public void givenANotAuthenticatedRequest_shouldReturn401() throws Exception {
        givenSetup()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/payments/%s", randomUuid()))
                .then()
                .statusCode(401);

        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/payments?productExternalId=%s", randomUuid()))
                .then()
                .statusCode(401);
    }

    @Test
    public void givenAnExistingProductExternalId_shouldFindAndReturnProducts() throws Exception {
        int gatewayAccountId = randomInt();
        String productExternalId = randomUuid();
        String paymentExternalId1 = randomUuid();
        String paymentExternalId2 = randomUuid();

        ProductEntity productEntity = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withExternalId(productExternalId)
                .build();

        Product product = productEntity.toProduct();

        databaseHelper.addProduct(product);

        Integer productId = databaseHelper.findProductId(productExternalId);
        productEntity.setId(productId);

        String nextUrl = "www.gov.uk/pay";

        PaymentEntity payment_1 = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(paymentExternalId1)
                .withProduct(productEntity)
                .withNextUrl(nextUrl)
                .build();

        databaseHelper.addPayment(payment_1.toPayment());

        PaymentEntity payment_2 = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(paymentExternalId2)
                .withProduct(productEntity)
                .withNextUrl(nextUrl)
                .build();

        databaseHelper.addPayment(payment_2.toPayment());

        ValidatableResponse response = givenAuthenticatedSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/payments?productExternalId=%s", productExternalId))
                .then()
                .statusCode(200);

        String paymentsUrl = "http://localhost:8080/v1/api/payments/";
        String productsUIUrl = "http://localhost:3000/pay/";
        response
                .body("", hasSize(2))
                .body("[0]._links[0].href", matchesPattern(paymentsUrl + payment_1.getExternalId()))
                .body("[0]._links[0].method", is(HttpMethod.GET))
                .body("[0]._links[0].rel", is("self"))
                .body("[1]._links[1].href", matchesPattern(productsUIUrl + payment_2.getExternalId()))
                .body("[1]._links[1].method", is(HttpMethod.POST))
                .body("[1]._links[1].rel", is("pay"));
    }
}