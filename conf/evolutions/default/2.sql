# --- !Ups

ALTER TABLE offer ADD discount INT NOT NULL DEFAULT 0;
ALTER TABLE offer ADD visits BIGINT NOT NULL DEFAULT 0;

# --- !Downs
;
ALTER TABLE offer DROP discount;
ALTER TABLE offer DROP visits;
