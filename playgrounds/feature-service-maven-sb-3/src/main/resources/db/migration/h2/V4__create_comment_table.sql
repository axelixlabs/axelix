create sequence comment_id_seq start with 100 increment by 50;

create table comments
(
    id         bigint       not null default nextval('comment_id_seq'),
    feature_id bigint       not null references features (id),
    created_by varchar(255) not null,
    content    text         not null,
    created_at timestamp    not null default current_timestamp,
    primary key (id)
);
