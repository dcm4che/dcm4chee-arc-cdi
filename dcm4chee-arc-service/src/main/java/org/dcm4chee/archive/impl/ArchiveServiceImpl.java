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
package org.dcm4chee.archive.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Code;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.imageio.codec.ImageWriterFactory;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.net.hl7.service.HL7Service;
import org.dcm4che3.net.hl7.service.HL7ServiceRegistry;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.DicomService;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4chee.archive.ArchiveService;
import org.dcm4chee.archive.ArchiveServiceStarted;
import org.dcm4chee.archive.ArchiveServiceStopped;
import org.dcm4chee.archive.code.CodeService;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.dto.Participant;
import org.dcm4chee.archive.event.ConnectionEventSource;
import org.dcm4chee.archive.event.LocalSource;
import org.dcm4chee.archive.event.StartStopEvent;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Singleton
@Startup
public class ArchiveServiceImpl implements ArchiveService {
    
    @Inject
    private IApplicationEntityCache aeCache;

    private static final String DEVICE_NAME_PROPERTY =
            "org.dcm4chee.archive.deviceName";
    private static final String DEF_DEVICE_NAME =
            "dcm4chee-arc";
    private static String[] JBOSS_PROPERITIES = {
        "jboss.home",
        "jboss.modules",
        "jboss.server.base",
        "jboss.server.config",
        "jboss.server.data",
        "jboss.server.deploy",
        "jboss.server.log",
        "jboss.server.temp",
    };

    private ExecutorService executor;

    private ScheduledExecutorService scheduledExecutor;

    @Inject
    private Instance<DicomService> dicomServices;

    @Inject
    private Instance<HL7Service> hl7Services;

    @Inject
    private DicomConfiguration conf;

    @Inject
    private CodeService codeService;
    
    @Inject @ArchiveServiceStarted
    private Event<StartStopEvent> archiveServiceStarted;
    
    @Inject @ArchiveServiceStopped
    private Event<StartStopEvent> archiveServiceStopped;
    
    @Inject
    private ConnectionEventSource connectionEventSource;

    private Device device;

    private boolean running;

    private final DicomService echoscp = new BasicCEchoSCP();

    private final DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();

    private final HL7ServiceRegistry hl7ServiceRegistry = new HL7ServiceRegistry();

    private static void addJBossDirURLSystemProperties() {
        for (String key : JBOSS_PROPERITIES) {
            String url = new File(System.getProperty(key + ".dir"))
                .toURI().toString();
            System.setProperty(key + ".url", url.substring(0, url.length()-1));
        }
    }

    private Device findDevice() throws ConfigurationException {
        return conf.findDevice(
                System.getProperty(DEVICE_NAME_PROPERTY, DEF_DEVICE_NAME));
    }


    @PostConstruct
    public void init() {
        addJBossDirURLSystemProperties();
        try {
            executor = Executors.newCachedThreadPool();
            scheduledExecutor = Executors.newScheduledThreadPool(10);
            device = findDevice();
            device.setConnectionMonitor(connectionEventSource);
            findOrCreateRejectionCodes(device);
            initImageReaderFactory();
            initImageWriterFactory();
            device.setExecutor(executor);
            device.setScheduledExecutor(scheduledExecutor);
            serviceRegistry.addDicomService(echoscp);
            for (DicomService service : dicomServices) {
                serviceRegistry.addDicomService(service);
            }
            for (HL7Service service : hl7Services) {
                hl7ServiceRegistry.addHL7Service(service);
            }
            device.setDimseRQHandler(serviceRegistry);
            HL7DeviceExtension hl7Extension = 
                    device.getDeviceExtension(HL7DeviceExtension.class);
            if (hl7Extension != null) {
                hl7Extension.setHL7MessageListener(hl7ServiceRegistry);
            }
            start(new LocalSource());
        } catch (RuntimeException re) {
            shutdown(executor);
            shutdown(scheduledExecutor);
            throw re;
        } catch (Exception e) {
            shutdown(executor);
            shutdown(scheduledExecutor);
            throw new RuntimeException(e);
        }
    }

    private void shutdown(ExecutorService executor) {
        if (executor != null)
            executor.shutdown();
    }

    @PreDestroy
    public void destroy() {
        stop(new LocalSource());

        serviceRegistry.removeDicomService(echoscp);
        for (DicomService service : dicomServices) {
            serviceRegistry.removeDicomService(service);
        }
        for (HL7Service service : hl7Services) {
            hl7ServiceRegistry.removeHL7Service(service);
        }
        shutdown(executor);
        shutdown(scheduledExecutor);
    }

    @Override
    public void start(Participant source) throws Exception {
        
        device.bindConnections();
        running = true;
        archiveServiceStarted.fire(new StartStopEvent(device,source));
    }

    @Override
    public void stop(Participant source) {
        
        device.unbindConnections();
        running = false;
        archiveServiceStopped.fire(new StartStopEvent(device,source));
    }

    @Override
    public void reload() throws Exception {
        aeCache.clear();
        device.reconfigure(findDevice());
        findOrCreateRejectionCodes(device);
        initImageReaderFactory();
        initImageWriterFactory();
        device.rebindConnections();
    }

    @Override
    @Produces
    public Device getDevice() {
        return device;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void findOrCreateRejectionCodes(Device dev) {
        Collection<org.dcm4chee.archive.entity.Code> found =
                new ArrayList<org.dcm4chee.archive.entity.Code>();
        ArchiveDeviceExtension arcDev =
                dev.getDeviceExtensionNotNull(ArchiveDeviceExtension.class);
        arcDev.setIncorrectWorklistEntrySelectedCode(
                findOrCreate(arcDev.getIncorrectWorklistEntrySelectedCode(), found));
        arcDev.setRejectedForQualityReasonsCode(
                findOrCreate(arcDev.getRejectedForQualityReasonsCode(), found));
        arcDev.setRejectedForPatientSafetyReasonsCode(
                findOrCreate(arcDev.getRejectedForPatientSafetyReasonsCode(), found));
        arcDev.setIncorrectModalityWorklistEntryCode(
                findOrCreate(arcDev.getIncorrectModalityWorklistEntryCode(), found));
        arcDev.setDataRetentionPeriodExpiredCode(
                findOrCreate(arcDev.getDataRetentionPeriodExpiredCode(), found));
        for (ApplicationEntity ae : dev.getApplicationEntities()) {
            ArchiveAEExtension arcAE = ae.getAEExtension(ArchiveAEExtension.class);
            Code[] codes = arcAE.getShowInstancesRejectedByCodes();
            for (int i = 0; i < codes.length; i++) {
                codes[i] = findOrCreate(codes[i], found);
            }
        }
    }

    private Code findOrCreate(Code code, Collection<org.dcm4chee.archive.entity.Code> found) {
        try {
            return (org.dcm4chee.archive.entity.Code) code;
        } catch (ClassCastException e) {
            for (org.dcm4chee.archive.entity.Code code2 : found) {
                if (code2.equalsIgnoreMeaning(code))
                    return code2;
            }
            org.dcm4chee.archive.entity.Code code2 = codeService.findOrCreate(
                        new org.dcm4chee.archive.entity.Code(code));
            found.add(code2);
            return code2;
        }
    }

    private void initImageReaderFactory() {
        ImageReaderExtension ext = device.getDeviceExtension(ImageReaderExtension.class);
        if (ext != null)
            ImageReaderFactory.setDefault(ext.getImageReaderFactory());
        else
            ImageReaderFactory.resetDefault();
    }

    private void initImageWriterFactory() {
        ImageWriterExtension ext = device.getDeviceExtension(ImageWriterExtension.class);
        if (ext != null)
            ImageWriterFactory.setDefault(ext.getImageWriterFactory());
        else
            ImageWriterFactory.resetDefault();
    }
}
