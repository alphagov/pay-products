--liquibase formatted sql

--changeset uk.gov.pay:add_unique_two_column_constraint_to_products_for_product_path
ALTER TABLE products ADD UNIQUE (service_name_path, product_name_path);

--rollback alter table payments drop unique (service_name_path, product_name_path);
