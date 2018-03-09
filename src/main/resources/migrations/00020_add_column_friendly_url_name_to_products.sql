--liquibase formatted sql

--changeset uk.gov.pay:add_column_friendly_url_on_products
ALTER TABLE products ADD COLUMN friendly_url VARCHAR(1024);

--rollback alter table products drop column friendly_url;
