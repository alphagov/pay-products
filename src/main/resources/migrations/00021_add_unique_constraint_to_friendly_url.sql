--liquibase formatted sql

--changeset uk.gov.pay:add_unique_constraint_on_friendly_url
ALTER TABLE products ADD UNIQUE (friendly_url);

--rollback alter table payments drop unique friendly_url;
