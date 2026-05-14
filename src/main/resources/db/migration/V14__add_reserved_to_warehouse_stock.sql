alter table warehouse_stock add column reserved int not null default 0;
create index idx_warehouse_stock_reserved on warehouse_stock(reserved);
