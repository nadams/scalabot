# --- !Ups
CREATE TABLE User (
  UserId INTEGER PRIMARY KEY,
  Name TEXT NOT NULL UNIQUE
);

# --- !Downs
DROP TABLE User;
