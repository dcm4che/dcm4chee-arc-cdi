/**
 * 
 */
package org.dcm4chee.archive.mpps.emulate.impl;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.UIDUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.archive.mpps.emulate.MPPSEmulator;
import org.dcm4chee.archive.mpps.event.MPPSCreate;
import org.dcm4chee.archive.mpps.event.MPPSEvent;
import org.dcm4chee.archive.mpps.event.MPPSFinal;

/**
 * @author gunter
 *
 */
@ApplicationScoped
public class MPPSEmulatorImpl implements MPPSEmulator {

    @Inject
    MPPSService mppsService;

    @Inject
    @MPPSCreate
    Event<MPPSEvent> createMPPSEvent;

    @Inject
    @MPPSFinal
    Event<MPPSEvent> finalMPPSEvent;

    @Override
    public MPPS emulatePerformedProcedureStep(ArchiveAEExtension aeArc, 
            Collection<Series> series) throws DicomServiceException {
        String mppsiuid = UIDUtils.createUID();
        Attributes mppsAttrs = createMPPS(aeArc, series);
        MPPS mpps = mppsService.createPerformedProcedureStep(aeArc,
                mppsiuid, mppsAttrs , mppsService);
        updateSeriesAttributes(aeArc, series, mppsiuid, mppsAttrs);

        ApplicationEntity ae = aeArc.getApplicationEntity();
        createMPPSEvent.fire(
                new MPPSEvent(ae, Dimse.N_CREATE_RQ,
                        setStatus(mppsAttrs, MPPS.IN_PROGRESS), mpps));
        finalMPPSEvent.fire(
                new MPPSEvent(ae, Dimse.N_SET_RQ,
                        setStatus(new Attributes(1), MPPS.COMPLETED), mpps));
        return mpps;
    }

    private Attributes setStatus(Attributes attrs, String value) {
        attrs.setString(Tag.PerformedProcedureStepStatus, VR.CS, value);
        return attrs;
    }

    private Attributes createMPPS(ArchiveAEExtension aeArc,
            Collection<Series> series) {
        Attributes attrs = new Attributes();
        // TODO Auto-generated method stub
        return attrs;
    }

    private void updateSeriesAttributes(ArchiveAEExtension aeArc,
            Collection<Series> seriess, String mppsiuid, Attributes attrs) {
        ArchiveDeviceExtension arcDev = aeArc.getApplicationEntity()
                .getDevice().getDeviceExtension(ArchiveDeviceExtension.class);
        for (Series series : seriess) {
            Attributes seriesAttrs = series.getAttributes();
            //TODO 
            series.setAttributes(attrs, 
                    arcDev.getAttributeFilter(Entity.Series),
                    arcDev.getFuzzyStr());
        }
        
    }

    
}
