--liquibase formatted sql

--changeset uk.gov.pay:add_catalogues_external-service-id_index
CREATE INDEX external_service_id_idx ON catalogues(external_service_id);

--rollback drop index external_service_id_idx on catalogues