--liquibase formatted sql

--changeset uk.gov.pay:add_column_reference_hint_to_products
ALTER TABLE products ADD COLUMN reference_hint VARCHAR(255);

--rollback alter table products drop column reference_hint;
