--liquibase formatted sql

--changeset uk.gov.pay:add_table-products
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    catalogue_id INTEGER,
    external_id VARCHAR(32) NOT NULL UNIQUE DEFAULT replace(uuid_generate_v4()::VARCHAR,'-',''),
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    api_key VARCHAR(255) NOT NULL,
    price BIGINT NOT NULL
);
--rollback drop table products;

--changeset uk.gov.pay:add_products_catalogues_fk-catalogue_id
ALTER TABLE products ADD CONSTRAINT fk_products_catalogues FOREIGN KEY (catalogue_id) REFERENCES catalogues (id) ON DELETE CASCADE;
