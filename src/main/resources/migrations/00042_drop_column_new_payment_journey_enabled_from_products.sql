--liquibase formatted sql

--changeset uk.gov.pay:drop_column_new_payment_link_journey_enabled_on_products
ALTER TABLE products DROP COLUMN new_payment_link_journey_enabled;
