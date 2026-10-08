create table order_line_category_snapshots (
    order_line_id binary(16) not null primary key,
    category_id binary(16) not null,
    index idx_order_line_category_snapshot_category (category_id),
    constraint fk_order_line_category_snapshot_line
        foreign key (order_line_id) references order_lines(id) on delete cascade
);

create table quote_line_category_snapshots (
    quote_line_id binary(16) not null primary key,
    category_id binary(16) not null,
    index idx_quote_line_category_snapshot_category (category_id),
    constraint fk_quote_line_category_snapshot_line
        foreign key (quote_line_id) references quote_lines(id) on delete cascade
);

insert into order_line_category_snapshots (order_line_id, category_id)
select order_lines.id, products.category_id
from order_lines
join products on products.id = order_lines.product_id
where products.category_id is not null;

insert into quote_line_category_snapshots (quote_line_id, category_id)
select quote_lines.id, products.category_id
from quote_lines
join products on products.id = quote_lines.product_id
where products.category_id is not null;
