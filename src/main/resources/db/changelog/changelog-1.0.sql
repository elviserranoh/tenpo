--liquibase formatted sql

--changeset elvis.serrano:call_history_changelog.0.1 context:dev,prod,test
--comment call_history creation tag: call_history_changelog.0.1

--
-- call_history
--

CREATE TABLE call_history (
    "id" uuid NOT NULL,
    "endpoint" character varying(255) NOT NULL,
    "parameters" character varying(255) NOT NULL,
    "message_or_error" text NOT NULL
);

--rollback DROP TABLE call_history
