--liquibase formatted sql

--changeset uk.gov.pay:drop_column_catalogue_id_on_products
ALTER TABLE products DROP COLUMN catalogue_id;
