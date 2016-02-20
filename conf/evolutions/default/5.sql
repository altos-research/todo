# Add owner to todo list

# --- !Ups

ALTER TABLE todo_list ADD COLUMN user_id INT;

# --- !Downs

ALTER TABLE todo_list DROP user_id;
