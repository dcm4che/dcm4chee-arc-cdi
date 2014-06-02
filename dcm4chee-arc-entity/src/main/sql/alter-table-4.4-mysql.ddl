create table issuer (
    pk bigint not null auto_increment,
    entity_id varchar(255),
    entity_uid varchar(255),
    entity_uid_type varchar(255),
    primary key (pk)
) ENGINE=InnoDB;

create unique index issuer_entity_id_idx on issuer (entity_id);
create unique index issuer_entity_uid_idx on issuer (entity_uid, entity_uid_type);

create table patient_id (
    pk bigint not null auto_increment,
    pat_id varchar(255) not null,
    issuer_fk bigint,
    patient_fk bigint,
    primary key (pk)
) ENGINE=InnoDB;

alter table patient_id 
    add constraint FK8523EC95A511AE1E 
    foreign key (patient_fk) 
    references patient (pk);

alter table patient_id 
    add constraint FK8523EC959E0B30AA 
    foreign key (issuer_fk) 
    references issuer (pk);

create table rel_linked_patient_id (
    patient_fk bigint not null,
    patient_id_fk bigint not null
) ENGINE=InnoDB;

alter table rel_linked_patient_id 
    add constraint FK268C10558B0E8FE9 
    foreign key (patient_id_fk) 
    references patient_id (pk);

alter table rel_linked_patient_id 
    add constraint FK268C1055A511AE1E 
    foreign key (patient_fk) 
    references patient (pk);

drop index pat_id_idx on patient;
create unique index pat_id_idx on patient_id (pat_id, issuer_fk);

drop table rel_pat_other_pid;
drop table other_pid;

insert patient_id (patient_fk, pat_id)
    select pk, pat_id from patient;

alter table patient 
    drop pat_id,
    drop pat_id_issuer;
