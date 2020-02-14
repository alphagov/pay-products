--liquibase formatted sql

--changeset uk.gov.pay:add_column_amount_on_payments
ALTER TABLE products_metadata ADD COLUMN version INTEGER DEFAULT 0 NOT NULL;

--rollback alter table products_metadata drop column version;
