--liquibase formatted sql

--changeset uk.gov.pay:add_table-catalogues
CREATE TABLE catalogues (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(32) NOT NULL UNIQUE DEFAULT replace(uuid_generate_v4()::VARCHAR,'-',''),
    external_service_id VARCHAR(32) NOT NULL,
    name VARCHAR(255) DEFAULT 'System Generated Catalogue'
);
--rollback drop table catalogues;

