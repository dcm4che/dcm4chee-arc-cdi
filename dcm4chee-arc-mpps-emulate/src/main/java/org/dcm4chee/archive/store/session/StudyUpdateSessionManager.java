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

package org.dcm4chee.archive.store.session;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.ArchiveServiceReloaded;
import org.dcm4chee.archive.ArchiveServiceStarted;
import org.dcm4chee.archive.ArchiveServiceStopped;
import org.dcm4chee.archive.conf.*;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.event.StartStopReloadEvent;
import org.dcm4chee.archive.query.QueryService;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.archive.store.StoreSessionClosed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Observes store events (StoreContext, StoreSession) and triggers StudyUpdatedEvent notifications when a coarse-grain update of a study is finished
 * Configurable behavior:
 * - when store session is finished/when study is changed during the session
 * - when no more instances of a study are received for a configured amount of time
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Roman K
 */
@ApplicationScoped
public class StudyUpdateSessionManager {

    private static final Logger LOG = LoggerFactory.getLogger(StudyUpdateSessionManager.class);

    @Inject
    private StudyUpdateSessionEJB ejb;

    @Inject
    private Device device;

    @Inject
    DicomConfiguration config;

    @Inject
    Event<StudyUpdatedEvent> studyUpdatedEventTrigger;

    @Inject
    private QueryService queryService;

    private int lastPollingInterval;
    private ScheduledFuture<?> polling;

    public void onInstanceStored(@Observes StoreContext storeContext) {

        StoreSession storeSession = storeContext.getStoreSession();
        String remoteAET = storeSession.getRemoteAET();
        String studyInstanceUID = storeContext.getAttributes().getString(Tag.StudyInstanceUID);
        String seriesInstanceUID = storeContext.getAttributes().getString(Tag.SeriesInstanceUID);
        String sopInstanceUID = storeContext.getAttributes().getString(Tag.SOPInstanceUID);
        String localAET = storeSession.getLocalAET();
        StoreAction storeAction = storeContext.getStoreAction();

        // find an applicable rule
        MPPSEmulationAndStudyUpdateRule rule =
                storeSession.getDevice()
                        .getDeviceExtensionNotNull(ArchiveDeviceExtension.class)
                        .getMppsEmulationRule(remoteAET);

        // noop if no rule
        if (rule == null) return;

        // if a delay is configured => multiple associations/cluster nodes case => update the StudyStoreSession entity
        if (rule.getEmulationDelay() > -1) {
            // async call
            ejb.addStoredInstance(
                    remoteAET,
                    localAET,
                    studyInstanceUID,
                    seriesInstanceUID,
                    sopInstanceUID,
                    storeAction,
                    rule.getEmulationDelay());
        }
        // otherwise the StudyUpdatedEvent is bound to the Association/StoreSession
        else {

            // get/init pending StudyUpdatedEvent
            StudyUpdatedEvent pendingStudyUpdatedEvent = (StudyUpdatedEvent) storeSession.getProperty("pendingStudyUpdatedEvent");
            if (pendingStudyUpdatedEvent == null) {
                pendingStudyUpdatedEvent = new StudyUpdatedEvent(studyInstanceUID, remoteAET);
                storeSession.setProperty("pendingStudyUpdatedEvent", pendingStudyUpdatedEvent);
            }

            // check if there was a "study switch", i.e. an instance of a different study is sent in the same StoreSession
            if (!studyInstanceUID.equals(pendingStudyUpdatedEvent.getStudyInstanceUID())) {

                // fire StudyUpdatedEvent, async from the current thread, not blocking for the store itself
                fireStudyUpdatedEventAsync(pendingStudyUpdatedEvent);

                // create a new pendingStudyUpdatedEvent
                pendingStudyUpdatedEvent = new StudyUpdatedEvent(studyInstanceUID, remoteAET);
                storeSession.setProperty("pendingStudyUpdatedEvent", pendingStudyUpdatedEvent);
            }

            // add instance to the pending event
            pendingStudyUpdatedEvent.addStoredInstance(localAET, sopInstanceUID, seriesInstanceUID, storeAction);
        }
    }

    public void onStoreSessionClosed(@Observes @StoreSessionClosed StoreSession storeSession) {
        StudyUpdatedEvent pendingStudyUpdatedEvent = (StudyUpdatedEvent) storeSession.getProperty("pendingStudyUpdatedEvent");

        // in case of multiple associations/cluster nodes
        if (pendingStudyUpdatedEvent == null) return;

        fireStudyUpdatedEventAsync(pendingStudyUpdatedEvent);
    }


    public void onArchiveServiceStarted(@Observes @ArchiveServiceStarted StartStopReloadEvent start) {
        startPolling(lastPollingInterval = getConfiguredPollInterval());
    }

    public void onArchiveServiceStopped(@Observes @ArchiveServiceStopped StartStopReloadEvent stop) {
        stopPolling();
    }

    public void onArchiveServiceReloaded(@Observes @ArchiveServiceReloaded StartStopReloadEvent reload) {
        if (lastPollingInterval != getConfiguredPollInterval()) {
            stopPolling();
            startPolling(lastPollingInterval = getConfiguredPollInterval());
        }
    }

    protected int getConfiguredPollInterval() {
        return device
                .getDeviceExtension(ArchiveDeviceExtension.class)
                .getMppsEmulationPollInterval();
    }

    protected synchronized void startPolling(int pollInterval) {
        if (polling == null && pollInterval > 0) {
            polling = device.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        checkAndNotifyOfUpdatedStudies();
                    } catch (Exception e) {
                        LOG.error("Error while checking for updated studies (study update session tracking daemon): ", e);
                    }
                }
            }, pollInterval, pollInterval, TimeUnit.SECONDS);
            LOG.info("Study update session tracking daemon: start polling for finished updates with interval {}s", pollInterval);
        }
    }

    protected synchronized void stopPolling() {
        if (polling != null) {
            polling.cancel(false);
            polling = null;
            LOG.info("Study update session tracking daemon: stop polling for finished updates");
        }
    }

    public int checkAndNotifyOfUpdatedStudies() {
        int count = 0;
        while (notifyAboutNextFinishedUpdate()) count++;
        return count;
    }

    /**
     * @return true if a pending study update notification was fired, false if not
     */
    public boolean notifyAboutNextFinishedUpdate() {
        StudyUpdatedEvent studyUpdatedEvent = ejb.findNextFinishedStudyUpdateSession();
        if (studyUpdatedEvent == null) return false;
        fireStudyUpdatedEvent(studyUpdatedEvent);
        return true;
    }

    public void fireStudyUpdatedEventAsync(final StudyUpdatedEvent studyUpdatedEvent) {
        device.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                fireStudyUpdatedEvent(studyUpdatedEvent);
            }
        });
    }

    private void fireStudyUpdatedEvent(StudyUpdatedEvent studyUpdatedEvent) {

        // calc derived fields
        ApplicationEntity ae = device.getApplicationEntityNotNull(studyUpdatedEvent.getLocalAETs().iterator().next());
        Study studyByUID = ejb.findStudyByUID(studyUpdatedEvent.getStudyInstanceUID());
        if (studyByUID != null) {
            LOG.info("Recalculating derived fields for study {}", studyUpdatedEvent.getStudyInstanceUID());
            queryService.recalculateDerivedFields(studyByUID, ae);

            LOG.info("Triggering StudyUpdatedEvent for study {}", studyUpdatedEvent.getStudyInstanceUID());
            studyUpdatedEventTrigger.fire(studyUpdatedEvent);
        } else {
            LOG.warn("StudyUpdatedEvent was scheduled to be fired for study {}, but the study cannot be found anymore => not triggering the event", studyUpdatedEvent.getStudyInstanceUID());
        }
    }
}
