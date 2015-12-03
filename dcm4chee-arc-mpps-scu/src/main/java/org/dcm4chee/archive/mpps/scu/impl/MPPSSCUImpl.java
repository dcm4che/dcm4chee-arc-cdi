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

package org.dcm4chee.archive.mpps.scu.impl;

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.DimseRSP;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.mpps.scu.MPPSSCU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@ApplicationScoped
public class MPPSSCUImpl implements MPPSSCU {

    private static final Logger LOG = LoggerFactory.getLogger(MPPSSCUImpl.class);

    @Inject
    private IApplicationEntityCache aeCache;

    @Inject
    private Device device;

    @Override
    public void sendMPPS(Dimse dimse, String localAET, String remoteAET,
            String iuid, Attributes attrs) throws DicomServiceException {
        ApplicationEntity localAE = device
                .getApplicationEntity(localAET);
        if (localAE == null) {
            LOG.warn("Failed to forward MPPS to {} - no such local AE: {}",
                    remoteAET, localAET);
            return;
        }
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.addPresentationContext(
                        new PresentationContext(
                                1,
                                UID.ModalityPerformedProcedureStepSOPClass,
                                UID.ExplicitVRLittleEndian,
                                UID.ImplicitVRLittleEndian));
        try {
            ApplicationEntity remoteAE = aeCache
                    .findApplicationEntity(remoteAET);
            Association as = localAE.connect(remoteAE, aarq);
            DimseRSP rsp = sendMPPS(as, dimse, iuid, attrs);
            rsp.next();
            try {
                as.release();
            } catch (IOException e) {
                LOG.info("{}: Failed to release Association to {}", as, remoteAET);
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, "Failed to forward MPPS to " + remoteAET, e);
        }
    }

    private DimseRSP sendMPPS(Association as, Dimse dimse, String iuid,
            Attributes attrs) throws IOException, InterruptedException {
        switch (dimse) {
            case N_CREATE_RQ:
                return as.ncreate(
                        UID.ModalityPerformedProcedureStepSOPClass,
                        iuid, attrs, null);
            case N_SET_RQ:
                return as.nset(
                        UID.ModalityPerformedProcedureStepSOPClass,
                        iuid, attrs, null);
            default:
                throw new IllegalArgumentException("dimse: " + dimse);
        }
    }

}
