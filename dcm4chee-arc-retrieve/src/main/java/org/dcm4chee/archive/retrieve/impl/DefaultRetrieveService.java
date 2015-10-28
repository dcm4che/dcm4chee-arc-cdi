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

package org.dcm4chee.archive.retrieve.impl;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.ExternalLocationTuple;
import org.dcm4chee.archive.entity.ExternalRetrieveLocation;
import org.dcm4chee.archive.entity.QLocation;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.QSeries;
import org.dcm4chee.archive.entity.QStudy;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.locationmgmt.LocationMgmt;
import org.dcm4chee.archive.query.util.QueryBuilder;
import org.dcm4chee.archive.retrieve.RetrieveContext;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.jboss.logging.Logger;

import com.mysema.query.Tuple;
import com.mysema.query.types.Expression;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@ApplicationScoped
public class DefaultRetrieveService implements RetrieveService {

    private static final Logger LOG = Logger.getLogger(DefaultRetrieveService.class);

    private static final Expression<?>[] SELECT = {
        QLocation.location.storagePath,
        QLocation.location.entryName,
        QLocation.location.transferSyntaxUID,
        QLocation.location.timeZone,
        QLocation.location.storageSystemGroupID,
        QLocation.location.storageSystemID,
        QLocation.location.withoutBulkData,
        QSeries.series.pk,
        QInstance.instance.pk,
        QInstance.instance.sopClassUID,
        QInstance.instance.sopInstanceUID,
        QInstance.instance.retrieveAETs,
        QueryBuilder.instanceAttributesBlob.encodedAttributes,
        QSeries.series.seriesInstanceUID,
        QStudy.study.studyInstanceUID
    };

    @Inject
    private Device device;

    @Inject
    private RetrieveServiceEJB ejb;

    @Inject
    private LocationMgmt locationManager;

    public RetrieveContext createRetrieveContext(RetrieveService service,
            String sourceAET, ArchiveAEExtension arcAE) {
        return new RetrieveContextImpl(service, sourceAET, arcAE);
    }

    @Override
    public IDWithIssuer[] queryPatientIDs(RetrieveContext context,
            Attributes keys) {
        IDWithIssuer pid = IDWithIssuer.pidOf(keys);
        return pid == null ? IDWithIssuer.EMPTY : new IDWithIssuer[] { pid };
    }

    @Override
    public List<ArchiveInstanceLocator> calculateMatches(IDWithIssuer[] pids,
            Attributes keys, QueryParam queryParam, boolean withoutBulkData) {

        return locate(
                ejb.query(SELECT,
                        pids,
                        keys.getStrings(Tag.StudyInstanceUID),
                        keys.getStrings(Tag.SeriesInstanceUID),
                        keys.getStrings(Tag.SOPInstanceUID),
                        queryParam),
                withoutBulkData
        );
    }

    /**
     * Given study and/or series and/or object uids, performs the query and
     * returns references to the instances.
     * The list contains one ArchiveInstanceLocator for each instance. 
     * Additional locations of an instance are available in a chain of FallBackLocator.
     * Note: The external locations are only available if the instance has no Location. 
     */
    @Override
    public List<ArchiveInstanceLocator> calculateMatches(String studyIUID,
            String seriesIUID, String objectIUID, QueryParam queryParam, boolean withoutBulkData) {

        return locate(
                ejb.query(SELECT,
                        null,
                        studyIUID == null ? null : new String[]{studyIUID},
                        seriesIUID == null ? null : new String[]{seriesIUID},
                        objectIUID == null ? null : new String[]{objectIUID},
                        queryParam),
                withoutBulkData);
    }

    private List<ArchiveInstanceLocator> locate(List<Tuple> tuples, boolean withoutBulkData) {

        List<String> studiesUpdatedForAccess = new ArrayList<String>();
        List<ArchiveInstanceLocator> locators = new ArrayList<ArchiveInstanceLocator>(tuples.size());
        StorageDeviceExtension storageConf = device.getDeviceExtension(StorageDeviceExtension.class);
        long instPk = -1;
        long seriesPk = -1;
        Attributes seriesAttrs = null;
        ArchiveInstanceLocator locator = null;

        for (Tuple tuple : tuples) {
            Boolean locatorWithoutBulkData = tuple.get(QLocation.location.withoutBulkData);
            String retrieveAETs = tuple.get(QInstance.instance.retrieveAETs);
            if (locatorWithoutBulkData == null) { // No Location
                if (retrieveAETs == null) // No external location
                    continue;
            } else if (locator != null && !locator.isWithoutBulkdata() && locatorWithoutBulkData && !withoutBulkData) {
            	LOG.debug("Skip Locator withoutBulkData! We have already a locator with bulkData and request is also for 'with bulkdata'");
            	continue;
            }
            long nextSeriesPk = tuple.get(QSeries.series.pk);
            long nextInstPk = tuple.get(QInstance.instance.pk);

            if (seriesPk != nextSeriesPk) {
                seriesAttrs = ejb.getSeriesAttributes(nextSeriesPk);
                seriesPk = nextSeriesPk;
                
                String groupID = tuple.get(QLocation.location.storageSystemGroupID);
                String currentStudy = tuple.get(QStudy.study.studyInstanceUID);
                
                if(!studiesUpdatedForAccess.contains(currentStudy) 
                        && groupID !=null) {
                    locationManager.findOrCreateStudyOnStorageGroup(currentStudy, 
                            groupID);
                }
                
            }
            if (instPk != nextInstPk) {
                if (locator != null)
                    locators.add(locator);
                locator = null;
            }
            instPk = nextInstPk;
            if(tuple.get(QLocation.location.storageSystemGroupID) == null) { //can only be null if instance has no location!
                locator = augmentExternalLocations(updateLocator(storageConf, 
                        locator, seriesAttrs, tuple));
            } else {
            	if (locator != null && !withoutBulkData && !locatorWithoutBulkData && locator.isWithoutBulkdata())
            		locator = null; //replace current locator (withoutBulkData) with locator with bulkdata 
            	locator = updateLocator(storageConf, locator, seriesAttrs, tuple);
            }
        }
        if (locator != null)
            locators.add(locator);
        return locators;
    }

    private ArchiveInstanceLocator augmentExternalLocations(
            ArchiveInstanceLocator updateLocator) {
        ArrayList<ExternalRetrieveLocation> externalLocations =
                (ArrayList<ExternalRetrieveLocation>) 
                ejb.getExternalLocations(updateLocator.iuid);
        for(ExternalRetrieveLocation loc : externalLocations)
        updateLocator.getExternalLocators().add(new ExternalLocationTuple(
                loc.getRetrieveDeviceName(), loc.getAvailability()));
            //update external retrieveLocation 
            return updateLocator;
    }

    private static ArchiveInstanceLocator updateLocator(
            StorageDeviceExtension storageConf,
            ArchiveInstanceLocator locator,
            Attributes seriesAttrs,
            Tuple tuple) {
        String cuid = tuple.get(QInstance.instance.sopClassUID);
        String iuid = tuple.get(QInstance.instance.sopInstanceUID);
        String retrieveAETs = tuple.get(QInstance.instance.retrieveAETs);
        String storageSystemGroupID = tuple.get(QLocation.location.storageSystemGroupID);
        String storageSystemID = tuple.get(QLocation.location.storageSystemID);
        String storagePath = tuple.get(QLocation.location.storagePath);
        String entryName = tuple.get(QLocation.location.entryName);
        String tsuid = tuple.get(QLocation.location.transferSyntaxUID);
        String timeZone = tuple.get(QLocation.location.timeZone);
        String seriesInstanceUID = tuple.get(QSeries.series.seriesInstanceUID);
        String studyInstanceUID = tuple.get(QStudy.study.studyInstanceUID);
        boolean withoutBulkData = tuple.get(QLocation.location.withoutBulkData) == null ? false
                : tuple.get(QLocation.location.withoutBulkData);
        StorageSystem storageSystem = storageSystemGroupID != null ? storageConf
                .getStorageSystem(storageSystemGroupID, storageSystemID) : null;
        ArchiveInstanceLocator newLocator = new ArchiveInstanceLocator.Builder(cuid, iuid, tsuid)
                .storageSystem(storageSystem)
                .storagePath(storagePath)
                .entryName(entryName)
                .fileTimeZoneID(timeZone)
                .retrieveAETs(retrieveAETs)
                .withoutBulkdata(withoutBulkData)
                .seriesInstanceUID(seriesInstanceUID)
                .studyInstanceUID(studyInstanceUID)
                .externalLocators(new ArrayList<ExternalLocationTuple>())
                .build();
        if (locator == null) {
            byte[] encodedInstanceAttrs =
                    tuple.get(QueryBuilder.instanceAttributesBlob.encodedAttributes);
            Attributes instanceAttrs = Utils.decodeAttributes(encodedInstanceAttrs);
            newLocator.setObject(Utils.mergeAndNormalize(seriesAttrs, instanceAttrs));
            return newLocator;
        }
        newLocator.setObject(locator.getObject());
        return updateFallbackLocator(locator, newLocator);
    }

    private static ArchiveInstanceLocator updateFallbackLocator(
            ArchiveInstanceLocator locator, ArchiveInstanceLocator newLocator) {
        if (locator == null || newLocator.compareTo(locator) < 0) {
            newLocator.setFallbackLocator(locator);
            return newLocator;
        }

        locator.setFallbackLocator(
                updateFallbackLocator(locator.getFallbackLocator(), newLocator));
        return locator;
    }
}
