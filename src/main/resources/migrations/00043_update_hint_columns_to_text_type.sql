--liquibase formatted sql

--changeset uk.gov.pay:update_reference_hint_column_to_text_type
ALTER TABLE products ALTER COLUMN reference_hint TYPE text;

--changeset uk.gov.pay:update_amount_hint_column_to_text_type
ALTER TABLE products ALTER COLUMN amount_hint TYPE text;