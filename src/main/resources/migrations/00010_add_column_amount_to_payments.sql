--liquibase formatted sql

--changeset uk.gov.pay:add_column_amount_on_payments
ALTER TABLE payments ADD COLUMN amount BIGINT;

--rollback alter table payments drop column amount;
