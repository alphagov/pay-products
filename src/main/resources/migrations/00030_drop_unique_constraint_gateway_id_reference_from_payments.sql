--liquibase formatted sql

--changeset uk.gov.pay:drop_unique_constraint_gateway_id_reference_from_payments.sql
ALTER TABLE payments DROP CONSTRAINT payments_gateway_account_id_reference_number_key;