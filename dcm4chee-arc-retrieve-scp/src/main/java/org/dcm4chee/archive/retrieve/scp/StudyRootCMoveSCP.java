package org.dcm4chee.archive.retrieve.scp;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Typed;

import org.dcm4che3.data.UID;
import org.dcm4che3.net.service.DicomService;

@ApplicationScoped
@Typed(DicomService.class)
public class StudyRootCMoveSCP extends CMoveSCP {
    public StudyRootCMoveSCP() {
        super(UID.StudyRootQueryRetrieveInformationModelMOVE,
                "STUDY", "SERIES", "IMAGE");
    }
}