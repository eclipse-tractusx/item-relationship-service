CREATE TABLE public.part_relationship (
    oneidmanufacturer character varying(255) NOT NULL,
    objectidmanufacturer character varying(255) NOT NULL,
    parent_oneidmanufacturer character varying(255) NOT NULL,
    parent_objectidmanufacturer character varying(255) NOT NULL,
    part_relationship_list_id uuid NOT NULL,
    upload_date_time timestamp without time zone NOT NULL,
    effect_time timestamp without time zone NOT NULL,
    removed boolean DEFAULT false NOT NULL,
    life_cycle_stage character varying(255) DEFAULT 'BUILD'::character varying NOT NULL,
    version bigint DEFAULT 0 NOT NULL
);

ALTER TABLE ONLY public.part_relationship
    ADD CONSTRAINT part_relationship_pkey PRIMARY KEY (oneidmanufacturer, objectidmanufacturer, parent_oneidmanufacturer, parent_objectidmanufacturer, effect_time, removed, life_cycle_stage);
