    create index code_idx on code4 (code_value, code_designator, code_version);

    create index content_item_instance_fk_idx on content_item4 (instance_fk);
    create index content_item_rel_type_idx on content_item4 (rel_type);
    create index content_item_name_fk_idx on content_item4 (name_fk);
    create index content_item_code_fk_idx on content_item4 (code_fk);
    create index content_item_text_value_idx on content_item4 (text_value);

    create index file_ref_instance_fk_idx on file_ref4 (instance_fk);
    create index file_ref_filesystem_fk_idx on file_ref4 (filesystem_fk);

    create index fs_next_fk_idx on filesystem4 (next_fk);
    create index fs_group_id_idx on filesystem4 (fs_group_id);
    create index fs_status_idx on filesystem4 (fs_status);

    create index inst_series_fk_idx on instance4 (series_fk);
    create index inst_sop_iuid_idx on instance4 (sop_iuid);
    create index inst_sop_cuid_idx on instance4 (sop_cuid);
    create index inst_no_idx on instance4 (inst_no);
    create index inst_content_date_idx on instance4 (content_date);
    create index inst_content_time_idx on instance4 (content_time);
    create index inst_sr_verified_idx on instance4 (sr_verified);
    create index inst_sr_complete_idx on instance4 (sr_complete);
    create index inst_availability on instance4 (availability);
    create index inst_srcode_fk_idx on instance4 (srcode_fk);
    create index inst_reject_code_fk_idx on instance4 (reject_code_fk);
    create index inst_custom1_idx on instance4 (inst_custom1);
    create index inst_custom2_idx on instance4 (inst_custom2);
    create index inst_custom3_idx on instance4 (inst_custom3);
    create index inst_replaced_idx on instance4 (replaced);

    create index pat_id_idx on patient4 (pat_id);
    create index pat_id_issuer_idx on patient4 (pat_id_issuer);
    create index pat_name_idx on patient4 (pat_name);
    create index pat_p_name_idx on patient4 (pat_p_name);
    create index pat_i_name_idx on patient4 (pat_i_name);
    create index pat_fn_sx_idx on patient4 (pat_fn_sx);
    create index pat_gn_sx_idx on patient4 (pat_gn_sx);
    create index pat_birthdate_idx on patient4 (pat_birthdate);
    create index pat_sex_idx on patient4 (pat_sex);
    create index pat_custom1_idx on patient4 (pat_custom1);
    create index pat_custom2_idx on patient4 (pat_custom2);
    create index pat_custom3_idx on patient4 (pat_custom3);
    create index pat_merge_fk_idx on patient4 (merge_fk);

    create index other_pid_pat_id_idx on other_pid4 (pat_id);
    create index other_pid_pat_id_issuer_idx on other_pid4 (pat_id_issuer);

    create index rel_pat_other_pid_other_pid_fk_idx on rel_pat_other_pid4 (other_pid_fk);
    create index rel_pat_other_pid_patient_fk_idx on rel_pat_other_pid4 (patient_fk);

    create index rel_study_pcode_study_fk_idx on rel_study_pcode4 (study_fk);
    create index rel_study_pcode_pcode_fk_idx on rel_study_pcode4 (pcode_fk);

    create index series_study_fk_idx on series4 (study_fk);
    create unique index series_iuid_idx on series4 (series_iuid);
    create index series_no_idx on series4 (series_no);
    create index series_modality_idx on series4 (modality);
    create index series_station_name_idx on series4 (station_name);
    create index series_pps_start_date_idx on series4 (pps_start_date);
    create index series_pps_start_time_idx on series4 (pps_start_time);
    create index series_perf_phys_name_idx on series4 (perf_phys_name);
    create index series_perf_phys_p_name_idx on series4 (perf_phys_p_name);
    create index series_perf_phys_i_name_idx on series4 (perf_phys_i_name);
    create index series_perf_phys_fn_sx_idx on series4 (perf_phys_fn_sx);
    create index series_perf_phys_gn_sx_idx on series4 (perf_phys_gn_sx);
    create index series_body_part_idx on series4 (body_part);
    create index series_laterality_idx on series4 (laterality);
    create index series_desc_idx on series4 (series_desc);
    create index series_institution_idx on series4 (institution);
    create index series_inst_code_fk_idx on series4 (inst_code_fk);
    create index series_department_idx on series4 (department);
    create index series_custom1_idx on series4 (series_custom1);
    create index series_custom2_idx on series4 (series_custom2);
    create index series_custom3_idx on series4 (series_custom3);

    create index series_req_physician_idx on series_req4 (req_physician);
    create index series_req_phys_i_name_idx on series_req4 (req_phys_i_name);
    create index series_req_phys_p_name_idx on series_req4 (req_phys_p_name);
    create index series_req_phys_gn_sx_idx on series_req4 (req_phys_gn_sx);
    create index series_req_phys_fn_sx_idx on series_req4 (req_phys_fn_sx);
    create index series_req_accession_no_idx on series_req4 (accession_no);
    create index series_req_accno_issuer_idx on series_req4 (accno_issuer);
    create index series_req_service_idx on series_req4 (req_service);
    create index series_req_proc_id_idx on series_req4 (req_proc_id);
    create index series_req_sps_id_idx on series_req4 (sps_id);
    create index series_req_study_iuid_idx on series_req4 (study_iuid);
    create index series_req_series_fk_idx on series_req4 (series_fk);

    create index study_patient_fk_idx on study4 (patient_fk);
    create unique index study_iuid_idx on study4 (study_iuid);
    create index study_id_idx on study4 (study_id);
    create index study_date_idx on study4 (study_date);
    create index study_time_idx on study4 (study_time);
    create index study_accession_no_idx on study4 (accession_no);
    create index study_accno_issuer_idx on study4 (accno_issuer);
    create index study_desc_idx on study4 (study_desc);
    create index study_ref_physician_idx on study4 (ref_physician);
    create index study_ref_phys_p_name_idx on study4 (ref_phys_p_name);
    create index study_ref_phys_i_name_idx on study4 (ref_phys_i_name);
    create index study_ref_phys_fn_sx_idx on study4 (ref_phys_fn_sx);
    create index study_ref_phys_gn_sx_idx on study4 (ref_phys_gn_sx);
    create index study_custom1_idx on study4 (study_custom1);
    create index study_custom2_idx on study4 (study_custom2);
    create index study_custom3_idx on study4 (study_custom3);
    create index study_access_control_id_idx on study4 (access_control_id);

    create index vo_instance_fk_idx on verify_observer4 (instance_fk);
    create index vo_verify_datetime_idx on verify_observer4 (verify_datetime);
    create index vo_observer_name_idx on verify_observer4 (observer_name);
    create index vo_observer_p_name_idx on verify_observer4 (observer_p_name);
    create index vo_observer_i_name_idx on verify_observer4 (observer_i_name);
    create index vo_observer_fn_sx_idx on verify_observer4 (observer_fn_sx);
    create index vo_observer_gn_sx_idx on verify_observer4 (observer_gn_sx);

    create index mpps_patient_fk_idx on mpps4 (patient_fk);
    create index mpps_iuid_idx on mpps4 (mpps_iuid);

    create index mwl_item_sps_id_idx on mwl_item4 (sps_id);
    create index mwl_item_req_proc_id_idx on mwl_item4 (req_proc_id);
    create index mwl_item_study_iuid_idx on mwl_item4 (study_iuid);
    create index mwl_item_accession_no_idx on mwl_item4 (accession_no);
    create index mwl_item_sps_status_idx on mwl_item4 (sps_status);
    create index mwl_item_sps_start_date_idx on mwl_item4 (sps_start_date);
    create index mwl_item_sps_start_time_idx on mwl_item4 (sps_start_time);
    create index mwl_item_modality_idx on mwl_item4 (modality);
    create index mwl_item_perf_phys_name_idx on mwl_item4 (perf_phys_name);
    create index mwl_item_perf_phys_p_name_idx on mwl_item4 (perf_phys_p_name);
    create index mwl_item_perf_phys_i_name_idx on mwl_item4 (perf_phys_i_name);
    create index mwl_item_perf_phys_fn_sx_idx on mwl_item4 (perf_phys_fn_sx);
    create index mwl_item_perf_phys_gn_sx_idx on mwl_item4 (perf_phys_gn_sx);
    create index mwl_item_patient_fk_idx on mwl_item4 (patient_fk);

    create index sps_station_aet_mwl_item_fk_idx on sps_station_aet4 (mwl_item_fk);
    create index sps_station_aet_station_aet_idx on sps_station_aet4 (station_aet);
