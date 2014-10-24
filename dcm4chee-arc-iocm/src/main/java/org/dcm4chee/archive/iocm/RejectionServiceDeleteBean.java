package org.dcm4chee.archive.iocm;

import java.sql.Timestamp;
import java.util.Collection;

import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Instance;

public interface RejectionServiceDeleteBean {

    public abstract void deleteRejected(Object source,
            Collection<Instance> instances);

    public abstract Collection<Instance> findRejectedObjects(
            Code rejectionNote, Timestamp deadline, int maxDeletes);

}