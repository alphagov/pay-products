package uk.gov.pay.products.utils;

import org.skife.jdbi.v2.DBI;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.model.Product;

import java.util.List;
import java.util.Map;

public class DatabaseTestHelper {

    private DBI jdbi;

    public DatabaseTestHelper(DBI jdbi) {
        this.jdbi = jdbi;
    }

    public DatabaseTestHelper addProduct(Product product) {
        jdbi.withHandle(handle -> handle.createStatement("INSERT INTO products " +
                "(external_id, name, description, pay_api_token, price, " +
                "status, return_url, gateway_account_id)" +
                "VALUES " +
                "(:external_id, :name, :description, :pay_api_token, :price, " +
                ":status, :return_url, :gateway_account_id)")
                .bind("external_id", product.getExternalId())
                .bind("name", product.getName())
                .bind("description", product.getDescription())
                .bind("pay_api_token", product.getPayApiToken())
                .bind("price", product.getPrice())
                .bind("status", product.getStatus())
                .bind("return_url", product.getReturnUrl())
                .bind("gateway_account_id", product.getGatewayAccountId())
                .execute());

        return this;
    }

    public DatabaseTestHelper addPayment(Payment payment, Integer gatewayAccountId) {
        jdbi.withHandle(handle -> handle.createStatement("INSERT INTO payments " +
                "(external_id, govuk_payment_id, next_url, product_id, status, amount, gateway_account_id, reference_number)" +
                "VALUES " +
                "(:external_id, :govuk_payment_id, :next_url, :product_id, :status, :amount, :gateway_account_id, :reference_number)")
                .bind("external_id", payment.getExternalId())
                .bind("govuk_payment_id", payment.getGovukPaymentId())
                .bind("next_url", payment.getNextUrl())
                .bind("product_id",payment.getProductId())
                .bind("status", payment.getStatus())
                .bind("amount", payment.getAmount())
                .bind("gateway_account_id", gatewayAccountId)
                .bind("reference_number", payment.getReferenceNumber())
                .execute());

        return this;
    }

    public Integer findProductId(String externalId){
        return jdbi.withHandle(handle -> handle.createQuery("SELECT id " +
                "FROM products WHERE external_id = :externalId")
                .bind("externalId", externalId)
                .mapTo(Integer.class)
                .first());
    }

    public List<Map<String, Object>> getPaymentsByProductExternalId(String productExternalId) {
        return jdbi.withHandle(h ->
                h.createQuery("SELECT pa.id, pa.external_id, pa.govuk_payment_id, pa.next_url, pa.date_created, pa.product_id, pa.status, pa.amount, pa.reference_number " +
                        "FROM payments pa, products pr " +
                        "WHERE pr.id = pa.product_id " +
                        "AND pr.external_id = :product_external_id")
                        .bind("product_external_id", productExternalId)
                        .list());
    }

}
