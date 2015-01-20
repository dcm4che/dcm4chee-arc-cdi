/* start QC ACTION History */
ALTER TABLE qc_action RENAME qc_action_history;

ALTER TABLE qc_action_history
DROP TRANSACTION_ID;

ALTER TABLE qc_action_history CHANGE ACTION action VARCHAR(255) NOT NULL;

ALTER TABLE qc_action_history CHANGE DB_CREATED_TIME created_time datetime NOT NULL;

 LOCK TABLES qc_study_history WRITE,
 qc_action_history WRITE;

ALTER TABLE qc_study_history
DROP
FOREIGN KEY QC_STUDY_HIST_ACTION_FK;

ALTER TABLE qc_study_history
DROP INDEX QC_STUDY_HIST_ACTION_FK;

ALTER TABLE qc_action_history CHANGE PK pk bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE qc_study_history CHANGE QC_ACTION_FK qc_action_history_fk bigint(20);

ALTER TABLE qc_study_history ADD CONSTRAINT FK154398B16C9B9292
FOREIGN KEY (qc_action_history_fk) REFERENCES qc_action_history (pk);

 UNLOCK TABLES;
 
/* end QC ACTION History */
 
 /* start QC STUDY History */
ALTER TABLE qc_study_history CHANGE OLD_STUDY_IUID old_study_uid varchar(255) NOT NULL;

ALTER TABLE qc_study_history CHANGE NEW_STUDY_IUID next_study_uid varchar(255) NULL DEFAULT NULL;

 LOCK TABLES qc_series_history WRITE,
 qc_study_history WRITE;

ALTER TABLE qc_series_history
DROP
FOREIGN KEY QC_SERIES_HIST_STUDY_HIST_FK;

ALTER TABLE qc_series_history
DROP INDEX QC_SERIES_HIST_STUDY_HIST_FK;

ALTER TABLE qc_study_history CHANGE PK pk bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE qc_series_history CHANGE OLD_STUDY_HIST_FK qc_study_history_fk bigint(20);

ALTER TABLE qc_series_history ADD CONSTRAINT FKFD935C99C2EE0096
FOREIGN KEY (qc_study_history_fk) REFERENCES qc_study_history (pk);

UNLOCK TABLES;

 /* start attributes  procedure */
ALTER TABLE dicomattrs ADD qc_study_history_fk bigint(20);

ALTER TABLE qc_study_history ADD dicomattrs_fk bigint(20);

ALTER TABLE qc_study_history ADD CONSTRAINT FK154398B185AF69D8
FOREIGN KEY (dicomattrs_fk) REFERENCES dicomattrs (pk);

INSERT INTO dicomattrs (attrs, qc_study_history_fk)
SELECT STUDY_ATTRS,
       pk
FROM qc_study_history;

UPDATE qc_study_history,
       dicomattrs
SET dicomattrs_fk = dicomattrs.pk
WHERE qc_study_history.pk = dicomattrs.qc_study_history_fk;

ALTER TABLE dicomattrs
DROP qc_study_history_fk;

ALTER TABLE qc_study_history
DROP STUDY_ATTRS;

 /* end attributes  procedure */ 
 
 /* end QC STUDY History */
 
 /* start QC SERIES History */
ALTER TABLE qc_series_history CHANGE OLD_SERIES_IUID old_series_iuid varchar(255);

ALTER TABLE qc_series_history
DROP NEW_SERIES_IUID;

 LOCK TABLES qc_instance_history WRITE,
 qc_series_history WRITE;

ALTER TABLE qc_instance_history
DROP
FOREIGN KEY QC_INST_HIST_SERIES_HIST_FK;

ALTER TABLE qc_instance_history
DROP INDEX QC_INST_HIST_SERIES_HIST_FK;

ALTER TABLE qc_series_history CHANGE PK pk bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE qc_instance_history CHANGE OLD_SERIES_HIST_FK qc_series_history_fk bigint(20);

ALTER TABLE qc_instance_history ADD CONSTRAINT FK68FB577E4DDF450
FOREIGN KEY (qc_series_history_fk) REFERENCES qc_series_history (pk);

 UNLOCK TABLES;

 /* start attributes  procedure */
ALTER TABLE dicomattrs ADD qc_series_history_fk bigint(20);

ALTER TABLE qc_series_history ADD dicomattrs_fk bigint(20);

ALTER TABLE qc_series_history ADD CONSTRAINT FKFD935C9985AF69D8
FOREIGN KEY (dicomattrs_fk) REFERENCES dicomattrs (pk);

INSERT INTO dicomattrs (attrs, qc_series_history_fk)
SELECT BIN(pk),
       pk
FROM qc_series_history;

UPDATE qc_series_history,
       dicomattrs
SET dicomattrs_fk = dicomattrs.pk
WHERE qc_series_history.pk = dicomattrs.qc_series_history_fk;

ALTER TABLE dicomattrs
DROP qc_series_history_fk;

 /* end attributes  procedure */ 
 
 /* end QC SERIES History */
  
 /* start QC Instance History */
ALTER TABLE qc_instance_history CHANGE CREATED_FOR_CLONE cloned bit(1) NOT NULL DEFAULT 0;

ALTER TABLE qc_instance_history CHANGE CURRENT_IUID current_uid varchar(255) NOT NULL;

ALTER TABLE qc_instance_history CHANGE NEXT_IUID next_uid varchar(255) NOT NULL;

ALTER TABLE qc_instance_history CHANGE OBSOLETE_IUID old_uid varchar(255) NOT NULL;

ALTER TABLE qc_instance_history CHANGE PK pk bigint(20) NOT NULL AUTO_INCREMENT;

 /* start attributes  procedure */
ALTER TABLE dicomattrs ADD qc_instance_history_fk bigint(20);

ALTER TABLE qc_instance_history ADD dicomattrs_fk bigint(20);

ALTER TABLE qc_instance_history ADD CONSTRAINT FK68FB57785AF69D8
FOREIGN KEY (dicomattrs_fk) REFERENCES dicomattrs (pk);

INSERT INTO dicomattrs (attrs, qc_instance_history_fk)
SELECT BIN(pk),
       pk
FROM qc_instance_history;

UPDATE qc_instance_history,
       dicomattrs
SET dicomattrs_fk = dicomattrs.pk
WHERE qc_instance_history.pk = dicomattrs.qc_instance_history_fk;

ALTER TABLE dicomattrs
DROP qc_instance_history_fk;

 /* end attributes  procedure */ 
 /* end QC Instance History */
 
 /* Add current_study_uid and current_series_uid*/
ALTER TABLE qc_instance_history ADD current_study_uid varchar(255) NOT NULL;

ALTER TABLE qc_instance_history ADD current_series_uid varchar(255) NOT NULL;

/*INSERT INTO qc_instance_history (current_study_uid, current_series_uid)
SELECT st.study_iuid,sr.series_iuid FROM instance inst
join series sr join study st
left outer join qc_instance_history qci on qci.old_uid=inst.sop_iuid;*/
CREATE INDEX qc_instance_history_old_uid_idx ON qc_instance_history (old_uid);
CREATE INDEX qc_instance_history_next_uid_idx ON qc_instance_history (next_uid);
CREATE INDEX qc_instance_history_current_uid_idx ON qc_instance_history (current_uid);
CREATE INDEX qc_action_history_created_time_idx ON qc_action_history (created_time);