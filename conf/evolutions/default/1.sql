# --- !Ups

create table "user" ("id" BIGSERIAL NOT NULL PRIMARY KEY,
                     "username" VARCHAR(254),
                     "name" VARCHAR(254),
                     "email" VARCHAR(254),
                     "birthdate" DATE,
                     "password" VARCHAR(254),
                     "photo" VARCHAR(254)
);

create table "watchListItem" ("id" BIGSERIAL NOT NULL PRIMARY KEY,
                            "idUser" BIGINT,
                            "idGame" BIGINT,
                            "creationDate" TIMESTAMP,
                            "threshold" INTEGER
);

create table "offer" ("id" BIGSERIAL NOT NULL PRIMARY KEY,
                    "link" VARCHAR(254),
                    "idGame" BIGINT,
                    "idPlatform" BIGINT,
                    "fromDate" TIMESTAMP,
                    "untilDate" TIMESTAMP,
                    "normalPrice" DOUBLE PRECISION,
                    "offerPrice" DOUBLE PRECISION
);

create table "game" ("id" BIGSERIAL NOT NULL PRIMARY KEY,
                   "name" VARCHAR(254),
                   "cover" VARCHAR(254),
                   "publisher" VARCHAR(254),
                   "rating" VARCHAR(254),
                   "releaseDate" DATE,
                   "typeGame" VARCHAR(254)
);

create table "gameCategory" ("id" BIGSERIAL NOT NULL PRIMARY KEY,
                           "idGame" BIGINT,
                           "idCategory" BIGINT
);

create table "category" ("id" BIGSERIAL NOT NULL PRIMARY KEY,
                       "name" VARCHAR(254)
);

create table "gameGenre" ("id" BIGSERIAL NOT NULL PRIMARY KEY,
                        "idGame" BIGINT,
                        "idGenre" BIGINT
);

create table "genre" ("id" BIGSERIAL NOT NULL PRIMARY KEY,
                    "name" VARCHAR(254)
);

create table "gamePlatform" ("id" BIGSERIAL NOT NULL PRIMARY KEY,
                           "idGame" BIGINT,
                           "idPlatform" BIGINT
);

create table "platform" ("id" BIGSERIAL NOT NULL PRIMARY KEY,
                       "name" VARCHAR(254)
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