package org.dcm4chee.archive.iocm;

public enum RejectionType {
    IncorrectWorklistEntrySelected,
    IncorrectModalityWorklistEntry,
    RejectedForPatientSafetyReasons,
    RejectedForQualityReasons,
    DataRetentionPeriodExpired
}