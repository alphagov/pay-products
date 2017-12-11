--liquibase formatted sql

--changeset uk.gov.pay:migrate_gateway_account_id_from_products_to_payments
UPDATE payments SET gateway_account_id =
(SELECT gateway_account_id FROM products WHERE payments.product_id = products.id);
