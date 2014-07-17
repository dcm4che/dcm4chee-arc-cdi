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
    drop pat_id_issuer,
    add no_pat_id bit not null;

create index no_pat_id_idx on patient (no_pat_id);

alter table study
    drop accno_issuer,
    add accno_issuer_fk bigint;

alter table study
    add constraint FK68B0DC9C45E7AAD 
    foreign key (accno_issuer_fk) 
    references issuer (pk);

alter table series_req
    drop accno_issuer,
    add accno_issuer_fk bigint;

alter table series_req 
    add constraint FKE38CD2D6C45E7AAD 
    foreign key (accno_issuer_fk) 
    references issuer (pk);

create table person_name (
    pk bigint not null auto_increment,
    family_name varchar(255),
    given_name varchar(255),
    i_family_name varchar(255),
    i_given_name varchar(255),
    i_middle_name varchar(255),
    i_prefix varchar(255),
    i_suffix varchar(255),
    middle_name varchar(255),
    p_family_name varchar(255),
    p_given_name varchar(255),
    p_middle_name varchar(255),
    p_prefix varchar(255),
    p_suffix varchar(255),
    prefix varchar(255),
    suffix varchar(255),
    primary key (pk)
) ENGINE=InnoDB;


create table soundex_code (
    pk bigint not null auto_increment,
    sx_code_value varchar(255) not null,
    pn_comp_part integer not null,
    pn_comp integer not null,
    person_name_fk bigint,
    primary key (pk)
) ENGINE=InnoDB;

create index family_name_idx on person_name (family_name);
create index given_name_idx on person_name (given_name);
create index middle_name_idx on person_name (middle_name);
create index i_family_name_idx on person_name (i_family_name);
create index i_given_name_idx on person_name (i_given_name);
create index i_middle_name_idx on person_name (i_middle_name);
create index p_family_name_idx on person_name (p_family_name);
create index p_given_name_idx on person_name (p_given_name);
create index p_middle_name_idx on person_name (p_middle_name);

create index sx_code_value_idx on soundex_code (sx_code_value);
create index sx_pn_comp_idx on soundex_code (pn_comp);
create index sx_pn_comp_part_idx on soundex_code (pn_comp_part);

alter table patient
    drop pat_name,
    drop pat_i_name,
    drop pat_p_name,
    drop pat_fn_sx,
    drop pat_gn_sx,
    add pat_name_fk bigint;

alter table patient 
    add constraint FKD0D3EB05E7945C3 
    foreign key (pat_name_fk) 
    references person_name (pk);

alter table study
    drop ref_physician,
    drop ref_phys_i_name,
    drop ref_phys_p_name,
    drop ref_phys_fn_sx,
    drop ref_phys_gn_sx,
    add ref_phys_name_fk bigint;

alter table study 
    add constraint FK68B0DC97F2DAD5E 
    foreign key (ref_phys_name_fk) 
    references person_name (pk);

alter table series
    drop perf_phys_name,
    drop perf_phys_i_name,
    drop perf_phys_p_name,
    drop perf_phys_fn_sx,
    drop perf_phys_gn_sx,
    add perf_phys_name_fk bigint;

alter table series 
    add constraint FKCA01FE77E53AEEC8 
    foreign key (perf_phys_name_fk) 
    references person_name (pk);

alter table series_req
    drop req_physician,
    drop req_phys_i_name,
    drop req_phys_p_name,
    drop req_phys_fn_sx,
    drop req_phys_gn_sx,
    add req_phys_name_fk bigint;

alter table series_req 
    add constraint FKE38CD2D633B55733 
    foreign key (req_phys_name_fk) 
    references person_name (pk);

alter table verify_observer
    drop observer_name,
    drop observer_i_name,
    drop observer_p_name,
    drop observer_fn_sx,
    drop observer_gn_sx,
    add observer_name_fk bigint;

alter table verify_observer 
    add constraint FKC9DB73DC661F04F6 
    foreign key (observer_name_fk) 
    references person_name (pk);

alter table mwl_item
    drop perf_phys_name,
    drop perf_phys_i_name,
    drop perf_phys_p_name,
    drop perf_phys_fn_sx,
    drop perf_phys_gn_sx,
    add perf_phys_name_fk bigint;

alter table mwl_item 
    add constraint FK8F9D3D30E53AEEC8 
    foreign key (perf_phys_name_fk) 
    references person_name (pk);

alter table soundex_code 
    add constraint FKA3E90A0A7665E75 
    foreign key (person_name_fk) 
    references person_name (pk);

