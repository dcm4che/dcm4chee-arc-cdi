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

package org.dcm4chee.archive.mpps.scp;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Association;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.Status;
import org.dcm4che.net.service.BasicMPPSSCP;
import org.dcm4che.net.service.DicomService;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.entity.PerformedProcedureStep;
import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.archive.mpps.event.MPPSCreate;
import org.dcm4chee.archive.mpps.event.MPPSEvent;
import org.dcm4chee.archive.mpps.event.MPPSUpdate;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@ApplicationScoped
@Typed(DicomService.class)
public class MPPSSCP extends BasicMPPSSCP implements DicomService {

    @Inject
    private MPPSService mppsService;

    @Inject
    @MPPSCreate
    Event<MPPSEvent> createMPPSEvent;

    @Inject
    @MPPSUpdate
    Event<MPPSEvent> updateMPPSEvent;

    @Override
    protected Attributes create(Association as, Attributes rq,
            Attributes rqAttrs, Attributes rsp) throws DicomServiceException {
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
        ApplicationEntity ae = as.getApplicationEntity();
        ArchiveAEExtension aeExt = ae.getAEExtension(ArchiveAEExtension.class);
        try {
            PerformedProcedureStep mpps =
                    mppsService.createPerformedProcedureStep(
                            mppsService, iuid , rqAttrs, aeExt);
            
            createMPPSEvent.fire(
                    new MPPSEvent(Dimse.N_CREATE_RQ, mpps, aeExt, rqAttrs));
        } catch (DicomServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
        return null;
    }

    @Override
    protected Attributes set(Association as, Attributes rq, Attributes rqAttrs,
            Attributes rsp) throws DicomServiceException {
        String iuid = rq.getString(Tag.RequestedSOPInstanceUID);
        ApplicationEntity ae = as.getApplicationEntity();
        ArchiveAEExtension aeExt = ae.getAEExtension(ArchiveAEExtension.class);
        try {
            PerformedProcedureStep mpps =
                    mppsService.updatePerformedProcedureStep(
                            mppsService, iuid, rqAttrs, aeExt);

            updateMPPSEvent.fire(
                    new MPPSEvent(Dimse.N_SET_RQ, mpps, aeExt, rqAttrs));
        } catch (DicomServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }

        return null;
    }

}
