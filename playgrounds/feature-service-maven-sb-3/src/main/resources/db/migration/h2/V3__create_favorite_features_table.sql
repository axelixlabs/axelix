create sequence favorite_features_id_seq start with 100 increment by 50;

create table favorite_features(
    id bigint not null default nextval('favorite_features_id_seq'),
    feature_id bigint references features(id) not null,
    user_id varchar(255) not null,
    PRIMARY KEY (id),
    UNIQUE (feature_id, user_id)
);
