create table auth_mfa (
    subject_id varchar(36) primary key,
    enabled boolean not null default false,
    secret varchar(255),
    setup_expires_at timestamp(6) null,
    last_step bigint not null default -1,
    recovery_hashes text,
    challenge_hash varchar(64) unique,
    challenge_expires_at timestamp(6) null,
    failures int not null default 0,
    locked_until timestamp(6) null
);
