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

package org.dcm4chee.archive.mpps.emulate.impl;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.ArchiveServiceReloaded;
import org.dcm4chee.archive.ArchiveServiceStarted;
import org.dcm4chee.archive.ArchiveServiceStopped;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.MPPSEmulationRule;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.event.StartStopReloadEvent;
import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.archive.mpps.emulate.MPPSEmulator;
import org.dcm4chee.archive.mpps.emulate.impl.MPPSEmulatorEJB.EmulationResult;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@ApplicationScoped
public class MPPSEmulatorImpl implements MPPSEmulator {

    private static final Logger LOG = LoggerFactory
            .getLogger(MPPSEmulatorImpl.class);

    @Inject
    private Device device;

    @Inject
    private MPPSService mppsService;

    @Inject
    private MPPSEmulatorEJB ejb;

    private int currentPollInterval;
    private ScheduledFuture<?> polling;

    @Override
    public MPPS emulatePerformedProcedureStep(String emulatorAET,
            String sourceAET, String studyInstanceUID)
            throws DicomServiceException {
        ApplicationEntity ae = device.getApplicationEntity(emulatorAET);
        MPPS mpps = ejb.emulatePerformedProcedureStep(ae, sourceAET,
                studyInstanceUID, mppsService);
        fireMPPSEvents(ae, mpps.getAttributes(), mpps);
        return mpps;
    }

    private void fireMPPSEvents(ApplicationEntity ae, Attributes attrs,
            MPPS mpps) {
        mppsService.fireCreateMPPSEvent(ae, setStatus(attrs, MPPS.IN_PROGRESS),
                mpps);
        mppsService.fireFinalMPPSEvent(ae,
                setStatus(new Attributes(1), MPPS.COMPLETED), mpps);
    }

    private static Attributes setStatus(Attributes attrs, String value) {
        attrs.setString(Tag.PerformedProcedureStepStatus, VR.CS, value);
        return attrs;
    }

    @Override
    public void scheduleMPPSEmulation(String sourceAET,
            String studyInstanceUID, MPPSEmulationRule mppsEmulationRule) {
        ejb.scheduleMPPSEmulation(sourceAET, studyInstanceUID,
                mppsEmulationRule);
    }

    @Override
    public MPPS emulateNextScheduled() throws DicomServiceException {
        EmulationResult result = ejb.emulateNextPerformedProcedureStep(device,
                mppsService);
        if (result == null)
            return null;

        MPPS mpps = result.mpps;
        fireMPPSEvents(result.ae, mpps.getAttributes(), mpps);
        return mpps;
    }

    @Override
    public int emulateAllScheduled() throws DicomServiceException {
        int count = 0;
        while (emulateNextScheduled() != null)
            count++;
        return count;
    }

    public void onStoreInstance(@Observes StoreContext storeContext) {
        switch (storeContext.getStoreAction()) {
        case REPLACE:
        case RESTORE:
        case STORE:
        case UPDATEDB:
            break;
        default:
            return;
        }

        StoreSession storeSession = storeContext.getStoreSession();
        ArchiveAEExtension arcAE = storeSession.getArchiveAEExtension();
        MPPSEmulationRule mppsEmulationRule = arcAE
                .getMppsEmulationRule(storeSession.getRemoteAET());
        if (mppsEmulationRule != null)
            ejb.scheduleMPPSEmulation(storeSession.getRemoteAET(), storeContext
                    .getAttributes().getString(Tag.StudyInstanceUID),
                    mppsEmulationRule);
    }

    public void onArchiveServiceStarted(
            @Observes @ArchiveServiceStarted StartStopReloadEvent start) {
        startPolling(pollInterval());
    }

    public void onArchiveServiceStopped(
            @Observes @ArchiveServiceStopped StartStopReloadEvent stop) {
        stopPolling();
    }

    public void onArchiveSeriviceReloaded(
            @Observes @ArchiveServiceReloaded StartStopReloadEvent reload) {
        int pollInterval = pollInterval();
        if (currentPollInterval != pollInterval) {
            stopPolling();
            startPolling(pollInterval);
        }
    }

    private int pollInterval() {
        return device.getDeviceExtension(ArchiveDeviceExtension.class)
                .getMppsEmulationPollInterval();
    }

    private synchronized void startPolling(int pollInterval) {
        if (polling == null && pollInterval > 0) {
            polling = device.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        emulateAllScheduled();
                    } catch (DicomServiceException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }, pollInterval, pollInterval, TimeUnit.SECONDS);
            currentPollInterval = pollInterval;
            LOG.info(
                    "MPPS Emulator Service: start polling for scheduled MPPS Emulations with interval {}s",
                    pollInterval);
        }
    }

    private synchronized void stopPolling() {
        if (polling != null) {
            polling.cancel(false);
            polling = null;
            LOG.info("MPPS Emulator Service: stop polling for scheduled MPPS Emulations");
        }
    }
}
