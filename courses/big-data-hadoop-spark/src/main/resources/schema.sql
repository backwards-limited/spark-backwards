create table if not exists example(
    course_id integer not null,
    course_name character varying collate pg_catalog."default" not null,
    author_name character varying collate pg_catalog."default" not null,
    course_section json not null,
    creation_date date not null,
    constraint example_pkey PRIMARY KEY (course_id)
);

insert into example(
    course_id, course_name, author_name, course_section, creation_date)
values (2, 'Hadoop Spark', 'Backwards', '{ "section": 1, "title": "Hadoop" }', '2020-01-13');

insert into example(
    course_id, course_name, author_name, course_section, creation_date)
values (1, 'Spring Boot Microservices', 'Backwards', '{ "section": 1, "title": "Microservices" }', '2020-02-20');

create table if not exists courses(
    course_id character varying not null,
    course_name character varying collate pg_catalog."default" not null,
    author_name character varying collate pg_catalog."default" not null,
    no_of_reviews character varying not null,
    constraint courses_pkey PRIMARY KEY (course_id)
);