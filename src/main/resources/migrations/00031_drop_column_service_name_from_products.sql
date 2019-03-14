--liquibase formatted sql

--changeset uk.gov.pay:drop_column_service_name_on_products
ALTER TABLE products DROP COLUMN service_name;
