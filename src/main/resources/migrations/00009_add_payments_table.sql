--liquibase formatted sql

--changeset uk.gov.pay:add_table-payments
CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(32) NOT NULL UNIQUE DEFAULT replace(uuid_generate_v4()::VARCHAR,'-',''),
    product_id INTEGER NOT NULL,
    govuk_payment_id VARCHAR(255),
    next_url VARCHAR(255),
    date_created TIMESTAMP WITH TIME ZONE DEFAULT (now() AT TIME ZONE 'utc') NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL,
    status VARCHAR(255) NOT NULL
);
--rollback drop table payments;

--changeset uk.gov.pay:add_payments_products_fk-product_external_id
ALTER TABLE payments ADD CONSTRAINT fk_payments_products FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE;

--changeset uk.gov.pay:add_index_to_payment_external_id_on_payments
CREATE INDEX payment_external_idx ON payments(external_id);

--rollback drop index payment_external_idx on payments
