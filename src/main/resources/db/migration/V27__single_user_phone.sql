alter table users add column phone varchar(16) null;

update users
set phone = nullif(json_unquote(json_extract(phones, '$[0].number')), 'null')
where json_length(phones) > 0;

alter table users drop column phones;
