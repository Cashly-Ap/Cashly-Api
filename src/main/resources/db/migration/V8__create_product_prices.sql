create table product_prices (
    id binary(16) not null,
    product_id binary(16) not null,
    price_type_id binary(16) not null,
    price decimal(19, 4) not null,
    profit_margin decimal(5,2) null,
    primary key (id),
    constraint fk_product_prices_product foreign key (product_id) references products(id) on delete cascade,
    constraint fk_product_prices_price_type foreign key (price_type_id) references price_types(id),
    constraint uq_product_price_type unique (product_id, price_type_id)
);

create index idx_product_prices_product_id on product_prices(product_id);
create index idx_product_prices_price_type_id on product_prices(price_type_id);
