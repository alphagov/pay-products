--liquibase formatted sql

--changeset uk.gov.pay:add_table-products
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    catalogue_id INTEGER,
    external_id VARCHAR(32) NOT NULL UNIQUE DEFAULT replace(uuid_generate_v4()::VARCHAR,'-',''),
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    pay_api_token VARCHAR(255) NOT NULL,
    price BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    date_created TIMESTAMP WITH TIME ZONE DEFAULT (now() AT TIME ZONE 'utc') NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL,
    return_url VARCHAR(255)
);
--rollback drop table products;

--changeset uk.gov.pay:add_products_catalogues_fk-catalogue_id
ALTER TABLE products ADD CONSTRAINT fk_products_catalogues FOREIGN KEY (catalogue_id) REFERENCES catalogues (id) ON DELETE CASCADE;
