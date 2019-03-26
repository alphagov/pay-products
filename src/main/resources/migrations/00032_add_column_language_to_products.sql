--liquibase formatted sql

--changeset uk.gov.pay:add_column_language_to_products
ALTER TABLE products ADD COLUMN "language" VARCHAR(2) NULL;
ALTER TABLE products ALTER COLUMN "language" SET DEFAULT 'en';
UPDATE products SET "language" = 'en' WHERE "language" IS NULL;

--rollback ALTER TABLE products DROP COLUMN "language";
