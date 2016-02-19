# Populate user table

# --- !Ups

INSERT INTO users (name)
VALUES ('andy'), ('bob'), ('chris'), ('dan');

# --- !Downs

DELETE users;
