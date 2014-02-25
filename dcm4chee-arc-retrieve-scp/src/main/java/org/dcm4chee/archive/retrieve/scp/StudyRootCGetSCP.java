package org.dcm4chee.archive.retrieve.scp;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Typed;

import org.dcm4che3.data.UID;
import org.dcm4che3.net.service.DicomService;

@ApplicationScoped
@Typed(DicomService.class)
public class StudyRootCGetSCP extends CGetSCP {
    public StudyRootCGetSCP() {
        super(UID.StudyRootQueryRetrieveInformationModelGET,
                "STUDY", "SERIES", "IMAGE");
    }
}