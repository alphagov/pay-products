--liquibase formatted sql

--changeset uk.gov.pay:add_column_product_name_path_to_products
ALTER TABLE products ADD COLUMN product_name_path VARCHAR(255);

--rollback alter table products drop column product_name_path;
