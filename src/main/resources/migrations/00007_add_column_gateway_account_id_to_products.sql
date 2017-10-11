--liquibase formatted sql

--changeset uk.gov.pay:add_column_gateway_account_id_on_products
ALTER TABLE products ADD COLUMN gateway_account_id BIGINT NOT NULL DEFAULT 0;

--rollback alter table products drop column gateway_account_id;
