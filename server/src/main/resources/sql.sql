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