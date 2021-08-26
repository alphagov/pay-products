--liquibase formatted sql

--changeset uk.gov.pay:add_column_require_captcha_to_products
ALTER TABLE products ADD COLUMN require_captcha BOOLEAN DEFAULT false NOT NULL;

--rollback alter table products drop column require_captcha;
