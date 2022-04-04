--liquibase formatted sql

--changeset uk.gov.pay:add_column_amount_hint_to_products
ALTER TABLE products ADD COLUMN amount_hint VARCHAR(255);

--rollback alter table products drop column amount_hint;
