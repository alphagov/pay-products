--liquibase formatted sql

--changeset uk.gov.pay:add_column_gateway_account_id_on_payments
ALTER TABLE payments ADD COLUMN gateway_account_id BIGINT;

--rollback alter table payments drop column gateway_account_id;
