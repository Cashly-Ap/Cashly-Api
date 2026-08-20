create table price_types (
    id binary(16) not null,
    name varchar(100) not null,
    description varchar(500),
    active boolean not null default true,
    created_at datetime not null,
    updated_at datetime not null,
    primary key (id),
    constraint uq_price_type_name unique (name)
);
