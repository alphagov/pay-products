--liquibase formatted sql

--changeset uk.gov.pay:set_gateway_account_id_and_reference_number_not_null_on_payments
ALTER TABLE payments ALTER COLUMN reference_number SET NOT NULL;

ALTER TABLE payments ALTER COLUMN gateway_account_id SET NOT NULL;
