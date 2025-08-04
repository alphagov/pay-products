package uk.gov.pay.products.utils;

import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.model.Product;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;

public class DatabaseTestHelper {

    private final Jdbi jdbi;

    public DatabaseTestHelper(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void addProduct(Product product) {
        jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO products " +
                "(external_id, name, description, pay_api_token, price, " +
                "status, return_url, type, gateway_account_id, " +
                "service_name_path, product_name_path, reference_enabled, " +
                "reference_label, reference_hint, language, require_captcha, date_created) " +
                "VALUES " +
                "(:external_id, :name, :description, :pay_api_token, :price, " +
                ":status, :return_url, :type, :gateway_account_id, " +
                ":service_name_path, :product_name_path, :reference_enabled, " +
                ":reference_label, :reference_hint, :language, :require_captcha, :date_created" +
                ")")
                .bind("external_id", product.getExternalId())
                .bind("name", product.getName())
                .bind("description", product.getDescription())
                .bind("pay_api_token", product.getPayApiToken())
                .bind("price", product.getPrice())
                .bind("status", product.getStatus())
                .bind("return_url", product.getReturnUrl())
                .bind("type", product.getType())
                .bind("gateway_account_id", product.getGatewayAccountId())
                .bind("service_name_path", product.getServiceNamePath())
                .bind("product_name_path", product.getProductNamePath())
                .bind("reference_enabled", product.getReferenceEnabled())
                .bind("reference_label", product.getReferenceLabel())
                .bind("reference_hint", product.getReferenceHint())
                .bind("language", product.getLanguage().toString())
                .bind("require_captcha", product.isRequireCaptcha())
                .bind("date_created", getFixedDateTime())
                .execute());

    }

    public ZonedDateTime getFixedDateTime() {
        Clock clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00.00Z"), UTC);
        return ZonedDateTime.ofInstant(clock.instant(), UTC);
    }

    public void addPayment(Payment payment, Integer gatewayAccountId) {
        jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO payments " +
                "(external_id, govuk_payment_id, next_url, product_id, status, amount, gateway_account_id, reference_number, date_created)" +
                "VALUES " +
                "(:external_id, :govuk_payment_id, :next_url, :product_id, :status, :amount, :gateway_account_id, :reference_number, :date_created)")
                .bind("external_id", payment.getExternalId())
                .bind("govuk_payment_id", payment.getGovukPaymentId())
                .bind("next_url", payment.getNextUrl())
                .bind("product_id", payment.getProductId())
                .bind("status", payment.getStatus())
                .bind("amount", payment.getAmount())
                .bind("date_created", payment.getDateCreated())
                .bind("gateway_account_id", gatewayAccountId)
                .bind("reference_number", payment.getReferenceNumber())
                .execute());

    }

    public Integer findProductId(String externalId) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT id " +
                "FROM products WHERE external_id = :externalId")
                .bind("externalId", externalId)
                .mapTo(Integer.class)
                .findOnly());
    }

    public List<Map<String, Object>> getPaymentsByProductExternalId(String productExternalId) {
        return jdbi.withHandle(h ->
                h.createQuery("SELECT pa.id, pa.external_id, pa.govuk_payment_id, pa.next_url, pa.date_created, pa.product_id, pa.status, pa.amount, pa.reference_number " +
                        "FROM payments pa, products pr " +
                        "WHERE pr.id = pa.product_id " +
                        "AND pr.external_id = :product_external_id")
                        .bind("product_external_id", productExternalId)
                        .mapToMap()
                        .list());
    }

    public List<Map<String, Object>> findProductEntityByGatewayAccountId(Integer gatewayAccountId) {
        return jdbi.withHandle(h ->
                h.createQuery("SELECT * FROM products " +
                        "WHERE gateway_account_id = :gateway_account_id")
                        .bind("gateway_account_id", gatewayAccountId)
                        .mapToMap()
                        .list());
    }

    public Optional<Map<String, Object>> findProductEntityByExternalId(String productExternalId) {
        return jdbi.withHandle(h ->
                h.createQuery("SELECT * FROM products " +
                                "WHERE external_id = :external_id")
                        .bind("external_id", productExternalId)
                        .mapToMap()
                        .findFirst());
    }

    public List<Map<String, Object>> findMetadataByProductExternalId(String productExternalId) {
        return jdbi.withHandle(h ->
                h.createQuery("SELECT m.* FROM products_metadata m, products p " +
                        "WHERE m.product_id = p.id AND p.external_id = :productExternalId")
                .bind("productExternalId", productExternalId)
                .mapToMap()
                .list());
    }

    public void addMetadata(String productExternalId, String key, String value) {
        jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO products_metadata " +
                "(product_id, metadata_key, metadata_value) " +
                "VALUES((SELECT id FROM products p WHERE p.external_id = :productExternalId), :key, :value)")
                    .bind("productExternalId", productExternalId)
                    .bind("key", key)
                    .bind("value", value)
                    .execute());
    }

    public void truncateAllData() {
        jdbi.withHandle(handle -> handle.execute("TRUNCATE TABLE products CASCADE"));
        jdbi.withHandle(handle -> handle.execute("TRUNCATE TABLE payments CASCADE"));
    }
}
