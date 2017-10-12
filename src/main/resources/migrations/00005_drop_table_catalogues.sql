--liquibase formatted sql

--changeset uk.gov.pay:drop_table_catalogues
ALTER TABLE products DROP  CONSTRAINT fk_products_catalogues;

DROP TABLE catalogues;

