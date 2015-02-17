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
    pat_id_type_code varchar(255),
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

create table study_query_attrs (
    pk bigint not null auto_increment,
    availability integer,
    ext_retr_aet varchar(255),
    mods_in_study varchar(255),
    num_instances integer,
    num_series integer,
    retrieve_aets varchar(255),
    cuids_in_study varchar(255),
    view_id varchar(255),
    study_fk bigint,
    primary key (pk)
) ENGINE=InnoDB;

create table series_query_attrs (
    pk bigint not null auto_increment,
    availability integer,
    ext_retr_aet varchar(255),
    num_instances integer,
    retrieve_aets varchar(255),
    view_id varchar(255),
    series_fk bigint,
    primary key (pk)
) ENGINE=InnoDB;

alter table study_query_attrs 
    add constraint FK1D7130F54BDB761E 
    foreign key (study_fk) 
    references study (pk);

alter table series_query_attrs 
    add constraint FK530E8CA38151AFEA 
    foreign key (series_fk) 
    references series (pk);

alter table study
    drop num_instances,
    drop num_instances_a,
    drop num_series,
    drop num_series_a,
    drop mods_in_study,
    drop cuids_in_study,
    drop retrieve_aets,
    drop ext_retr_aet,
    drop availability;

alter table series
    drop num_instances,
    drop num_instances_a,
    drop retrieve_aets,
    drop ext_retr_aet,
    drop availability;

create index study_view_id_idx on study_query_attrs(view_id);
create index series_view_id_idx on series_query_attrs(view_id);

create table mpps_emulate (
    pk bigint not null auto_increment,
    emulation_time datetime not null,
    emulator_aet varchar(255) not null,
    src_aet varchar(255) not null,
    study_iuid varchar(255) not null,
    primary key (pk)
) ENGINE=InnoDB;

create unique index mpps_emulate_study_iuid_idx on mpps_emulate(study_iuid,src_aet);
create index mpps_emulate_time_idx on mpps_emulate(emulation_time);


create table qc_action_history (
    pk bigint not null auto_increment,
    action varchar(255) not null,
    created_time datetime not null,
    primary key (pk)
) ENGINE=InnoDB;

create table qc_instance_history (
    pk bigint not null auto_increment,
    cloned bit not null,
    current_series_uid varchar(255) not null,
    current_study_uid varchar(255) not null,
    current_uid varchar(255) not null,
    next_uid varchar(255) not null,
    old_uid varchar(255) not null,
    dicomattrs_fk bigint,
    qc_series_history_fk bigint,
    primary key (pk)
) ENGINE=InnoDB;

create table qc_series_history (
    pk bigint not null auto_increment,
    old_series_uid varchar(255) not null,
    qc_study_history_fk bigint,
    dicomattrs_fk bigint,
    primary key (pk)
) ENGINE=InnoDB;

create table qc_study_history (
    pk bigint not null auto_increment,
    old_study_uid varchar(255) not null,
    next_study_uid varchar(255) not null,
    qc_action_history_fk bigint,
    dicomattrs_fk bigint,
    primary key (pk)
) ENGINE=InnoDB;

create table qc_update_history (
    pk bigint not null auto_increment,
    created_time datetime not null,
    object_uid varchar(255) not null,
    scope varchar(255) not null,
    qc_update_history_fk bigint,
    dicomattrs_fk bigint,
    primary key (pk)
) ENGINE=InnoDB;

create index qc_instance_history_old_uid_idx on qc_instance_history (old_uid);
create index qc_instance_history_next_uid_idx on qc_instance_history (next_uid);
create index qc_instance_history_current_uid_idx on qc_instance_history (current_uid);
create index qc_action_history_created_time_idx on qc_action_history (created_time);

alter table instance add version bigint ;
alter table mpps add version bigint ;
alter table mwl_item add version bigint ;
alter table patient add version bigint ;
alter table patient_id add version bigint ;
alter table series add version bigint ;
alter table study add version bigint ;

alter table file_ref rename location;

alter table location
    change filepath storage_path varchar(255) not null,
    change file_digest digest varchar(255),
    change file_size object_size bigint not null,
    change file_status status integer not null,
    change file_time_zone time_zone varchar(255),
    change file_tsuid tsuid varchar(255) not null,
    add storage_group_id varchar(255),
    add storage_id varchar(255),
    add entry_name varchar(255),
    add otherAttsDigest varchar(255),
    drop foreign key FKD42DBF50206F5C8A,
    drop filesystem_fk;

update location set storage_group_id='DEFAULT', storage_id='fs1';

alter table location
    modify storage_group_id varchar(255) not null,
    modify storage_id varchar(255) not null;

drop table filesystem;

create table rel_instance_location (
    instance_fk bigint not null,
    location_fk bigint not null
) ENGINE=InnoDB;

alter table rel_instance_location 
    add constraint FK877EA1F9265C5DAA 
    foreign key (location_fk) 
    references location (pk);

alter table rel_instance_location 
    add constraint FK877EA1F937EDB1AA 
    foreign key (instance_fk) 
    references instance (pk);

create table archiving_task (
    pk bigint not null auto_increment,
    archiving_time datetime not null,
    series_iuid varchar(255) not null,
    source_stg_group_id varchar(255) not null,
    target_name varchar(255) not null,
    target_stg_group_id varchar(255) not null,
    delay_reason_code_fk bigint,
    primary key (pk)
) ENGINE=InnoDB;

alter table archiving_task
    add constraint FKD72560C52DC908EB
    foreign key (delay_reason_code_fk)
    references code (pk);
