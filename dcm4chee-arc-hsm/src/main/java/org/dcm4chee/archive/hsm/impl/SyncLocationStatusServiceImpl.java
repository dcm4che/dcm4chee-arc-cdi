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
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
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
package org.dcm4chee.archive.hsm.impl;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dcm4che3.net.Device;
import org.dcm4chee.archive.ArchiveServiceReloaded;
import org.dcm4chee.archive.ArchiveServiceStarted;
import org.dcm4chee.archive.ArchiveServiceStopped;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.event.StartStopReloadEvent;
import org.dcm4chee.archive.hsm.SyncLocationStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Steve Kroetsch<stevekroetsch@hotmail.com>
 *
 */
public class SyncLocationStatusServiceImpl implements SyncLocationStatusService {

    private static final Logger LOG = LoggerFactory
            .getLogger(SyncLocationStatusServiceImpl.class);

    @Inject
    private Device device;

    @Inject
    private SyncLocationStatusServiceEJB ejb;

    private int currentPollInterval;
    private ScheduledFuture<?> polling;

    @Override
    public int checkStatus() throws IOException {
        return ejb.checkStatus();
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
                .getSyncLocationStatusPollInterval();
    }

    private synchronized void startPolling(int pollInterval) {
        if (polling == null && pollInterval > 0) {
            polling = device.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        checkStatus();
                    } catch (IOException e) {
                        LOG.error("Failed to check status", e);
                    }
                }
            }, pollInterval, pollInterval, TimeUnit.SECONDS);
            currentPollInterval = pollInterval;
            LOG.info(
                    "Sync Location Status Service: start polling for location status changes with interval {}s",
                    pollInterval);
        }
    }

    private synchronized void stopPolling() {
        if (polling != null) {
            polling.cancel(false);
            polling = null;
            LOG.info("Sync Location Status Service: stop polling for location status changes");
        }
    }

}
