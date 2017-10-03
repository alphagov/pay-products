--liquibase formatted sql

--changeset uk.gov.pay:add_table-charges
CREATE TABLE charges (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(32) NOT NULL UNIQUE DEFAULT replace(uuid_generate_v4()::VARCHAR,'-',''),
    product_id VARCHAR(32) NOT NULL,
    price BIGINT NOT NULL,
    date_created TIMESTAMP WITH TIME ZONE DEFAULT (now() AT TIME ZONE 'utc') NOT NULL,
    status VARCHAR(32) NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table charges;

