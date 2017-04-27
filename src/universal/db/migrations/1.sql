# --- !Ups
CREATE TABLE "user" (
  id SERIAL NOT NULL,
  name VARCHAR(128) NOT NULL,
  password CHAR(60) NOT NULL,
  date_created TIMESTAMPTZ NOT NULL,
  hostname VARCHAR(255) NOT NULL,
  last_identified TIMESTAMPTZ NOT NULL,
  permissions INT NOT NULL DEFAULT 0,

  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT uq_user_name UNIQUE (name)
);

CREATE TABLE network (
  id SERIAL NOT NULL,
  name VARCHAR(255) NOT NULL,

  CONSTRAINT pk_network PRIMARY KEY (id),
  CONSTRAINT uq_network_name UNIQUE (name)
);

CREATE TABLE channel (
  id SERIAL NOT NULL,
  network_id INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  auto_join BOOLEAN NOT NULL DEFAULT true,

  CONSTRAINT pk_channel PRIMARY KEY (id),
  CONSTRAINT uq_channel_network UNIQUE (network_id, name),
  CONSTRAINT fk_channel_network FOREIGN KEY (network_id) REFERENCES network(id)
);

# --- !Downs
DROP TABLE network;
DROP TABLE channel;
DROP TABLE "user";
