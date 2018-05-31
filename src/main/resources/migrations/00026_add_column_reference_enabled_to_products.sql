--liquibase formatted sql

--changeset uk.gov.pay:add_column_reference_enabled_to_products
ALTER TABLE products ADD COLUMN reference_enabled BOOLEAN NOT NULL DEFAULT FALSE;

--rollback alter table products drop column reference_enabled;
