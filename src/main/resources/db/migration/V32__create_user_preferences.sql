create table auth_user_preferences (
    user_id binary(16) primary key,
    company_id binary(16) not null default 0x92000000000000000000000000000001,
    theme_mode varchar(10) not null default 'system',
    color_mode varchar(15) not null default 'minimal',
    shortcuts_enabled boolean not null default false,
    constraint fk_auth_user_preferences_user foreign key (user_id) references users(id) on delete cascade,
    constraint fk_auth_user_preferences_company foreign key (company_id) references companies(id),
    constraint chk_auth_user_preferences_theme check (theme_mode in ('system', 'light', 'dark')),
    constraint chk_auth_user_preferences_colors check (color_mode in ('minimal', 'informative'))
);
