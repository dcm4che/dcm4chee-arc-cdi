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
    i_name_prefix varchar(255),
    i_name_suffix varchar(255),
    middle_name varchar(255),
    p_family_name varchar(255),
    p_given_name varchar(255),
    p_middle_name varchar(255),
    p_name_prefix varchar(255),
    p_name_suffix varchar(255),
    name_prefix varchar(255),
    name_suffix varchar(255),
    primary key (pk)
) ENGINE=InnoDB;


create table soundex_code (
    pk bigint not null auto_increment,
    sx_code_value varchar(255) not null,
    sx_pn_comp_part integer not null,
    sx_pn_comp integer not null,
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
create index sx_pn_comp_idx on soundex_code (sx_pn_comp);
create index sx_pn_comp_part_idx on soundex_code (sx_pn_comp_part);

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

alter table study
    change num_instances num_instances1 integer not null,
    change num_instances_a num_instances2 integer not null,
    change num_series num_series1 integer not null,
    change num_series_a num_series2 integer not null,
    add num_instances3 integer,
    add num_series3 integer;

update study set num_instances3 = -1, num_series3 = -1;

alter table study
    modify num_instances3 integer not null,
    modify num_series3 integer not null;

alter table series
    change num_instances num_instances1 integer not null,
    change num_instances_a num_instances2 integer not null,
    add num_instances3 integer;

update series set num_instances3 = -1;

alter table series
    modify num_instances3 integer not null;

alter table file_ref
    add file_status integer;

update file_ref set status = 0;

alter table file_ref
    modify file_status integer not null;

update file_ref, instance
    set file_ref.file_status=2, file_ref.instance_fk=null
    where instance.replaced=true and file_ref.instance_fk=instance.pk;

delete from instance
    where instance.replaced=true;

alter table instance
    drop replaced;

drop index inst_sop_iuid_idx on instance;

create unique index inst_sop_iuid_idx on instance (sop_iuid);

create dicomattrs (
    pk bigint not null auto_increment,
    attrs longblob not null,
    patient_fk bigint,
    study_fk bigint,
    series_fk bigint,
    instance_fk bigint,
    mpps_fk bigint,
    mwl_item_fk bigint,
    primary key (pk)
) ENGINE=InnoDB;

alter table patient
    add dicomattrs_fk bigint;

alter table study
    add dicomattrs_fk bigint;

alter table series
    add dicomattrs_fk bigint;

alter table instance
    add dicomattrs_fk bigint;

alter table mwl_item
    add dicomattrs_fk bigint;

alter table mpps
    add dicomattrs_fk bigint;

alter table patient 
    add constraint FKD0D3EB0585AF69D8 
    foreign key (dicomattrs_fk) 
    references dicomattrs (pk);

alter table study 
    add constraint FK68B0DC985AF69D8 
    foreign key (dicomattrs_fk) 
    references dicomattrs (pk);

alter table series 
    add constraint FKCA01FE7785AF69D8 
    foreign key (dicomattrs_fk) 
    references dicomattrs (pk);

alter table instance 
    add constraint FK2116949585AF69D8 
    foreign key (dicomattrs_fk) 
    references dicomattrs (pk);

alter table mpps 
    add constraint FK333EE685AF69D8 
    foreign key (dicomattrs_fk) 
    references dicomattrs (pk);

alter table mwl_item 
    add constraint FK8F9D3D3085AF69D8 
    foreign key (dicomattrs_fk) 
    references dicomattrs (pk);

insert into dicomattrs (attrs, patient_fk)
    select pat_attrs, pk from patient;

insert into dicomattrs (attrs, study_fk)
    select study_attrs, pk from study;

insert into dicomattrs (attrs, series_fk)
    select series_attrs, pk from series;

insert into dicomattrs (attrs, instance_fk)
    select inst_attrs, pk from instance;

insert into dicomattrs (attrs, mpps_fk)
    select mpps_attrs, pk from mpps;

insert into dicomattrs (attrs, mwl_item_fk)
    select item_attrs, pk from mwl_item;

update patient, dicomattrs
    set dicomattrs_fk = dicomattrs.pk
    where patient.pk = dicomattrs.patient_fk;

update study, dicomattrs
    set dicomattrs_fk = dicomattrs.pk
    where study.pk = dicomattrs.study_fk;

update series, dicomattrs
    set dicomattrs_fk = dicomattrs.pk
    where series.pk = dicomattrs.series_fk;

update instance, dicomattrs
    set dicomattrs_fk = dicomattrs.pk
    where instance.pk = dicomattrs.instance_fk;

update mpps, dicomattrs
    set dicomattrs_fk = dicomattrs.pk
    where mpps.pk = dicomattrs.mpps_fk;

update mwl_item, dicomattrs
    set dicomattrs_fk = dicomattrs.pk
    where mwl_item.pk = dicomattrs.mwl_item_fk;

alter table dicomattrs
    drop patient_fk,
    drop study_fk,
    drop series_fk,
    drop instance_fk,
    drop mpps_fk,
    drop mwl_item_fk;

alter table issuer
    rename id_issuer;

