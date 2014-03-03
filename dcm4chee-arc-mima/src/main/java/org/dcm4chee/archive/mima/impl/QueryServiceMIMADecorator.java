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
import java.util.EnumSet;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import javax.interceptor.Interceptor;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.QueryOption;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.query.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Decorator @Priority(Interceptor.Priority.APPLICATION)
public abstract class QueryServiceMIMADecorator implements QueryService {

    private static Logger LOG =
            LoggerFactory.getLogger(QueryServiceMIMADecorator.class);

    @Inject @Delegate
    private QueryService queryService;

    @Inject
    private IApplicationEntityCache aeCache;

    @Inject
    private PIXConsumer pixConsumer;

    @Override
    public QueryParam getQueryParam(Object source, String sourceAET,
            ArchiveAEExtension aeExt, EnumSet<QueryOption> queryOpts) {
        QueryParam queryParam = queryService.getQueryParam(
                source, sourceAET, aeExt, queryOpts);
        try {
            ApplicationEntity scrAE = aeCache.get(sourceAET);
            if (scrAE != null) {
                Device srcDev = scrAE.getDevice();
                queryParam.setDefaultIssuerOfPatientID(
                        srcDev.getIssuerOfPatientID());
                queryParam.setDefaultIssuerOfAccessionNumber(
                        srcDev.getIssuerOfAccessionNumber());
            }
        } catch (ConfigurationException e) {
            LOG.warn("Failed to access configuration for query source {} - no MIMA support:",
                    source, e);
        }
        return queryParam;
    }

    @Override
    public IDWithIssuer[] queryPatientIDs(ArchiveAEExtension aeExt,
           Attributes keys, QueryParam queryParam) {
        IDWithIssuer pid = IDWithIssuer.fromPatientIDWithIssuer(keys);
        if (pid == null)
            return IDWithIssuer.EMPTY;
        
        if (pid.getIssuer() == null) {
            if (queryParam.getDefaultIssuerOfPatientID() == null)
                return new IDWithIssuer[] { pid };
            pid.setIssuer(queryParam.getDefaultIssuerOfPatientID());
        }
        return pixConsumer.pixQuery(aeExt, pid);
    }

    @Override
    public void adjustMatch(QueryContext query, Attributes match) {
        QueryInfo info = (QueryInfo) query.getProperty(
                QueryInfo.class.getName());
        if (info == null) {
            info = new QueryInfo(query);
            query.setProperty(QueryInfo.class.getName(), info);
        }
        adjustPatientID(query.getArchiveAEExtension(), info, match);
        adjustAccessionNumbers(info, match);
    }

    private void adjustPatientID(ArchiveAEExtension arcAE, QueryInfo info,
            Attributes match) {
        IDWithIssuer pid = IDWithIssuer.fromPatientIDWithIssuer(match);
        if (pid == null)
            return;

        Issuer requestedIssuer = info.getRequestedIssuerOfPatientID();
        if (requestedIssuer == null
                && !info.isReturnOtherPatientIDs()
                && !info.isReturnOtherPatientNames())
            return;

        Issuer issuer = pid.getIssuer();
        if (issuer == null) {
            if (requestedIssuer != null) {
                match.setNull(Tag.PatientID, VR.LO);
                requestedIssuer.toIssuerOfPatientID(match);
                LOG.info("Nullify Patient ID for requested Issuer: {}",
                        requestedIssuer);
            }
            return;
        }

        PatientIDsWithPatientNames pidsWithNames =
                info.getPatientIDsWithPatientNames(pid);
        if (pidsWithNames == null) {
            pidsWithNames = new PatientIDsWithPatientNames(
                    pixConsumer.pixQuery(arcAE, pid));
            info.addPatientIDsWithPatientNames(pidsWithNames);
        }
        if (requestedIssuer != null && !requestedIssuer.matches(issuer)) {
            IDWithIssuer requestedPID = pidsWithNames.getPatientIDByIssuer(requestedIssuer);
            if (requestedPID != null) {
                match.setString(Tag.PatientID, VR.LO, requestedPID.getID());
                LOG.info("Adjust Patient ID to {}", requestedPID);
            } else {
                match.setNull(Tag.PatientID, VR.LO);
                LOG.info("Nullify Patient ID for requested Issuer: {}", 
                        requestedIssuer);
            }
            requestedIssuer.toIssuerOfPatientID(match);
        }
        if (info.isReturnOtherPatientIDs()) {
            addOtherPatientIDs(match, pidsWithNames.getPatientIDs());
        }
        if (info.isReturnOtherPatientNames()) {
            addOtherPatientNames(match, pidsWithNames);
        }
    }

    private static void addOtherPatientIDs(Attributes attrs,
            IDWithIssuer... pids) {
        Sequence seq = attrs.newSequence(Tag.OtherPatientIDsSequence,
                pids.length);
        for (IDWithIssuer pid : pids)
            seq.add(pid.toPatientIDWithIssuer(null));
        LOG.info("Add Other Patient IDs: {}", Arrays.toString(pids));
    }
    
    private void addOtherPatientNames(Attributes match,
            PatientIDsWithPatientNames pidsWithNames) {
        if (pidsWithNames.getPatientIDs().length == 1) {
            String patientName = match.getString(Tag.PatientName);
            if (patientName != null) {
                match.setString(Tag.OtherPatientNames, VR.PN, patientName);
                LOG.info("Add Other Patient Name: {}", patientName);
            }
            return;
        }
        String[] patientNames = pidsWithNames.getPatientNames();
        if (patientNames == null) {
            patientNames = queryService.queryPatientNames(pidsWithNames.getPatientIDs());
            pidsWithNames.setPatientNames(patientNames);
        }
        match.setString(Tag.OtherPatientNames, VR.PN, patientNames);
        LOG.info("Add Other Patient Names: {}", Arrays.toString(patientNames));
    }

    private void adjustAccessionNumbers(QueryInfo info, Attributes match) {
        Issuer requestedIssuer = info.getRequestedIssuerOfAccessionNumber();
        if (requestedIssuer != null) {
            adjustAccessionNumber(requestedIssuer, match);
            Sequence rqAttrsSeq = match.getSequence(Tag.RequestAttributesSequence);
            if (rqAttrsSeq != null) {
                for (Attributes rqAttrs : rqAttrsSeq)
                    adjustAccessionNumber(requestedIssuer, rqAttrs);
            }
        }
    }

    private void adjustAccessionNumber(Issuer requestedIssuer, Attributes match) {
        if (!match.containsValue(Tag.AccessionNumber))
            return;

        Issuer issuer = Issuer.valueOf(
                match.getNestedDataset(Tag.IssuerOfAccessionNumberSequence));
        if (issuer == null || !requestedIssuer.matches(issuer)) {
            match.setNull(Tag.AccessionNumber, VR.SH);
            LOG.info("Nullify Accession Number for requested Issuer: {}",
                    requestedIssuer);
        }
        match.newSequence(Tag.IssuerOfAccessionNumberSequence, 1)
            .add(requestedIssuer.toItem());
    }
}
