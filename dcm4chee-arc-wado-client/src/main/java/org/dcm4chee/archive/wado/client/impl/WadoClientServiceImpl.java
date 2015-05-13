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

package org.dcm4chee.archive.wado.client.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.web.WebServiceAEExtension;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.StoreAction;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.GenericParticipant;
import org.dcm4chee.archive.dto.LocalAssociationParticipant;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.archive.wado.client.InstanceAvailableCallback;
import org.dcm4chee.archive.wado.client.WadoClient;
import org.dcm4chee.archive.wado.client.WadoClientResponse;
import org.dcm4chee.archive.wado.client.WadoClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
public class WadoClientServiceImpl implements WadoClientService {

    private static final Logger LOG = LoggerFactory.getLogger(
            WadoClientServiceImpl.class);

    @Inject
    private StoreService storeService;

    private InstanceAvailableCallback callBack;

    @Inject
    private IApplicationEntityCache aeCache;

    @Override
    public WadoClientResponse fetchStudy(ApplicationEntity localAE,
            ApplicationEntity remoteAE, String studyInstanceUID,
            InstanceAvailableCallback callBack) {
        return fetch(localAE, remoteAE, studyInstanceUID, null, null, callBack);
    }

    @Override
    public WadoClientResponse fetchSeries(ApplicationEntity localAE,
            ApplicationEntity remoteAE, String studyInstanceUID,
            String seriesInstanceUID,
            InstanceAvailableCallback callback) {
        return fetch(localAE, remoteAE, studyInstanceUID, seriesInstanceUID,
                null, callback);
    }

    @Override
    public WadoClientResponse fetchInstance(ApplicationEntity localAE,
            ApplicationEntity remoteAE, String studyInstanceUID,
            String seriesInstanceUID, String sopInstanceUID, InstanceAvailableCallback callback) {
        return fetch(localAE, remoteAE, studyInstanceUID, seriesInstanceUID,
                sopInstanceUID,  callback);
    }

    @Override
    public boolean store(StoreContext context) {
        context.setFetch(true);
        try {
            storeService.parseSpoolFile(context);
            try {
                storeService.storeMetaData(context);
                storeService.processFile(context);
                storeService.updateDB(context);
            } catch (DicomServiceException e) {
                context.setStoreAction(StoreAction.FAIL);
                context.setThrowable(e);
                throw e;
            } finally {
                storeService.cleanup(context);
            }
            LOG.debug("Fetched and Stored instance from remote AE {}"
                    , context.getStoreSession().getRemoteAET());
            getCallBack().onInstanceAvailable(createArchiveInstanceLocator(context));
            return true;
        } catch (Exception x) {
            LOG.error("Failed to store RejectionNote!", x);
            return false;
        }
}
    @Override
    public StoreContext spool(String localAETitle, String remoteAETitle,
            InputStream in, InstanceAvailableCallback callback) throws Exception {
        StoreContext context;
        ApplicationEntity localAE = aeCache.findApplicationEntity(localAETitle);
            StoreSession session = storeService.createStoreSession(storeService); 
        session.setSource(new GenericParticipant(localAE.getConnections()
                .get(0).getHostname(), "WadoRS Fetch"));
            session.setRemoteAET(remoteAETitle);
            ArchiveAEExtension arcAEExt = aeCache.get(localAETitle)
                    .getAEExtension(ArchiveAEExtension.class); 
            session.setArchiveAEExtension(arcAEExt);
            storeService.initStorageSystem(session);
            storeService.initSpoolDirectory(session);
            context = storeService.createStoreContext(session);
            try {
            DicomInputStream din = new DicomInputStream(in);
            Attributes fmi = din.getFileMetaInformation();
            storeService.writeSpoolFile(context, fmi, din);
        }
        catch (Exception e) {
            throw new Exception("Failed to spool WadoRS response from AE "+
                    remoteAETitle);
        }
        return context;
}

    @Override
    public InstanceAvailableCallback getCallBack() {
        return callBack;
    }

    protected void setCallBack(InstanceAvailableCallback callBack) {
        this.callBack = callBack;
    }

    private WadoClient createClient() {
        return new WadoClient(this);
    }


    private ArchiveInstanceLocator createArchiveInstanceLocator(
            StoreContext context) {
        ArchiveInstanceLocator newLocator = new ArchiveInstanceLocator.Builder(
                context.getInstance().getSopClassUID(), 
                context.getInstance().getSopInstanceUID(),
                context.getFileRef().getTransferSyntaxUID())
        .storageSystem(context.getStoreSession().getStorageSystem())
        .storagePath(context.getStoragePath())
        .entryName(context.getFileRef().getEntryName())
        .fileTimeZoneID(context.getFileRef().getTimeZone())
        .retrieveAETs(context.getInstance().getRawRetrieveAETs())
        .withoutBulkdata(context.getFileRef().isWithoutBulkData())
        .seriesInstanceUID(context.getAttributes()
                .getString(Tag.SeriesInstanceUID))
        .studyInstanceUID(context.getAttributes()
                .getString(Tag.StudyInstanceUID))
        .build();
        return newLocator;
    }

    private WadoClientResponse fetch(ApplicationEntity localAE,
            ApplicationEntity remoteAE, String studyInstanceUID,
            String seriesInstanceUID, String sopInstanceUID, InstanceAvailableCallback callback) {
        setCallBack(callback);
        WadoClient client = createClient();
        WebServiceAEExtension wsAEExt = remoteAE
                .getAEExtension(WebServiceAEExtension.class);
        try {
            return client.fetch(localAE.getAETitle(), remoteAE.getAETitle(),
                    studyInstanceUID, seriesInstanceUID, sopInstanceUID,
                    wsAEExt.getWadoRSBaseURL());
        } catch (IOException e) {
            LOG.error("Error fetching Study {}, from AE {}"
                    + " check baseurl configuration for WadoRS",
                    studyInstanceUID, remoteAE.getAETitle());
        }
        return null;
    }

}