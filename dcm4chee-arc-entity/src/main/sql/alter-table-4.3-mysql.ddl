    alter table instance
        add reject_code_fk bigint;

    alter table instance
        add constraint FK2116949540F8410A 
        foreign key (reject_code_fk) 
        references code (pk);

    create index inst_reject_code_fk_idx on instance (reject_code_fk);

    drop table rel_pps_sps;
    drop table rel_series_sps;
    drop table pps;
    drop table sps_station_aet;
    drop table sps;
    drop table req_proc;
    drop table request;
    drop table visit;

    create table series_req (
        pk bigint not null auto_increment,
        accession_no varchar(255) not null,
        accno_issuer varchar(255) not null,
        req_proc_id varchar(255) not null,
        req_physician varchar(255) not null,
        req_phys_fn_sx varchar(255) not null,
        req_phys_gn_sx varchar(255) not null,
        req_phys_i_name varchar(255) not null,
        req_phys_p_name varchar(255) not null,
        req_service varchar(255) not null,
        sps_id varchar(255) not null,
        study_iuid varchar(255) not null,
        series_fk bigint,
        primary key (pk)
    ) ENGINE=InnoDB;

    alter table series_req 
        add constraint FKE38CD2D68151AFEA 
        foreign key (series_fk) 
        references series (pk);

    create index series_req_physician_idx on series_req (req_physician);
    create index series_req_phys_i_name_idx on series_req (req_phys_i_name);
    create index series_req_phys_p_name_idx on series_req (req_phys_p_name);
    create index series_req_phys_gn_sx_idx on series_req (req_phys_gn_sx);
    create index series_req_phys_fn_sx_idx on series_req (req_phys_fn_sx);
    create index series_req_accession_no_idx on series_req (accession_no);
    create index series_req_accno_issuer_idx on series_req (accno_issuer);
    create index series_req_service_idx on series_req (req_service);
    create index series_req_proc_id_idx on series_req (req_proc_id);
    create index series_req_sps_id_idx on series_req (sps_id);
    create index series_req_study_iuid_idx on series_req (study_iuid);
    create index series_req_series_fk_idx on series_req (series_fk);

    create table mpps (
        pk bigint not null auto_increment,
        accession_no varchar(255),
        created_time datetime not null,
        mpps_attrs longblob not null,
        modality varchar(255) not null,
        station_aet varchar(255) not null,
        mpps_iuid varchar(255) not null unique,
        pps_start_date varchar(255) not null,
        pps_start_time varchar(255) not null,
        mpps_status integer not null,
        updated_time datetime not null,
        drcode_fk bigint,
        patient_fk bigint,
        primary key (pk)
    ) ENGINE=InnoDB;

    alter table mpps 
        add constraint FK333EE69DC28D5C 
        foreign key (drcode_fk) 
        references code (pk);

    alter table mpps 
        add constraint FK333EE6A511AE1E 
        foreign key (patient_fk) 
        references patient (pk);

    create index mpps_patient_fk_idx on mpps (patient_fk);
    create index mpps_iuid_idx on mpps (mpps_iuid);

    create table mwl_item (
        pk bigint not null auto_increment,
        accession_no varchar(255),
        created_time datetime not null,
        item_attrs longblob not null,
        modality varchar(255) not null,
        req_proc_id varchar(255) not null,
        perf_phys_fn_sx varchar(255) not null,
        perf_phys_gn_sx varchar(255) not null,
        perf_phys_i_name varchar(255) not null,
        perf_phys_name varchar(255) not null,
        perf_phys_p_name varchar(255) not null,
        sps_id varchar(255) not null,
        sps_start_date varchar(255) not null,
        sps_start_time varchar(255) not null,
        sps_status varchar(255) not null,
        study_iuid varchar(255) not null,
        updated_time datetime not null,
        patient_fk bigint,
        primary key (pk)
    ) ENGINE=InnoDB;

    alter table mwl_item 
        add constraint FK8F9D3D30A511AE1E 
        foreign key (patient_fk) 
        references patient (pk);

    create index mwl_item_sps_id_idx on mwl_item (sps_id);
    create index mwl_item_req_proc_id_idx on mwl_item (req_proc_id);
    create index mwl_item_study_iuid_idx on mwl_item (study_iuid);
    create index mwl_item_accession_no_idx on mwl_item (accession_no);
    create index mwl_item_sps_status_idx on mwl_item (sps_status);
    create index mwl_item_sps_start_date_idx on mwl_item (sps_start_date);
    create index mwl_item_sps_start_time_idx on mwl_item (sps_start_time);
    create index mwl_item_modality_idx on mwl_item (modality);
    create index mwl_item_perf_phys_name_idx on mwl_item (perf_phys_name);
    create index mwl_item_perf_phys_p_name_idx on mwl_item (perf_phys_p_name);
    create index mwl_item_perf_phys_i_name_idx on mwl_item (perf_phys_i_name);
    create index mwl_item_perf_phys_fn_sx_idx on mwl_item (perf_phys_fn_sx);
    create index mwl_item_perf_phys_gn_sx_idx on mwl_item (perf_phys_gn_sx);
    create index mwl_item_patient_fk_idx on mwl_item (patient_fk);

    create table sps_station_aet (
        pk bigint not null auto_increment,
        station_aet varchar(255) not null,
        mwl_item_fk bigint,
        primary key (pk)
    ) ENGINE=InnoDB;

    alter table sps_station_aet 
        add constraint FK786E2A3CF8FD7F43 
        foreign key (mwl_item_fk) 
        references mwl_item (pk);

    create index sps_station_aet_mwl_item_fk_idx on sps_station_aet (mwl_item_fk);
    create index sps_station_aet_station_aet_idx on sps_station_aet (station_aet);

    alter table patient
        add pat_id_issuer varchar(255);

    update patient join issuer on issuer.pk=patient.pat_id_issuer_fk
        set pat_id_issuer=entity_id;

    update patient join issuer on issuer.pk=patient.pat_id_issuer_fk
        set pat_id_issuer=concat(entity_id, '&', entity_uid, '&', entity_uid_type)
        where  entity_uid is not null;

    update patient
        set pat_id_issuer='*'
        where pat_id_issuer is null;

    alter table patient
        modify pat_id_issuer varchar(255) not null;

    alter table patient
        drop foreign key FKD0D3EB052D91ECA2;

    alter table patient
        drop pat_id_issuer_fk;

    create index pat_id_issuer_idx on patient (pat_id_issuer);

    alter table study
        add accno_issuer varchar(255);

    update study join issuer on issuer.pk=study.accno_issuer_fk
        set accno_issuer=entity_id;

    update study join issuer on issuer.pk=study.accno_issuer_fk
        set accno_issuer=concat(entity_id, '&', entity_uid, '&', entity_uid_type)
        where entity_uid is not null;

    update study
        set accno_issuer='*'
        where accno_issuer is null;

    alter table study
        modify accno_issuer varchar(255) not null;

    alter table study
        drop foreign key FK68B0DC9C45E7AAD;

    alter table study
        drop accno_issuer_fk;

    create index accno_issuer_idx on study (accno_issuer);
    
    drop table issuer;


