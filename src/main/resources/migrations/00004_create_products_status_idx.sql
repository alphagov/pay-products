--liquibase formatted sql

--changeset uk.gov.pay:add_products_status_index
CREATE INDEX status_idx ON products(status);

--rollback drop index status_idx on products
