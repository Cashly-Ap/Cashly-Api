alter table payments
    add column target_invoice_id binary(16) null,
    add column voucher_path varchar(500) null;

alter table payments
    add constraint fk_payments_target_invoice foreign key (target_invoice_id) references invoices(id);

create index idx_payments_target_invoice_id on payments(target_invoice_id);
