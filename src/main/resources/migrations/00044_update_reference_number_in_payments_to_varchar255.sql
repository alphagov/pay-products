--liquibase formatted sql

--changeset uk.gov.pay:00044_update_reference_number_in_payments_to_varchar255
ALTER TABLE payments ALTER COLUMN reference_number TYPE VARCHAR(255);
