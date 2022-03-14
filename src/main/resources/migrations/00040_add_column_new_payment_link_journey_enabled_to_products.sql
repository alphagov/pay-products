--liquibase formatted sql

--changeset uk.gov.pay:add_column_new_payment_link_journey_enabled_to_products
ALTER TABLE products ADD COLUMN new_payment_link_journey_enabled BOOLEAN DEFAULT false NOT NULL;

--rollback alter table products drop column new_payment_link_journey_enabled;
