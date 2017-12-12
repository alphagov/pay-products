--liquibase formatted sql

--changeset uk.gov.pay:add_two_column_unique_constraint_on_payments
ALTER TABLE payments ADD UNIQUE (gateway_account_id, reference_number);

--rollback alter table payments drop unique (gateway_account_id, reference_number);
