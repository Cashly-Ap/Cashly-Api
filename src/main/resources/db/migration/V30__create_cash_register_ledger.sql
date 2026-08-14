alter table cash_registers drop index closure_date;

update cash_registers
set status = 'CLOSED',
    closed_by = opened_by,
    closed_by_name = opened_by_name,
    closed_at = coalesce(updated_at, opened_at),
    closing_notes = concat_ws('\n', closing_notes, 'Sesión histórica normalizada por la migración de caja')
where status = 'OPEN'
  and id <> (select id from (
      select id from cash_registers where status = 'OPEN' order by opened_at desc limit 1
  ) latest_open);

alter table cash_registers
    add column opening_balance decimal(15,2) not null default 0 after opened_at,
    add column total_income decimal(15,2) not null default 0 after difference,
    add column total_expense decimal(15,2) not null default 0 after total_income,
    add column open_slot tinyint generated always as (case when status = 'OPEN' then 1 else null end) stored,
    add unique index uq_cash_register_single_open (open_slot);

update cash_registers
set total_income = total_payments + total_advances,
    total_expense = total_returns + total_credit_notes + total_debit_notes;

create table cash_register_movements (
    id binary(16) primary key,
    register_id binary(16) not null,
    direction varchar(10) not null,
    type varchar(40) not null,
    amount decimal(15,2) not null,
    payment_method_id binary(16) null,
    payment_method_name varchar(100) null,
    description varchar(500) not null,
    source_type varchar(40) not null,
    source_id binary(16) null,
    created_by binary(16) null,
    created_by_name varchar(200) null,
    occurred_at datetime not null,
    constraint fk_cash_movement_register foreign key (register_id) references cash_registers(id),
    constraint fk_cash_movement_payment_method foreign key (payment_method_id) references payment_methods(id),
    constraint fk_cash_movement_created_by foreign key (created_by) references users(id),
    constraint chk_cash_movement_amount check (amount > 0),
    index idx_cash_movement_register_occurred (register_id, occurred_at),
    unique index uq_cash_movement_source (source_type, source_id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

insert into cash_register_movements (
    id, register_id, direction, type, amount, payment_method_id, payment_method_name,
    description, source_type, source_id, created_by, created_by_name, occurred_at
)
select uuid_to_bin(uuid()), cr.id,
       case when p.status = 'REFUND' then 'OUT' else 'IN' end,
       case when p.status = 'REFUND' then 'REFUND' else 'PAYMENT' end,
       p.amount, p.payment_method_id, coalesce(pm.name, 'Sin método'),
       coalesce(nullif(p.notes, ''), concat('Pago ', p.payment_number)),
       'PAYMENT', p.id, p.created_by, p.created_by_name, p.created_at
from cash_registers cr
join payments p on p.created_at >= cr.opened_at
    and p.status in ('APPLIED', 'REFUND')
    and p.source = 'PAYMENT_METHOD'
left join payment_methods pm on pm.id = p.payment_method_id
where cr.status = 'OPEN';

insert into cash_register_movements (
    id, register_id, direction, type, amount, payment_method_id, payment_method_name,
    description, source_type, source_id, created_by, created_by_name, occurred_at
)
select uuid_to_bin(uuid()), cr.id,
       case when bt.type = 'REFUND' then 'OUT' else 'IN' end,
       case when bt.type = 'REFUND' then 'REFUND' else 'ADVANCE' end,
       abs(bt.amount), null, 'Sin método',
       coalesce(nullif(bt.number, ''), case when bt.type = 'REFUND' then 'Devolución de saldo' else 'Anticipo histórico' end),
       'CUSTOMER_BALANCE', bt.id, bt.created_by, null, bt.created_at
from cash_registers cr
join balance_transactions bt on bt.created_at >= cr.opened_at
    and bt.type in ('ADVANCED', 'REFUND')
where cr.status = 'OPEN';
