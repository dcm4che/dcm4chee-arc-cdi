/* start QC ACTION History */
ALTER TABLE action RENAME action_history;

ALTER TABLE action_history
DROP TRANSACTION_ID;

ALTER TABLE action_history CHANGE ACTION action VARCHAR(255) NOT NULL;

ALTER TABLE action_history CHANGE DB_CREATED_TIME created_time datetime NOT NULL;

 LOCK TABLES study_history WRITE,
 action_history WRITE;

ALTER TABLE study_history
DROP
FOREIGN KEY STUDY_HIST_ACTION_FK;

ALTER TABLE study_history
DROP INDEX STUDY_HIST_ACTION_FK;

ALTER TABLE action_history CHANGE PK pk bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE study_history CHANGE ACTION_FK action_history_fk bigint(20);

ALTER TABLE study_history ADD CONSTRAINT FK154398B16C9B9292
FOREIGN KEY (action_history_fk) REFERENCES action_history (pk);

 UNLOCK TABLES;
 
/* end QC ACTION History */
 
 /* start QC STUDY History */
ALTER TABLE study_history CHANGE OLD_STUDY_IUID old_study_uid varchar(255) NOT NULL;

ALTER TABLE study_history CHANGE NEW_STUDY_IUID next_study_uid varchar(255) NULL DEFAULT NULL;

 LOCK TABLES series_history WRITE,
 study_history WRITE;

ALTER TABLE series_history
DROP
FOREIGN KEY SERIES_HIST_STUDY_HIST_FK;

ALTER TABLE series_history
DROP INDEX SERIES_HIST_STUDY_HIST_FK;

ALTER TABLE study_history CHANGE PK pk bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE series_history CHANGE OLD_STUDY_HIST_FK study_history_fk bigint(20);

ALTER TABLE series_history ADD CONSTRAINT FKFD935C99C2EE0096
FOREIGN KEY (study_history_fk) REFERENCES study_history (pk);

UNLOCK TABLES;

 /* start attributes  procedure */
ALTER TABLE dicomattrs ADD study_history_fk bigint(20);

ALTER TABLE study_history ADD dicomattrs_fk bigint(20);

ALTER TABLE study_history ADD CONSTRAINT FK154398B185AF69D8
FOREIGN KEY (dicomattrs_fk) REFERENCES dicomattrs (pk);

INSERT INTO dicomattrs (attrs, study_history_fk)
SELECT STUDY_ATTRS,
       pk
FROM study_history;

UPDATE study_history,
       dicomattrs
SET dicomattrs_fk = dicomattrs.pk
WHERE study_history.pk = dicomattrs.study_history_fk;

ALTER TABLE dicomattrs
DROP study_history_fk;

ALTER TABLE study_history
DROP STUDY_ATTRS;

 /* end attributes  procedure */ 
 
 /* end QC STUDY History */
 
 /* start QC SERIES History */
ALTER TABLE series_history CHANGE OLD_SERIES_IUID old_series_iuid varchar(255);

ALTER TABLE series_history
DROP NEW_SERIES_IUID;

 LOCK TABLES instance_history WRITE,
 series_history WRITE;

ALTER TABLE instance_history
DROP
FOREIGN KEY INST_HIST_SERIES_HIST_FK;

ALTER TABLE instance_history
DROP INDEX INST_HIST_SERIES_HIST_FK;

ALTER TABLE series_history CHANGE PK pk bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE instance_history CHANGE OLD_SERIES_HIST_FK series_history_fk bigint(20);

ALTER TABLE instance_history ADD CONSTRAINT FK68FB577E4DDF450
FOREIGN KEY (series_history_fk) REFERENCES series_history (pk);

 UNLOCK TABLES;

 /* start attributes  procedure */
ALTER TABLE dicomattrs ADD series_history_fk bigint(20);

ALTER TABLE series_history ADD dicomattrs_fk bigint(20);

ALTER TABLE series_history ADD CONSTRAINT FKFD935C9985AF69D8
FOREIGN KEY (dicomattrs_fk) REFERENCES dicomattrs (pk);

INSERT INTO dicomattrs (attrs, series_history_fk)
SELECT BIN(pk),
       pk
FROM series_history;

UPDATE series_history,
       dicomattrs
SET dicomattrs_fk = dicomattrs.pk
WHERE series_history.pk = dicomattrs.series_history_fk;

ALTER TABLE dicomattrs
DROP series_history_fk;

 /* end attributes  procedure */ 
 
 /* end QC SERIES History */
  
 /* start QC Instance History */
ALTER TABLE instance_history CHANGE CREATED_FOR_CLONE cloned bit(1) NOT NULL DEFAULT 0;

ALTER TABLE instance_history CHANGE CURRENT_IUID current_uid varchar(255) NOT NULL;

ALTER TABLE instance_history CHANGE NEXT_IUID next_uid varchar(255) NOT NULL;

ALTER TABLE instance_history CHANGE OBSOLETE_IUID old_uid varchar(255) NOT NULL;

ALTER TABLE instance_history CHANGE PK pk bigint(20) NOT NULL AUTO_INCREMENT;

 /* start attributes  procedure */
ALTER TABLE dicomattrs ADD instance_history_fk bigint(20);

ALTER TABLE instance_history ADD dicomattrs_fk bigint(20);

ALTER TABLE instance_history ADD CONSTRAINT FK68FB57785AF69D8
FOREIGN KEY (dicomattrs_fk) REFERENCES dicomattrs (pk);

INSERT INTO dicomattrs (attrs, instance_history_fk)
SELECT BIN(pk),
       pk
FROM instance_history;

UPDATE instance_history,
       dicomattrs
SET dicomattrs_fk = dicomattrs.pk
WHERE instance_history.pk = dicomattrs.instance_history_fk;

ALTER TABLE dicomattrs
DROP instance_history_fk;

 /* end attributes  procedure */ 
 /* end QC Instance History */
 
 /* Add current_study_uid and current_series_uid*/
ALTER TABLE instance_history ADD current_study_uid varchar(255) NOT NULL;

ALTER TABLE instance_history ADD current_series_uid varchar(255) NOT NULL;

/*INSERT INTO instance_history (current_study_uid, current_series_uid)
SELECT st.study_iuid,sr.series_iuid FROM instance inst
join series sr join study st
left outer join instance_history qci on qci.old_uid=inst.sop_iuid;*/
CREATE INDEX instance_history_old_uid_idx ON instance_history (old_uid);
CREATE INDEX instance_history_next_uid_idx ON instance_history (next_uid);
CREATE INDEX instance_history_current_uid_idx ON instance_history (current_uid);
CREATE INDEX action_history_created_time_idx ON action_history (created_time);