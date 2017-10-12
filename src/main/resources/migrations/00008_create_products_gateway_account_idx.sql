--liquibase formatted sql

--changeset uk.gov.pay:add_index_to_gateway_account_id_on_products
CREATE INDEX gateway_account_idx ON products(gateway_account_id);

--rollback drop index gateway_account_idx on products
