    alter table instance
        add reject_code_fk bigint;

    alter table instance
        add constraint FK2116949540F8410A 
        foreign key (reject_code_fk) 
        references code (pk);

    create index inst_reject_code_fk_idx on instance (reject_code_fk);
