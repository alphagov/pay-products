--liquibase formatted sql

--changeset uk.gov.pay:add_index_to_gateway_account_id_on_products
CREATE INDEX gateway_account_idx ON products(gateway_account_id);

--rollback alter table products drop column gateway_account_id
