create sequence product_id_seq start with 100 increment by 50;

create table products
(
    id          bigint       not null default nextval('product_id_seq'),
    code        varchar(50)  not null unique,
    prefix      varchar(10)  not null unique,
    name        varchar(255) not null unique,
    description text,
    image_url   varchar(255) not null,
    disabled    boolean      not null default false,
    created_by  varchar(255) not null,
    created_at  timestamp    not null default current_timestamp,
    updated_by  varchar(255),
    updated_at  timestamp,
    primary key (id)
);

create sequence release_id_seq start with 100 increment by 50;

create table releases
(
    id          bigint       not null default nextval('release_id_seq'),
    product_id  bigint       not null,
    code        varchar(50)  not null unique,
    description text,
    status      varchar(50)  not null,
    released_at timestamp,
    created_by  varchar(255) not null,
    created_at  timestamp    not null default current_timestamp,
    updated_by  varchar(255),
    updated_at  timestamp,
    primary key (id),
    constraint fk_releases_product_id foreign key (product_id) references products (id)
);

create sequence feature_id_seq start with 100 increment by 50;

create table features
(
    id          bigint       not null default nextval('feature_id_seq'),
    code        varchar(50)  not null unique,
    title       varchar(500) not null,
    description text,
    status      varchar(50)  not null,
    assigned_to varchar(255),
    product_id  bigint       not null,
    release_id  bigint,
    created_by  varchar(255) not null,
    created_at  timestamp    not null default current_timestamp,
    updated_by  varchar(255),
    updated_at  timestamp,
    primary key (id),
    constraint fk_features_product_id foreign key (product_id) references products (id),
    constraint fk_features_release_id foreign key (release_id) references releases (id)
);

create sequence feature_code_seq start with 100 increment by 1;
