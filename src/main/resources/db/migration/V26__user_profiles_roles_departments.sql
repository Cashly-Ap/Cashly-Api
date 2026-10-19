create table auth_roles (
    id binary(16) primary key,
    name varchar(100) collate utf8mb4_unicode_ci not null unique,
    description varchar(500),
    active boolean not null default true,
    version bigint not null default 0,
    system_key varchar(40) unique
);
create table auth_role_permissions (
    role_id binary(16) not null,
    module varchar(30) not null,
    action varchar(20) not null,
    primary key (role_id, module, action),
    constraint fk_role_permission_role foreign key (role_id) references auth_roles(id) on delete cascade
);
create table auth_departments (
    id binary(16) primary key,
    name varchar(100) collate utf8mb4_unicode_ci not null unique,
    description varchar(500),
    active boolean not null default true,
    version bigint not null default 0
);
create table auth_administration_lock (id integer primary key);
insert into auth_administration_lock values (1);
insert into auth_roles (id,name,description,system_key) values
    (unhex('90000000000000000000000000000001'),'System administrator','Protected recovery administrator','SYSTEM_ADMIN'),
    (unhex('90000000000000000000000000000003'),'Operator','No access until explicitly configured',null);
insert into auth_departments (id,name,description) values
    (unhex('91000000000000000000000000000001'),'Administration','Default organization department');
alter table users
    add column first_name varchar(100),
    add column last_name varchar(100),
    add column address json,
    add column phones json,
    add column role_id binary(16),
    add column department_id binary(16);
update users set first_name=user_name,last_name='Beta',phones=json_array(),
    role_id=case when role='SUPER_ADMIN' then unhex('90000000000000000000000000000001') else unhex('90000000000000000000000000000003') end,
    department_id=unhex('91000000000000000000000000000001');
alter table users
    modify first_name varchar(100) not null,
    modify last_name varchar(100) not null,
    modify role_id binary(16) not null,
    modify department_id binary(16) not null,
    add constraint fk_users_access_role foreign key (role_id) references auth_roles(id),
    add constraint fk_users_department foreign key (department_id) references auth_departments(id);
