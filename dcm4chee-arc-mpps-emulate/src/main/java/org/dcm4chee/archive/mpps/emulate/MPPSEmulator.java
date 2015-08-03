/*
 * *** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2015
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */

package org.dcm4chee.archive.mpps.emulate;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.archive.store.session.StudyUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author Roman K
 */
@ApplicationScoped
public class MPPSEmulator {

    private static Logger LOG = LoggerFactory.getLogger(MPPSEmulator.class);

    @Inject
    private MPPSEmulatorEJB ejb;

    @Inject
    private Device device;

    @Inject
    private MPPSService mppsService;

    public MPPS onStudyUpdated(@Observes StudyUpdatedEvent studyUpdatedEvent) {

        MPPS mpps = null;
        try {
            mpps = ejb.emulatePerformedProcedureStep(studyUpdatedEvent);
        } catch (DicomServiceException e) {
            LOG.error("Cannot emulate MPPS",e);
            return null;
        }

        ApplicationEntity applicationEntity;
        try {
            applicationEntity = device.getApplicationEntityNotNull(studyUpdatedEvent.getLocalAET());
        } catch (Exception e) {
            LOG.error("Archive AE {} not found", studyUpdatedEvent.getLocalAET(), e);
            return null;
        }

        //TODO: that should be done by MPPS service itself
        if (mpps != null) {
            mppsService.fireCreateMPPSEvent(applicationEntity, MPPSEmulator.setStatus(mpps.getAttributes(), MPPS.IN_PROGRESS), mpps);
            mppsService.fireFinalMPPSEvent(applicationEntity, MPPSEmulator.setStatus(new Attributes(1), MPPS.COMPLETED), mpps);
        }

        return mpps;
    }


    private static Attributes setStatus(Attributes attrs, String value) {
        attrs.setString(Tag.PerformedProcedureStepStatus, VR.CS, value);
        return attrs;
    }


}
