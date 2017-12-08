--liquibase formatted sql

--changeset uk.gov.pay:add_column_reference_number_on_payments
ALTER TABLE payments ADD COLUMN reference_number VARCHAR(16);

--rollback alter table payments drop column reference_number;

