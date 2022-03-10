CREATE TABLE public.part_attribute (
    oneidmanufacturer character varying(255) NOT NULL,
    objectidmanufacturer character varying(255) NOT NULL,
    attribute character varying(255) NOT NULL,
    value character varying(255) NOT NULL,
    effect_time timestamp without time zone NOT NULL,
    last_modified_time timestamp without time zone NOT NULL
);

ALTER TABLE ONLY public.part_attribute
    ADD CONSTRAINT part_attribute_pkey PRIMARY KEY (oneidmanufacturer, objectidmanufacturer, attribute);

