package org.dcm4chee.archive.util;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.Deidentifier;

/**
 * Created by Umberto Cappellini on 11/27/15.
 */
public enum ArchiveDeidentifier implements Deidentifier {
    DEFAULT;

    public Object deidentify(int tag, VR vr, Object value) {
        if (tag == Tag.PatientBirthDate ||
                tag == Tag.PatientSex ||
                tag == Tag.PatientAge ||
                tag == Tag.PatientAddress ||
                vr.equals(VR.PN))
            return "XXX";
        else
            return value;
    }
}
