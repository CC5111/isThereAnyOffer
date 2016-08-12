# --- !Ups

create table "user" (
  "id"        BIGSERIAL NOT NULL PRIMARY KEY,
  "username"  VARCHAR(256),
  "name"      VARCHAR(256),
  "email"     VARCHAR(256),
  "birthdate" DATE,
  "password"  VARCHAR(256),
  "photo"     VARCHAR(256)
);

create table "watchListItem" (
  "id"           BIGSERIAL NOT NULL PRIMARY KEY,
  "idUser"       BIGINT,
  "idGame"       BIGINT,
  "creationDate" TIMESTAMP,
  "threshold"    INTEGER
);

create table "offer" (
  "id"          BIGSERIAL NOT NULL PRIMARY KEY,
  "link"        VARCHAR(256),
  "idGame"      BIGINT,
  "idPlatform"  BIGINT,
  "idStore"     BIGINT,
  "fromDate"    TIMESTAMP,
  "untilDate"   TIMESTAMP,
  "normalPrice" DOUBLE PRECISION,
  "offerPrice"  DOUBLE PRECISION
);

create table "game" (
  "id"          BIGSERIAL NOT NULL PRIMARY KEY,
  "name"        VARCHAR(256),
  "cover"       VARCHAR(256),
  "publisher"   VARCHAR(256),
  "developer"   VARCHAR(256),
  "link"        VARCHAR(256),
  "description" TEXT,
  "rating"      VARCHAR(256),
  "releaseDate" DATE,
  "gameType"    VARCHAR(256),
  "videoLink"  VARCHAR(256),
  "metacritic"  VARCHAR(256)
);

create table "gameCategory" (
  "id"         BIGSERIAL NOT NULL PRIMARY KEY,
  "idGame"     BIGINT,
  "idCategory" BIGINT
);

create table "category" (
  "id"   BIGSERIAL NOT NULL PRIMARY KEY,
  "name" VARCHAR(256)
);

create table "gameGenre" (
  "id"      BIGSERIAL NOT NULL PRIMARY KEY,
  "idGame"  BIGINT,
  "idGenre" BIGINT
);

create table "genre" (
  "id"   BIGSERIAL NOT NULL PRIMARY KEY,
  "name" VARCHAR(256)
);

create table "gamePlatform" (
  "id"         BIGSERIAL NOT NULL PRIMARY KEY,
  "idGame"     BIGINT,
  "idPlatform" BIGINT
);

create table "platform" (
  "id"   BIGSERIAL NOT NULL PRIMARY KEY,
  "name" VARCHAR(256)
);

create table "store" (
  "id"                        BIGSERIAL NOT NULL PRIMARY KEY,
  "name"                      VARCHAR(256),
  "borderColor"               VARCHAR(16),
  "pointBorderColor"          VARCHAR(16),
  "pointBackgroundColor"      VARCHAR(16),
  "pointHoverBackgroundColor" VARCHAR(16),
  "pointHoverBorderColor"     VARCHAR(16)
);

create table "g2aStore" (
  "id"      BIGSERIAL NOT NULL PRIMARY KEY,
  "idGame"  BIGINT,
  "idStore" VARCHAR(256)
);

create table "gogStore" (
  "id"      BIGSERIAL NOT NULL PRIMARY KEY,
  "idGame"  BIGINT,
  "idStore" VARCHAR(256)
);

create table "psStore" (
  "id"      BIGSERIAL NOT NULL PRIMARY KEY,
  "idGame"  BIGINT,
  "idStore" VARCHAR(256)
);

create table "steamStore" (
  "id"      BIGSERIAL NOT NULL PRIMARY KEY,
  "idGame"  BIGINT,
  "idStore" VARCHAR(256)
);

create table "xboxStore" (
  "id"      BIGSERIAL NOT NULL PRIMARY KEY,
  "idGame"  BIGINT,
  "idStore" VARCHAR(256)
);

# --- !Downs
;
drop table "user";
drop table "watchListItem";
drop table "offer";
drop table "game";
drop table "gameCategory";
drop table "category";
drop table "gameGenre";
drop table "genre";
drop table "gamePlatform";
drop table "platform";
drop table "store";
drop table "g2aStore";
drop table "gogStore";
drop table "psStore";
drop table "steamStore";
drop table "xboxStore";