--liquibase formatted sql

--changeset uk.gov.pay:add_column_reference_label_to_products
ALTER TABLE products ADD COLUMN reference_label VARCHAR(50);

--rollback alter table products drop column reference_label;
