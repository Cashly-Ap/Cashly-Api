alter table auth_user_preferences
    add column language varchar(2) not null default 'es',
    add constraint chk_auth_user_preferences_language check (language in ('es', 'en'));
