-- One company per installation. Database defaults cover all existing and future ORM inserts.
create table companies (
    id binary(16) primary key,
    singleton_key tinyint not null default 1 unique,
    legal_name varchar(200) not null,
    trade_name varchar(200),
    tax_id varchar(50),
    email varchar(254),
    phone varchar(40),
    website varchar(255),
    street varchar(255),
    city varchar(100),
    region varchar(100),
    country varchar(100),
    postal_code varchar(30),
    logo_path varchar(500),
    constraint chk_companies_singleton check (singleton_key = 1),
    constraint chk_companies_installation_id check (id = 0x92000000000000000000000000000001)
);
insert into companies (id, legal_name) values (0x92000000000000000000000000000001, 'Cashly');

alter table auth_administration_lock
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_auth_administration_lock_company foreign key (company_id) references companies(id);
alter table auth_departments
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_auth_departments_company foreign key (company_id) references companies(id);
alter table auth_mfa
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_auth_mfa_company foreign key (company_id) references companies(id);
alter table auth_role_permissions
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_auth_role_permissions_company foreign key (company_id) references companies(id);
alter table auth_roles
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_auth_roles_company foreign key (company_id) references companies(id);
alter table balance_transactions
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_balance_transactions_company foreign key (company_id) references companies(id);
alter table balance_tx_sequence
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_balance_tx_sequence_company foreign key (company_id) references companies(id);
alter table cash_register_details
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_cash_register_details_company foreign key (company_id) references companies(id);
alter table cash_register_movements
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_cash_register_movements_company foreign key (company_id) references companies(id);
alter table cash_registers
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_cash_registers_company foreign key (company_id) references companies(id);
alter table categories
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_categories_company foreign key (company_id) references companies(id);
alter table credit_note_number_sequence
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_credit_note_number_sequence_company foreign key (company_id) references companies(id);
alter table credit_notes
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_credit_notes_company foreign key (company_id) references companies(id);
alter table customer_addresses
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_customer_addresses_company foreign key (company_id) references companies(id);
alter table customer_balances
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_customer_balances_company foreign key (company_id) references companies(id);
alter table customer_emails
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_customer_emails_company foreign key (company_id) references companies(id);
alter table customer_phones
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_customer_phones_company foreign key (company_id) references companies(id);
alter table customer_warehouse_access
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_customer_warehouse_access_company foreign key (company_id) references companies(id);
alter table customers
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_customers_company foreign key (company_id) references companies(id);
alter table debit_note_number_sequence
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_debit_note_number_sequence_company foreign key (company_id) references companies(id);
alter table debit_notes
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_debit_notes_company foreign key (company_id) references companies(id);
alter table invoice_line_allocations
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_invoice_line_allocations_company foreign key (company_id) references companies(id);
alter table invoice_lines
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_invoice_lines_company foreign key (company_id) references companies(id);
alter table invoice_number_sequence
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_invoice_number_sequence_company foreign key (company_id) references companies(id);
alter table invoice_order_links
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_invoice_order_links_company foreign key (company_id) references companies(id);
alter table invoice_quote_links
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_invoice_quote_links_company foreign key (company_id) references companies(id);
alter table invoices
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_invoices_company foreign key (company_id) references companies(id);
alter table notifications
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_notifications_company foreign key (company_id) references companies(id);
alter table order_line_allocations
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_order_line_allocations_company foreign key (company_id) references companies(id);
alter table order_line_category_snapshots
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_order_line_category_snapshots_company foreign key (company_id) references companies(id);
alter table order_lines
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_order_lines_company foreign key (company_id) references companies(id);
alter table order_number_sequence
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_order_number_sequence_company foreign key (company_id) references companies(id);
alter table orders
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_orders_company foreign key (company_id) references companies(id);
alter table payment_allocations
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_payment_allocations_company foreign key (company_id) references companies(id);
alter table payment_methods
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_payment_methods_company foreign key (company_id) references companies(id);
alter table payment_number_sequence
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_payment_number_sequence_company foreign key (company_id) references companies(id);
alter table payments
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_payments_company foreign key (company_id) references companies(id);
alter table price_types
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_price_types_company foreign key (company_id) references companies(id);
alter table product_code_sequence
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_product_code_sequence_company foreign key (company_id) references companies(id);
alter table product_components
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_product_components_company foreign key (company_id) references companies(id);
alter table product_prices
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_product_prices_company foreign key (company_id) references companies(id);
alter table products
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_products_company foreign key (company_id) references companies(id);
alter table purchase_line_allocations
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_purchase_line_allocations_company foreign key (company_id) references companies(id);
alter table purchase_lines
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_purchase_lines_company foreign key (company_id) references companies(id);
alter table purchase_number_sequence
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_purchase_number_sequence_company foreign key (company_id) references companies(id);
alter table purchases
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_purchases_company foreign key (company_id) references companies(id);
alter table quote_allocations
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_quote_allocations_company foreign key (company_id) references companies(id);
alter table quote_line_category_snapshots
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_quote_line_category_snapshots_company foreign key (company_id) references companies(id);
alter table quote_lines
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_quote_lines_company foreign key (company_id) references companies(id);
alter table quote_number_sequence
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_quote_number_sequence_company foreign key (company_id) references companies(id);
alter table quotes
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_quotes_company foreign key (company_id) references companies(id);
alter table refresh_tokens
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_refresh_tokens_company foreign key (company_id) references companies(id);
alter table return_cancelled_allocations
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_return_cancelled_allocations_company foreign key (company_id) references companies(id);
alter table return_line_allocations
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_return_line_allocations_company foreign key (company_id) references companies(id);
alter table return_lines
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_return_lines_company foreign key (company_id) references companies(id);
alter table return_number_sequence
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_return_number_sequence_company foreign key (company_id) references companies(id);
alter table returns
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_returns_company foreign key (company_id) references companies(id);
alter table stock_movements
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_stock_movements_company foreign key (company_id) references companies(id);
alter table supplier_addresses
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_supplier_addresses_company foreign key (company_id) references companies(id);
alter table supplier_emails
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_supplier_emails_company foreign key (company_id) references companies(id);
alter table supplier_phones
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_supplier_phones_company foreign key (company_id) references companies(id);
alter table suppliers
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_suppliers_company foreign key (company_id) references companies(id);
alter table user_module_permissions
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_user_module_permissions_company foreign key (company_id) references companies(id);
alter table users
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_users_company foreign key (company_id) references companies(id);
alter table warehouse_stock
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_warehouse_stock_company foreign key (company_id) references companies(id);
alter table warehouses
    add column company_id binary(16) not null default 0x92000000000000000000000000000001,
    add constraint fk_warehouses_company foreign key (company_id) references companies(id);

-- Existing administrators retain company configuration access; protected system administrators
-- automatically receive the permission registry independently of persisted role grants.
insert ignore into auth_role_permissions (role_id, module, action)
select distinct role_id, 'COMPANY', 'READ' from auth_role_permissions
where module = 'ROLES' and action = 'UPDATE';
insert ignore into auth_role_permissions (role_id, module, action)
select distinct role_id, 'COMPANY', 'UPDATE' from auth_role_permissions
where module = 'ROLES' and action = 'UPDATE';
