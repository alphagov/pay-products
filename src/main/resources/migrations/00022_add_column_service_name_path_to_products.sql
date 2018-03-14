--liquibase formatted sql

--changeset uk.gov.pay:add_column_service_name_path_to_products
ALTER TABLE products ADD COLUMN service_name_path VARCHAR(255);

--rollback alter table products drop column service_name_path;
