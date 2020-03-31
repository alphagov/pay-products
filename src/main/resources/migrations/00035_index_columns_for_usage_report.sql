--liquibase formatted sql

--changeset uk.gov.pay:index_type_products runInTransaction:false
CREATE INDEX CONCURRENTLY products_type_idx ON products(type);
-- rollback drop index concurrently products_type_idx

--changeset uk.gov.pay:index_product_id_date_created_payments runInTransaction:false
CREATE INDEX CONCURRENTLY payments_product_id_date_created_idx ON payments(product_id, date_created);
-- rollback drop index concurrently payments_product_id_date_created_idx
