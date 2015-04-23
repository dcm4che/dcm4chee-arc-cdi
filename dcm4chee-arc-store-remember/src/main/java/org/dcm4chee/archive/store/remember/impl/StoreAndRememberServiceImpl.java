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
 * Portions created by the Initial Developer are Copyright (C) 2011
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
package org.dcm4chee.archive.store.remember.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.dto.Service;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.entity.StoreVerifyDimse;
import org.dcm4chee.archive.entity.StoreVerifyStatus;
import org.dcm4chee.archive.entity.StoreVerifyWeb;
import org.dcm4chee.archive.qido.client.QidoResponse;
import org.dcm4chee.archive.stgcmt.scp.CommitEvent;
import org.dcm4chee.archive.store.remember.StoreAndRememberService;
import org.dcm4chee.archive.store.verify.StoreVerifyEJB;
import org.dcm4chee.storage.conf.Availability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
@ApplicationScoped
public class StoreAndRememberServiceImpl implements StoreAndRememberService {

    private static final Logger LOG = LoggerFactory
            .getLogger(StoreAndRememberServiceImpl.class);

    @Inject
    private StoreVerifyEJB ejb;

    @Inject
    private IApplicationEntityCache aeCache;

    @Override
    public void addExternalLocation(String iuid, String retrieveAET,
            Availability availability) {
        ejb.addExternalLocation(iuid, retrieveAET, availability);
    }

    @Override
    public void removeExternalLocations(String iuid, String retrieveAET) {
        ejb.removeExternalLocation(iuid, retrieveAET);
    }

    @Override
    public void removeExternalLocations(String iuid, Availability availability) {
        ejb.removeExternalLocation(iuid, availability);
    }

    public void verifyCommit(@Observes @Service(ServiceType.STOREREMEMBER) CommitEvent commitEvent) {

        String transactionUID = commitEvent.getTransactionUID();
        ApplicationEntity archiveAE = null;
        try {
            archiveAE = aeCache.findApplicationEntity(commitEvent
                    .getLocalAET());
        } catch (ConfigurationException e) {
            LOG.error("Unable to find Application"
                    + " Entity for {} or {} verification failure for "
                    + "store and remember transaction {}",
                    commitEvent.getLocalAET(), commitEvent.getRemoteAET(),
                    transactionUID);
            ejb.removeDimseEntry(transactionUID);
            return;
        }
        ArchiveAEExtension archAEExt = archiveAE
                .getAEExtension(ArchiveAEExtension.class);
        Availability defaultAvailability = archAEExt
                .getDefaultExternalRetrieveAETAvailability();

        Attributes eventInfo = commitEvent.getEventInfo();
        Sequence failSops = eventInfo.getSequence(Tag.FailedSOPSequence);
        Sequence refSops = eventInfo.getSequence(Tag.ReferencedSOPSequence);
        StoreVerifyDimse dimse = ejb.getDimseEntry(transactionUID);

        if (dimse == null) {
            LOG.info("StoreAndRemember: commitment not recognized  :"
                    + transactionUID);
            return;
        }

        StoreVerifyStatus status = dimse.getStatus();
        boolean statusChanged = false;

        if (failSops == null || failSops.size() == 0) {
            // no failures
            if (status == StoreVerifyStatus.PENDING) {
                status = StoreVerifyStatus.VERIFIED;
                statusChanged = true;
            }
        } else if (refSops == null || refSops.size() == 0) {
            // no success
            if (status != StoreVerifyStatus.FAILED) {
                status = StoreVerifyStatus.FAILED;
                statusChanged = true;
            }
        } else {
            // some failures, some success
            if (status == StoreVerifyStatus.PENDING) {
                status = StoreVerifyStatus.INCOMPLETE;
                statusChanged = true;
            }
        }

        if (refSops != null) {
            for (int i = 0; i < refSops.size(); i++)
                addExternalLocation(refSops.get(i)
                        .getString(Tag.ReferencedSOPInstanceUID),
                        commitEvent.getRemoteAET(), defaultAvailability);
        }

        if (statusChanged)
            ejb.updateStatus(transactionUID, status);
    }

    @Override
    public void verifyQido(@Observes @Service(ServiceType.STOREREMEMBER) QidoResponse response) {
        StoreVerifyWeb webEntry = ejb.getWebEntry(response.getTransactionID());
        //failed attempt
        if(webEntry == null)
            return;
        String transactionID = response.getTransactionID();
        Availability defaultAvailability = null;
        try {
            ApplicationEntity archiveAE = aeCache
                    .findApplicationEntity(webEntry.getLocalAET());
            ArchiveAEExtension archAEExt = archiveAE
                    .getAEExtension(ArchiveAEExtension.class);
            defaultAvailability = archAEExt
                    .getDefaultExternalRetrieveAETAvailability();
        } catch (ConfigurationException e) {
            //failure attempt
            LOG.error("Unable to find Application"
                    + " Entity for {} or {} verification failure for "
                    + "store and remember transaction {}",
                    webEntry.getLocalAET(), webEntry.getRemoteAET(),
                    response.getTransactionID());
            ejb.removeWebEntry(transactionID);
            return;
        }
        String retrieveAET = webEntry.getRemoteAET();
        HashMap<String, Availability> verifiedSopInstances = response.getVerifiedSopInstances();
        int numToVerify = verifiedSopInstances.size();
        for (Iterator<Entry<String, Availability>> iter = verifiedSopInstances
                .entrySet().iterator(); iter.hasNext();) {
            Entry<String, Availability> instance = iter.next();
            Availability externalAvailability = instance.getValue();
            String sopUID = instance.getKey();
            if (externalAvailability.ordinal() < 2) {
                ejb.addExternalLocation(
                        sopUID,
                        retrieveAET,
                        defaultAvailability == null ? externalAvailability
                                : (externalAvailability
                                        .compareTo(defaultAvailability) <= 0 ? externalAvailability
                                        : defaultAvailability));
                iter.remove();
            }
        }

        if (verifiedSopInstances.isEmpty())
            ejb.updateStatus(transactionID, StoreVerifyStatus.VERIFIED);
        else if (verifiedSopInstances.size() < numToVerify)
            ejb.updateStatus(transactionID, StoreVerifyStatus.INCOMPLETE);
        else
            ejb.updateStatus(transactionID, StoreVerifyStatus.FAILED);
    }

}
