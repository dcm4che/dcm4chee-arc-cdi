/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2012
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.archive.mima.impl;

import java.util.Arrays;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Device;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Supplements {

    private static Logger LOG = LoggerFactory.getLogger(Supplements.class);
    private static ElementDictionary DICT = 
            ElementDictionary.getStandardElementDictionary();

    public static void supplementComposite(Object prompt, Attributes ds, Device device) {
        supplementValue(prompt, ds, Tag.Manufacturer, VR.LO, device.getManufacturer());
        supplementValue(prompt, ds, Tag.ManufacturerModelName, VR.LO,
                device.getManufacturerModelName());
        supplementValue(prompt, ds, Tag.StationName, VR.SH, device.getStationName());
        supplementValue(prompt, ds, Tag.DeviceSerialNumber, VR.LO,
                device.getDeviceSerialNumber());
        supplementValues(prompt, ds, Tag.SoftwareVersions, VR.LO,
                device.getSoftwareVersions());
        supplementValue(prompt, ds, Tag.InstitutionName, VR.LO,
                device.getInstitutionNames());
        supplementCode(prompt, ds, Tag.InstitutionCodeSequence,
                device.getInstitutionCodes());
        supplementValue(prompt, ds, Tag.InstitutionalDepartmentName, VR.LO,
                device.getInstitutionalDepartmentNames());
        supplementIssuers(prompt, ds, device);
        supplementRequestIssuers(prompt, ds, device);
        supplementRequestIssuers(prompt, ds.getSequence(Tag.RequestAttributesSequence),
                device);
    }

    private static void supplementIssuers(Object prompt, Attributes ds, Device device) {
        if (ds.containsValue(Tag.PatientID))
            supplementIssuerOfPatientID(prompt, ds, device.getIssuerOfPatientID());
        if (ds.containsValue(Tag.AdmissionID))
            supplementIssuer(prompt, ds, Tag.IssuerOfAdmissionIDSequence,
                    device.getIssuerOfAdmissionID());
        if (ds.containsValue(Tag.ServiceEpisodeID))
            supplementIssuer(prompt, ds, Tag.IssuerOfServiceEpisodeID,
                    device.getIssuerOfServiceEpisodeID());
        if (ds.containsValue(Tag.ContainerIdentifier))
            supplementIssuer(prompt, ds, Tag.IssuerOfTheContainerIdentifierSequence,
                    device.getIssuerOfContainerIdentifier());
        if (ds.containsValue(Tag.SpecimenIdentifier))
            supplementIssuer(prompt, ds, Tag.IssuerOfTheSpecimenIdentifierSequence,
                    device.getIssuerOfSpecimenIdentifier());
    }

    private static void supplementRequestIssuers(Object prompt, Sequence rqSeq, Device device) {
        if (rqSeq != null)
            for (Attributes rq : rqSeq)
                supplementRequestIssuers(prompt, rq, device);
    }

    private static void supplementRequestIssuers(Object prompt, Attributes rq, Device device) {
        if (rq.containsValue(Tag.AccessionNumber))
            supplementIssuer(prompt, rq, Tag.IssuerOfAccessionNumberSequence,
                    device.getIssuerOfAccessionNumber());
        if (rq.containsValue(Tag.PlacerOrderNumberImagingServiceRequest))
            supplementIssuer(prompt, rq, Tag.OrderPlacerIdentifierSequence,
                    device.getOrderPlacerIdentifier());
        if (rq.containsValue(Tag.FillerOrderNumberImagingServiceRequest))
            supplementIssuer(prompt, rq, Tag.OrderFillerIdentifierSequence,
                    device.getOrderFillerIdentifier());
    }

    public static void supplementMPPS(Object prompt, Attributes mpps, Device device) {
        supplementIssuers(prompt, mpps, device);
        supplementRequestIssuers(prompt,
                mpps.getSequence(Tag.ScheduledStepAttributesSequence),
                device);
    }

    public static boolean supplementIssuerOfPatientID(Object prompt, Attributes ds, Issuer issuer) {
        if (issuer == null
                || ds.containsValue(Tag.IssuerOfPatientID) 
                || ds.containsValue(Tag.IssuerOfPatientIDQualifiersSequence))
            return false;
        
        String localNamespaceEntityID = issuer.getLocalNamespaceEntityID();
        if (localNamespaceEntityID != null) {
            ds.setString(Tag.IssuerOfPatientID, VR.LO, localNamespaceEntityID);
            log(prompt, Tag.IssuerOfPatientID, VR.LO, localNamespaceEntityID);
        }
        String universalEntityID = issuer.getUniversalEntityID();
        if (universalEntityID != null) {
            Attributes item = new Attributes(ds.bigEndian(), 2);
            item.setString(Tag.UniversalEntityID, VR.UT, universalEntityID);
            item.setString(Tag.UniversalEntityIDType, VR.CS,
                    issuer.getUniversalEntityIDType());
            ds.newSequence(Tag.IssuerOfPatientIDQualifiersSequence, 1).add(item);
            log(prompt, Tag.IssuerOfPatientIDQualifiersSequence, item);
        }
        return true;
    }

    private static boolean supplementValue(Object prompt, Attributes ds,
            int tag, VR vr, String... values) {
        if (values.length == 0 || values[0] == null
                || ds.containsValue(tag))
            return false;

        ds.setString(tag, vr, values[0]);
        log(prompt, tag, vr, values[0]);
        return true;
    }

    private static boolean supplementValues(Object prompt, Attributes ds,
            int tag, VR vr, String... values) {
        if (values.length == 0
                || ds.containsValue(tag))
            return false;

        ds.setString(tag, vr, values);
        log(prompt, tag, vr, values);
        return true;
    }

    public static boolean supplementIssuer(Object prompt, Attributes ds,
            int seqTag, Issuer issuer) {
        if (issuer == null || ds.containsValue(seqTag))
            return false;

        Attributes item = new Attributes(ds.bigEndian(), 3);
        String localNamespaceEntityID = issuer.getLocalNamespaceEntityID();
        if (localNamespaceEntityID != null)
            item.setString(Tag.LocalNamespaceEntityID, VR.LO, localNamespaceEntityID);
        String universalEntityID = issuer.getUniversalEntityID();
        if (universalEntityID != null) {
            item.setString(Tag.UniversalEntityID, VR.UT, universalEntityID);
            item.setString(Tag.UniversalEntityIDType, VR.CS,
                    issuer.getUniversalEntityIDType());
        }
        ds.newSequence(seqTag, 1).add(item);
        log(prompt, seqTag, item);
        return true;
    }

    public static boolean supplementCode(Object prompt, Attributes ds,
            int seqTag, Code... codes) {
        if (codes.length == 0 || codes[0] == null || ds.containsValue(seqTag))
            return false;

        Attributes item = new Attributes(ds.bigEndian(), 4);
        item.setString(Tag.CodeValue, VR.SH, codes[0].getCodeValue());
        item.setString(Tag.CodingSchemeDesignator, VR.SH,
                codes[0].getCodingSchemeDesignator());
        String version = codes[0].getCodingSchemeVersion();
        if (version != null)
            item.setString(Tag.CodingSchemeVersion, VR.SH, version);
        item.setString(Tag.CodeMeaning, VR.LO, codes[0].getCodeMeaning());
        ds.newSequence(seqTag, 1).add(item);
        log(prompt, seqTag, item);
        return true;
    }

    private static void log(Object prompt, int tag, VR vr, String value) {
        if (LOG.isDebugEnabled())
            LOG.debug("{}: Supplements {} {} [{}] {}",
                    prompt, TagUtils.toString(tag), vr, value, 
                    DICT.keywordOf(tag));
    }

    private static void log(Object prompt, int tag, VR vr, String[] values) {
        if (LOG.isDebugEnabled())
            LOG.debug("{}: Supplements {} {} {} {}",
                    prompt, TagUtils.toString(tag), vr, Arrays.toString(values),
                    DICT.keywordOf(tag));
    }

    private static void log(Object prompt, int tag, Attributes item) {
        if (LOG.isDebugEnabled())
            LOG.debug("{}: Supplements {} SQ {} with Item [\n{}]",
                    prompt, TagUtils.toString(tag), DICT.keywordOf(tag), item);
    }

}
