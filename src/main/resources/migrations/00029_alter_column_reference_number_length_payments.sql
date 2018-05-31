--liquibase formatted sql

--changeset uk.gov.pay:alter_column_reference_number_length_on_payments
ALTER TABLE payments ALTER COLUMN reference_number TYPE VARCHAR(50);

--rollback ALTER TABLE payments ALTER COLUMN reference_number TYPE VARCHAR(16);
