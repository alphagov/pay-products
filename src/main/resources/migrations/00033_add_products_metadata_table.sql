--liquibase formatted sql 

--changeset uk.gov.pay:add_products_metadata_table
CREATE TABLE products_metadata (
    id SERIAL PRIMARY KEY,
    product_id INTEGER,
    metadata_key VARCHAR(30) NOT NULL,
    metadata_value VARCHAR(50) NOT NULL,
    CONSTRAINT unq_product_id_metadata_key UNIQUE (product_id, metadata_key)
);
--rollback drop table products_metadata;

--changeset uk.gov.pay:add_fk_products_metadata_products
ALTER TABLE products_metadata ADD CONSTRAINT fk_products_metadata_products FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE;
