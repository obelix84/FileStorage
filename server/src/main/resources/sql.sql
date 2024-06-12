CREATE DATABASE filestorage
    WITH 
    OWNER = filestorage
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;
	
CREATE SCHEMA main
    AUTHORIZATION filestorage;
	
CREATE TABLE main.users
(
    id serial NOT NULL,
    login character varying(50) NOT NULL,
    password character varying(50) NOT NULL,
    default_dir character varying(100),
    read boolean,
    write boolean,
    management boolean,
    PRIMARY KEY (id)
);

ALTER TABLE main.users
    OWNER to filestorage;
	
CREATE TABLE main.files
(
    id serial NOT NULL,
    filename character varying(255) NOT NULL,
    sub_dir character varying(248),
    size bigint NOT NULL,
    owner_id serial NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT "FK_users" FOREIGN KEY (owner_id)
        REFERENCES main.users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
);

ALTER TABLE main.files
    OWNER to filestorage;
	
ALTER TABLE main.files ADD CONSTRAINT UQ_file_owner UNIQUE (filename, owner_id)