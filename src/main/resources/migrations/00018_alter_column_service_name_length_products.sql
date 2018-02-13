--liquibase formatted sql

--changeset uk.gov.pay:alter_column_service_name_length_on_products
ALTER TABLE products ALTER COLUMN service_name TYPE VARCHAR(50);

--rollback ALTER TABLE products ALTER COLUMN service_name TYPE VARCHAR(255);
