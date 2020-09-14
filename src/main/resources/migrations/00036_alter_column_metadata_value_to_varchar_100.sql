--liquibase formatted sql

--changeset uk.gov.pay:alter_column_metadata_value
ALTER TABLE products_metadata ALTER COLUMN metadata_value TYPE VARCHAR(100);
-- rollback alter table products_metadata alter column metadata_value type varchar(50);

