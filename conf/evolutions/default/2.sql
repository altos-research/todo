# Tasks belong to as todo_list

# --- !Ups

CREATE SEQUENCE todo_list_id_seq;
CREATE TABLE todo_list (
    id INT NOT NULL DEFAULT nextval('todo_list_id_seq'),
    label varchar(255)
);

ALTER TABLE task ADD COLUMN list_id INT;

# --- !Downs

DROP TABLE todo_list;
DROP SEQUENCE todo_list_id_seq;

ALTER TABLE task DROP list_id;
