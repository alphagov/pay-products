--liquibase formatted sql

--changeset uk.gov.pay:drop_column_friendly_url_from_products
ALTER TABLE products DROP COLUMN friendly_url;
