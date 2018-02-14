--liquibase formatted sql

--changeset uk.gov.pay:alter_column_service_name_length_on_products
ALTER TABLE products ALTER COLUMN price DROP NOT NULL;

--rollback ALTER TABLE products ALTER COLUMN price SET NOT NULL;
