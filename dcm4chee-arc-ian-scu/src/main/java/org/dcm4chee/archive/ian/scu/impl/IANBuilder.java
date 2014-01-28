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
import java.util.Set;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4chee.archive.entity.PerformedProcedureStep;
import org.dcm4chee.archive.entity.SOPInstanceReference;
import org.dcm4chee.archive.entity.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class IANBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(IANBuilder.class);

    private int outstanding;

    private final String ppsiuid;
    private final String studyiuid;
    private final Attributes ian;
    private final Sequence ianRefSeriesSeq;
    private final HashMap<String, Sequence> ianRefSOPSeqOfSeries =
            new HashMap<String, Sequence>();

    private final HashMap<String, HashMap<String,String>> perfSeries =
            new HashMap<String, HashMap<String,String>>();

    public IANBuilder(PerformedProcedureStep pps) {
        Attributes ppsAttrs = pps.getAttributes();
        for (Attributes series : 
            ppsAttrs.getSequence(Tag.PerformedSeriesSequence)) {
            HashMap<String,String> refInsts =
                    perfSeriesOf(series.getString(Tag.SeriesInstanceUID));
            addRefInstsTo(
                    series.getSequence(Tag.ReferencedImageSequence), refInsts);
            addRefInstsTo(
                    series.getSequence(
                            Tag.ReferencedNonImageCompositeSOPInstanceSequence),
                            refInsts);
        }
        this.ppsiuid = pps.getSopInstanceUID();
        Attributes ssa = ppsAttrs.getNestedDataset(Tag.ScheduledStepAttributesSequence);
        this.studyiuid = ssa.getString(Tag.StudyInstanceUID);
        this.ian = new Attributes();
        ian.newSequence(Tag.ReferencedPerformedProcedureStepSequence, 1)
            .add(mkRefPPS(ppsiuid));
        this.ianRefSeriesSeq = ian.newSequence(Tag.ReferencedSeriesSequence, 1);
        ian.setString(Tag.StudyInstanceUID, VR.UI, studyiuid);
    }

    public Set<String> getPerformedSeriesInstanceUIDs() {
        return perfSeries.keySet();
    }

    public boolean allReceived() {
        return outstanding == 0;
    }

    public Attributes getIAN() {
        return ian;
    }

    private Attributes mkRefPPS(String iuid) {
        Attributes refPPS = new Attributes(3);
        refPPS.setString(Tag.ReferencedSOPClassUID, VR.UI,
                UID.ModalityPerformedProcedureStepSOPClass);
        refPPS.setString(Tag.ReferencedSOPInstanceUID, VR.UI, iuid);
        refPPS.setNull(Tag.PerformedWorkitemCodeSequence, VR.SQ);
        return refPPS ;
    }

    private HashMap<String, String> perfSeriesOf(String iuid) {
        HashMap<String, String> refInsts = perfSeries.get(iuid);
        if (refInsts == null) {
            refInsts = new HashMap<String, String>();
            perfSeries.put(iuid, refInsts);
        }
        return refInsts;
    }

    private void addRefInstsTo(Sequence seq, HashMap<String, String> refInsts) {
        if (seq != null)
            for (Attributes refInst : seq) {
                if (refInsts.put(refInst.getString(Tag.ReferencedSOPInstanceUID),
                        refInst.getString(Tag.ReferencedSOPClassUID)) != null) {
                    LOG.warn("MPPS[iuid="
                            + ppsiuid
                            + "] contains multiple references of Instance[iuid="
                            + refInst.getString(Tag.ReferencedSOPInstanceUID)
                            + ", cuid="
                            + refInst.getString(Tag.ReferencedSOPClassUID)
                            + "]");
                } else {
                    outstanding++;
                }
            }
    }

    public boolean addSOPInstanceReference(SOPInstanceReference ref) {
        if (!studyiuid.equals(ref.studyInstanceUID)) {
            LOG.warn("Study[iuid="
                    + ref.studyInstanceUID
                    + "] of received Instance[iuid="
                    + ref.sopInstanceUID
                    + ", cuid="
                    + ref.sopClassUID
                    + "] mismatch Study[iuid="
                    + studyiuid
                    + "] referenced by MPPS[iuid="
                    + ppsiuid
                    + "] - no IAN may be emitted");
            return false;
        }
        HashMap<String, String> mppsRefSeries =
                perfSeries.get(ref.seriesInstanceUID);
        if (mppsRefSeries == null) {
            LOG.warn("Series[iuid="
                    + ref.seriesInstanceUID
                    + "] of received Instance[iuid="
                    + ref.sopInstanceUID
                    + ", cuid="
                    + ref.sopClassUID
                    + "] is not referenced by MPPS[iuid="
                    + ppsiuid
                    + "] - no IAN may be emitted");
            return false;
        }
        String cuid = mppsRefSeries.remove(ref.sopInstanceUID);
        if (cuid == null) {
            LOG.warn("Received Instance[iuid="
                    + ref.sopInstanceUID
                    + ", cuid="
                    + ref.sopClassUID
                    + "] in Series["
                    + ref.seriesInstanceUID
                    + "] is not referenced by MPPS[iuid="
                    + ppsiuid
                    + "] or was received multiple times - no IAN may be emitted");
            return false;
        }
        if (!cuid.equals(ref.sopClassUID)) {
            LOG.warn("SOP Class of received Instance[iuid="
                    + ref.sopInstanceUID
                    + ", cuid="
                    + ref.sopClassUID
                    + "] differs from SOP Class[cuid="
                    + cuid
                    + "] of the Instance referenced by MPPS[iuid="
                    + ppsiuid
                    + "] - emitted IAN will reference SOP Class of received Instance");
        }
        getIANRefSOPSeq(ref.seriesInstanceUID, mppsRefSeries.size()).add(mkRefSOP(ref));
        outstanding--;
        return true;
    }

    private Sequence getIANRefSOPSeq(String iuid, int seriesSize) {
        Sequence seq = ianRefSOPSeqOfSeries.get(iuid);
        if (seq == null) {
            Attributes seriesRef = new Attributes(2);
            seq = seriesRef.newSequence(Tag.ReferencedSOPSequence, seriesSize);
            seriesRef.setString(Tag.SeriesInstanceUID, VR.UI, iuid);
            ianRefSeriesSeq.add(seriesRef);
            ianRefSOPSeqOfSeries.put(iuid, seq);
        }
        return seq;
    }

    private static Attributes mkRefSOP(SOPInstanceReference sopRef) {
        Attributes refSOP = new Attributes(4);
        Utils.setRetrieveAET(refSOP, sopRef.retrieveAETs, sopRef.externalRetrieveAET);
        refSOP.setString(Tag.InstanceAvailability, VR.CS, sopRef.availability.name());
        refSOP.setString(Tag.ReferencedSOPClassUID, VR.UI, sopRef.sopClassUID);
        refSOP.setString(Tag.ReferencedSOPInstanceUID, VR.UI, sopRef.sopInstanceUID);
        return refSOP;
    }

}
