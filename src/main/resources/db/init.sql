DROP TABLE IF EXISTS "call_history";
CREATE TABLE "public"."call_history" (
                                         "id" uuid NOT NULL,
                                         "endpoint" character varying(255) NOT NULL,
                                         "parameters" character varying(255) NOT NULL,
                                         "message_or_error" text NOT NULL
) WITH (oids = false);