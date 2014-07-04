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

package org.dcm4chee.archive.mima.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.entity.QPatient;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.query.util.QueryBuilder;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.hibernate.HibernateQuery;

/**
 * Attributes coercion according to MIMA specs.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * 
 */
@ApplicationScoped
class MIMAAttributeCoercion {

    private static Logger LOG = LoggerFactory
            .getLogger(MIMAAttributeCoercion.class);

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    @Inject
    private PIXConsumer pixConsumer;

    public void coerce(ArchiveAEExtension arcAE, MIMAInfo info, Attributes attrs) {
        coercePatientIDsAndPatientNames(arcAE, info, attrs);
        coerceAccessionNumber(info, attrs);
    }

    private void coercePatientIDsAndPatientNames(ArchiveAEExtension arcAE,
            MIMAInfo info, Attributes attrs) {
        
        // ids contained in the response instance
        Set<IDWithIssuer> pids = IDWithIssuer.pidsOf(attrs); 

        if (pids == null || pids.size() == 0)
            return;

        IDWithIssuer coercedRootID = null;
        Set<IDWithIssuer> coercedOtherIDs = new HashSet<IDWithIssuer>();
        
        Issuer requestedIssuer = info.getRequestedIssuerOfPatientID();
        if (requestedIssuer == null && !info.isReturnOtherPatientIDs()
                && !info.isReturnOtherPatientNames())
            return;

        // group together all the patient's pids and the linked ids of each pid,
        // avoiding re-running a pix query when not necessary (i.e. using the
        // cached PIX responses).
        for (IDWithIssuer pid : pids) {

            // first, add the id itself, if not already added.
            // if the pid is already found in the list, all the rest is skipped 
            // as we consider the pix query to be simmetrical and we don't need
            // to perform the reverse pix query
            if (!containsMatch(coercedOtherIDs,pid))
            {
                coercedOtherIDs.add(pid);
    
                // Second, search for linked ids.
                // If a PIX query response already containing the pid is found in
                // the cache, reuse the cached PIX response, otherwise make a new
                // PIX query
                IDWithIssuer[] linkedIDs = info.getCachedPixResponse(pid);
                if (linkedIDs == null) {
                    linkedIDs = pixConsumer.pixQuery(arcAE, pid);
                    info.cachePixResponse(linkedIDs);
                }
    
                // third, add the linked ids, if not already added.
                for (IDWithIssuer linkedID : linkedIDs)
                    if (!containsMatch(coercedOtherIDs,linkedID))
                        coercedOtherIDs.add(linkedID);
            }
        }

        // calculate the returned root id, i.e. the id among the collected
        // coercedOtherIDs having the requested issuer (if any)
        if (requestedIssuer != null) {
            for (IDWithIssuer pid : coercedOtherIDs)
                if (pid.getIssuer() != null
                        && requestedIssuer.matches(pid.getIssuer())) {
                    coercedRootID = pid;
                    break;
                }

            if (coercedRootID == null) {
                attrs.setNull(Tag.PatientID, VR.LO);
                requestedIssuer.toIssuerOfPatientID(attrs);
                LOG.info("Nullify Patient ID for requested Issuer: {}",
                        requestedIssuer);
            } else {
                IDWithIssuer originalRootID = IDWithIssuer.pidOf(attrs);
                if (coercedRootID != originalRootID) {
                    attrs.setString(Tag.PatientID, VR.LO, coercedRootID.getID());
                    LOG.info("Adjust Patient ID to {}", coercedRootID);
                }
            }
        }

        // if requested, add the whole set of other patient ids to the response
        if (info.isReturnOtherPatientIDs()) {
            addOtherPatientIDs(attrs, coercedOtherIDs);
        }

        // if requested, add the whole set of patient names to the response
        if (info.isReturnOtherPatientNames()) {
            addOtherPatientNames(attrs, coercedOtherIDs, info);
        }
    }

    private static void addOtherPatientIDs(Attributes attrs,
            Set<IDWithIssuer> pids) {
        Sequence seq = attrs.newSequence(Tag.OtherPatientIDsSequence,
                pids.size());
        for (IDWithIssuer pid : pids)
            seq.add(pid.exportPatientIDWithIssuer(null));
        LOG.info("Add Other Patient IDs: {}", Arrays.toString(pids.toArray()));
    }

    private void addOtherPatientNames(Attributes match, Set<IDWithIssuer> pids,
            MIMAInfo info) {

        String[] patientNames = info.getPatientNamesFromCache(pids);

        if (patientNames == null) {
            patientNames = queryPatientNames(pids.toArray(new IDWithIssuer[pids
                    .size()]));
            info.cachePatientNames(pids, patientNames);
        }

        match.setString(Tag.OtherPatientNames, VR.PN, patientNames);
        LOG.info("Add Other Patient Names: {}", Arrays.toString(patientNames));
    }

    private String[] queryPatientNames(IDWithIssuer[] pids) {
        HashSet<String> c = new HashSet<String>(pids.length * 4 / 3 + 1);
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QueryBuilder.pids(pids, false));
        builder.and(QPatient.patient.mergedWith.isNull());
        List<Tuple> tuples = new HibernateQuery(em.unwrap(Session.class))
                .from(QPatient.patient).where(builder)
                .list(QPatient.patient.pk, QPatient.patient.encodedAttributes);
        for (Tuple tuple : tuples)
            c.add(Utils.decodeAttributes(tuple.get(1, byte[].class)).getString(
                    Tag.PatientName));
        c.remove(null);
        return c.toArray(new String[c.size()]);
    }

    private void coerceAccessionNumber(MIMAInfo info, Attributes attrs) {
        Issuer requestedIssuer = info.getRequestedIssuerOfAccessionNumber();
        if (requestedIssuer != null) {
            adjustAccessionNumber(requestedIssuer, attrs);
            Sequence rqAttrsSeq = attrs
                    .getSequence(Tag.RequestAttributesSequence);
            if (rqAttrsSeq != null) {
                for (Attributes rqAttrs : rqAttrsSeq)
                    adjustAccessionNumber(requestedIssuer, rqAttrs);
            }
        }
    }

    private void adjustAccessionNumber(Issuer requestedIssuer, Attributes attrs) {
        if (!attrs.containsValue(Tag.AccessionNumber))
            return;

        Issuer issuer = Issuer.valueOf(attrs
                .getNestedDataset(Tag.IssuerOfAccessionNumberSequence));
        if (issuer == null || !requestedIssuer.matches(issuer)) {
            attrs.setNull(Tag.AccessionNumber, VR.SH);
            LOG.info("Nullify Accession Number for requested Issuer: {}",
                    requestedIssuer);
        }
        attrs.newSequence(Tag.IssuerOfAccessionNumberSequence, 1).add(
                requestedIssuer.toItem());
    }
    
    private boolean containsMatch (Set<IDWithIssuer> set, IDWithIssuer id)
    {
        if (set == null || id == null)
            return false;

        boolean contains = false;
        
        for (IDWithIssuer idOfSet : set)
            if (idOfSet!=null && idOfSet.matches(id)) {
                contains = true;
                break;
            }
                
        return contains;
    }

}
