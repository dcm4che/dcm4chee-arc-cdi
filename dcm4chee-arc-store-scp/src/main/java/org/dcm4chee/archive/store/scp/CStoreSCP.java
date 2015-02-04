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

package org.dcm4chee.archive.store.scp;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomService;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.dto.LocalAssociationParticipant;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@ApplicationScoped
@Typed(DicomService.class)
public class CStoreSCP extends BasicCStoreSCP {

    @Inject
    private StoreService storeService;

    @Inject
    private IApplicationEntityCache aeCache;
    
    @Override
    protected void store(Association as, PresentationContext pc, Attributes rq,
            PDVInputStream data, Attributes rsp) throws IOException {

        try {
            StoreSession session =
                    (StoreSession) as.getProperty(StoreSession.class.getName());
            if (session == null) {
                ArchiveAEExtension arcAE = as.getApplicationEntity()
                        .getAEExtension(ArchiveAEExtension.class);
                session = storeService.createStoreSession(storeService);
                session.setSource(new LocalAssociationParticipant(as));
                session.setRemoteAET(as.getRemoteAET());
                session.setSourceDevice(aeCache.findApplicationEntity(as.getRemoteAET()).getDevice());
                session.setArchiveAEExtension(arcAE);
                storeService.initStorageSystem(session);
                storeService.initSpoolDirectory(session);
                as.setProperty(StoreSession.class.getName(), session);
            }
            Attributes fmi = as.createFileMetaInformation(
                  rq.getString(Tag.AffectedSOPInstanceUID),
                  rq.getString(Tag.AffectedSOPClassUID),
                  pc.getTransferSyntax());
            StoreContext context = storeService.createStoreContext(session);
            storeService.writeSpoolFile(context, fmi, data);
            storeService.parseSpoolFile(context);
            storeService.store(context);
            Attributes coercedAttrs = context.getCoercedOriginalAttributes();
            if (!coercedAttrs.isEmpty() 
                    && !session.getArchiveAEExtension()
                        .isSuppressWarningCoercionOfDataElements()) {
                rsp.setInt(Tag.Status, VR.US, Status.CoercionOfDataElements);
                rsp.setInt(Tag.OffendingElement, VR.AT, coercedAttrs.tags());
            }
        } catch (DicomServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }

    @Override
    public void onClose(Association as) {
        StoreSession session = as.getProperty(StoreSession.class);
        if (session != null)
            storeService.onClose(session);
    }

}
