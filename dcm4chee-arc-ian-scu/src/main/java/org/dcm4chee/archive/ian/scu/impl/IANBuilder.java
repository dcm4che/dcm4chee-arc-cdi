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
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
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

package org.dcm4chee.archive.ian.scu.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4chee.archive.entity.Availability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class IANBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(IANBuilder.class);

    private final Attributes ian;
    private final Sequence ianRefSeriesSeq;
    private final Sequence ianRefPPSSeq;
    private final HashSet<String> ianRefInstanceUIDs = new HashSet<String>();
    private String ppsiuid;
    private String studyiuid;
    private HashMap<String,Attributes> ppsRefSOPs;


    public IANBuilder() {
        this.ian = new Attributes(3);
        this.ianRefPPSSeq = ian.newSequence(Tag.ReferencedPerformedProcedureStepSequence, 1);
        this.ianRefSeriesSeq = 
                ian.newSequence(Tag.ReferencedSeriesSequence, 1);
    }

    public String getMPPSInstanceUID() {
        return ppsiuid;
    }

    public void setReferencedMPPS(String ppsiuid, Attributes attrs) {
        if (!ianRefInstanceUIDs.isEmpty())
            throw new IllegalStateException("SOP Reference already added");

        if (ppsiuid == null)
            throw new NullPointerException("ppsiuid");

        Attributes ssa = attrs.getNestedDataset(
                Tag.ScheduledStepAttributesSequence);

        if (ssa == null)
            throw new IllegalArgumentException("Missing Scheduled Step Attributes");

        String studyiuid = ssa.getString(Tag.StudyInstanceUID);
        if (studyiuid == null)
            throw new IllegalArgumentException(
                    "Missing Study Instance UID");

        Sequence perfSeriesSeq = attrs.getSequence(Tag.PerformedSeriesSequence);
        if (perfSeriesSeq == null)
            throw new IllegalArgumentException(
                    "Missing Performed Series Sequence");

        HashMap<String, Attributes> map = new HashMap<String,Attributes>();
        for (Attributes series : perfSeriesSeq) {
            if (!series.containsValue(Tag.SeriesInstanceUID))
                throw new IllegalArgumentException(
                        "Missing Series Instance UID");
            addRefSOPSeq(ppsiuid, 
                    series.getSequence(Tag.ReferencedImageSequence), map);
            addRefSOPSeq(ppsiuid,
                    series.getSequence(Tag.ReferencedNonImageCompositeSOPInstanceSequence), map);
        }
        this.ppsRefSOPs = map;
        this.ppsiuid =  ppsiuid;
        this.studyiuid = studyiuid;
        ian.setString(Tag.StudyInstanceUID, VR.UI, studyiuid);
        Attributes refPPS = new Attributes(3);
        refPPS.setString(Tag.ReferencedSOPClassUID, VR.UI,
                UID.ModalityPerformedProcedureStepSOPClass);
        refPPS.setString(Tag.ReferencedSOPInstanceUID, VR.UI, ppsiuid);
        refPPS.setNull(Tag.PerformedWorkitemCodeSequence, VR.SQ);
        ianRefPPSSeq.add(refPPS);
    }

    private void addRefSOPSeq(String ppsiuid, Sequence refSOPSeq, 
            HashMap<String,Attributes> map) {
        if (refSOPSeq != null)
            for (Attributes refSOP : refSOPSeq) {
                 String iuid = refSOP.getString(Tag.ReferencedSOPInstanceUID);
                 if (iuid == null)
                     throw new IllegalArgumentException("Missing Referenced SOP Instance UID");
                 String cuid = refSOP.getString(Tag.ReferencedSOPClassUID);
                 if (cuid == null)
                     throw new IllegalArgumentException("Missing Referenced SOP Class UID");
                 Attributes prev = map.put(iuid, refSOP);
                 if (prev != null) {
                     LOG.warn(
                             "MPPS[iuid={}] contains multiple references of Instance[iuid={}, cuid={}]",
                             ppsiuid, iuid, cuid);
                 }
            };
    }

    public int numberOfOutstandingInstances() {
        return ppsRefSOPs != null ? ppsRefSOPs.size() : 0;
    }

    public Attributes getIAN() {
        return ian;
    }

    public boolean addReferencedInstance(String studyiuid, String seriesiuid, 
            String iuid, String cuid, Availability availability,
            String... retrieveAETs) {
        if (this.studyiuid == null) {
            this.studyiuid = studyiuid;
            ian.setString(Tag.StudyInstanceUID, VR.UI, studyiuid);
        } else if (!this.studyiuid.equals(studyiuid)) {
                throw new IllegalStateException(
                    "Study[iuid=" + studyiuid 
                    + "] of received Instance[iuid=" + iuid + ", cuid=" + cuid
                    + "] of Series[iuid=" + seriesiuid
                    + "] does not match Study[iuid=" + this.studyiuid
                    + (ppsiuid != null
                         ? ("] referenced by MPPS[iuid=" + ppsiuid + "]")
                         : "] of previous added referenced Instance"));
        }
        if (!ianRefInstanceUIDs.add(iuid)) {
            return false;
        }
        if (ppsRefSOPs != null) {
            Attributes refSOP = ppsRefSOPs.remove(iuid);
            if (refSOP == null)
                return false;

            String seriesiuidInPPS = refSOP.getParent().getString(Tag.SeriesInstanceUID);
            String cuidInPPS = refSOP.getString(Tag.ReferencedSOPClassUID);
            if (!seriesiuid.equals(seriesiuidInPPS)) {
                LOG.warn("Series of received Instance[iuid={}, cuid={}] "
                        + "of Series[iuid={}] of Study[iuid={}] differs from"
                        + "Series[iuid={}] referenced by MPPS[iuid={}] - "
                        + "no IAN will be emitted",
                        iuid, cuid, seriesiuid, studyiuid, seriesiuidInPPS, ppsiuid);
                ppsRefSOPs.put(iuid, refSOP);
                return false;
            }
            if (!cuid.equals(cuidInPPS)) {
                LOG.warn("SOP Class of received Instance[iuid={}, cuid={}] "
                        + "of Series[iuid={}] of Study[iuid={}] differs from"
                        + "SOP Class[cuid={}] referenced by MPPS[iuid={}] - "
                        + "emitted IAN will reference SOP Class of received Instance",
                        iuid, cuid, seriesiuid, studyiuid, cuidInPPS, ppsiuid);
            }
        }
        getIANRefSeries(seriesiuid).getSequence(Tag.ReferencedSOPSequence)
                .add(mkRefSOP(iuid, cuid, availability, retrieveAETs));
        return true;
    }

    private Attributes getIANRefSeries(String seriesiuid) {
        for (ListIterator<Attributes> iter = 
                ianRefSeriesSeq.listIterator(ianRefSeriesSeq.size());
                        iter.hasPrevious();) {
            Attributes refSeries = iter.previous();
            if (refSeries.getString(Tag.SeriesInstanceUID).equals(seriesiuid)) {
                return refSeries;
            }
        }
        Attributes refSeries = new Attributes(2);
        refSeries.newSequence(Tag.ReferencedSOPSequence, 10);
        refSeries.setString(Tag.SeriesInstanceUID, VR.UI, seriesiuid);
        ianRefSeriesSeq.add(refSeries);
        return refSeries;
    }

    private Attributes mkRefSOP(String iuid, String cuid,
            Availability availability, String... retrieveAETs) {
        Attributes refSOP = new Attributes(4);
        refSOP.setString(Tag.RetrieveAETitle, VR.AE, retrieveAETs);
        refSOP.setString(Tag.InstanceAvailability, VR.CS, availability.name());
        refSOP.setString(Tag.ReferencedSOPClassUID, VR.UI, cuid);
        refSOP.setString(Tag.ReferencedSOPInstanceUID, VR.UI, iuid);
        return refSOP;
    }

}
