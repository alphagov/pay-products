--liquibase formatted sql

--changeset uk.gov.pay:add_column_type_on_products
ALTER TABLE products ADD COLUMN type VARCHAR(255) NOT NULL DEFAULT 'PROTOTYPE';

--rollback alter table products drop column type;
