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

package org.dcm4chee.archive.iocm.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dcm4che3.net.Device;
import org.dcm4chee.archive.ArchiveServiceReloaded;
import org.dcm4chee.archive.ArchiveServiceStarted;
import org.dcm4chee.archive.ArchiveServiceStopped;
import org.dcm4chee.archive.code.CodeService;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.RejectionParam;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.event.StartStopReloadEvent;
import org.dcm4chee.archive.iocm.RejectionDeleteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */

@ApplicationScoped
public class RejectedObjectCleanUpService {

    private static final Logger LOG = LoggerFactory.getLogger(RejectedObjectCleanUpService.class);

    @Inject
    private RejectionDeleteService rejectionServiceDeleteEJB;

    @Inject
    private CodeService codeService;

    private RejectionParam[] rejectionNotes;

    ScheduledFuture<?> cleanUpResult;
    private boolean running;
    private int maxNumberOfDeletes;
    private Runnable cleanUpTask = new Runnable() {
        @Override
        public void run() {

            ArrayList<Instance> rejectedObjects = new ArrayList<Instance>();
            for(RejectionParam rn : rejectionNotes) {
                if(rn.getRetentionTime() > 0) {
                Date now = new Date();
                Timestamp retentionUnitsAgo;
                retentionUnitsAgo = new Timestamp(now.getTime() - 
                        TimeUnit.MILLISECONDS.convert(
                                rn.getRetentionTime(), rn.getRetentionTimeUnit()));

                rejectedObjects.addAll(rejectionServiceDeleteEJB.findRejectedObjects(
                       codeService.findOrCreate(
                               new Code(rn.getRejectionNoteTitle())), retentionUnitsAgo, getMaxNumberOfDeletes()));
                }
            }
            rejectionServiceDeleteEJB.deleteRejected(cleanUpTask, rejectedObjects);
        }
    };

    /**
     * Initialize procedure.
     */
    public void initializeProcedure(Device device,int poll) {
        ArchiveDeviceExtension arcdevExt = device.getDeviceExtension(ArchiveDeviceExtension.class);
        this.rejectionNotes = arcdevExt.getRejectionParams();
        LOG.info("Initializing Rejected Objects CleanUp Thread");
        cleanUpResult = device.scheduleWithFixedDelay(
                cleanUpTask, 0,poll,TimeUnit.SECONDS);
        setMaxNumberOfDeletes(arcdevExt.getRejectedObjectsCleanUpMaxNumberOfDeletes());
        LOG.info("Initialized Rejected Objects CleanUp Thread with an Interval of {} SECONDS, Maximum files to delete per execution {}",poll,getMaxNumberOfDeletes());
        }
    

    
    public void startCleanUp(@Observes @ArchiveServiceStarted StartStopReloadEvent start) {
        ArchiveDeviceExtension arcDevExt = start.getDevice().getDeviceExtension(ArchiveDeviceExtension.class);
        int poll = arcDevExt.getRejectedObjectsCleanUpPollInterval();
        if(!isRunning() && poll > 0) {
            initializeProcedure(start.getDevice(), poll);
            setRunning(true);
        }

    }

    
    public void stopCleanUp(@Observes @ArchiveServiceStopped StartStopReloadEvent stop) {
        if(isRunning()) {
            cleanUpResult.cancel(false);
            cleanUpResult = null;
            setRunning(false);
            LOG.info("Stopped Rejected Objects CleanUp Thread");
        }
    }

    
    public void reconfigureCleanUp(@Observes @ArchiveServiceReloaded StartStopReloadEvent reload) {
        if(isRunning()) {
            ArchiveDeviceExtension arcDevExt = reload.getDevice().getDeviceExtension(ArchiveDeviceExtension.class);
            int poll = arcDevExt.getRejectedObjectsCleanUpPollInterval();
            cleanUpResult.cancel(false);
            setRunning(false);
            initializeProcedure(reload.getDevice(), poll);
            setRunning(true);
        }

    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }



    public int getMaxNumberOfDeletes() {
        return maxNumberOfDeletes;
    }



    public void setMaxNumberOfDeletes(int maxNumberOfDeletes) {
        this.maxNumberOfDeletes = maxNumberOfDeletes;
    }

}
