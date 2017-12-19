--liquibase formatted sql

--changeset uk.gov.pay:add_column_service_name_on_products
ALTER TABLE products ADD COLUMN service_name VARCHAR(255);

--rollback alter table products drop column service_name;
