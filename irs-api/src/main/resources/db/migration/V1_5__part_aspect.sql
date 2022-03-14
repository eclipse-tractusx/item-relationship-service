CREATE TABLE public.part_aspect (
    oneidmanufacturer character varying(255) NOT NULL,
    objectidmanufacturer character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    url character varying(2000) NOT NULL,
    effect_time timestamp without time zone NOT NULL,
    last_modified_time timestamp without time zone NOT NULL
);

ALTER TABLE ONLY public.part_aspect
    ADD CONSTRAINT part_aspect_pkey PRIMARY KEY (oneidmanufacturer, objectidmanufacturer, name);

