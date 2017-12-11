--liquibase formatted sql

--changeset uk.gov.pay:update_non_existestent_reference_ids_in_payments

UPDATE payments SET reference_number = upper(substring(replace(uuid_generate_v4()::VARCHAR,'-',''), 1, 10));
