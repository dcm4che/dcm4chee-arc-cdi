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

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
package org.dcm4chee.archive.api.impl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.QueryOption;
import org.dcm4chee.archive.api.ExportDicom;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ExportDicom}.
 * 
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@EJB(name = ExportDicom.JNDI_NAME, beanInterface = ExportDicom.class)
@Stateless
public class ExportDicomImpl implements ExportDicom {

    private static final Logger LOG = LoggerFactory.getLogger(ExportDicomImpl.class);

    @PersistenceContext(name = "dcm4chee-arc")
    EntityManager em;

    @Inject
    private RetrieveService retrieveService;

    @Inject
    private CStoreSCUService cstoreSCUService;

    @Inject
    private Device device;

    @Inject
    private IApplicationEntityCache aeCache;

    @Override
    public void exportStudy(String destinationAETitle, List<String> studyIuids) {

        CStoreSCUContext ctx = initializeContext(destinationAETitle);
        if(ctx !=null)
        cstoreSCUService.scheduleStoreSCU(UUID.randomUUID().toString(), ctx,
                toInstanceLocators(studyIuids, null, ctx.getLocalAE()), 1, 1, 0);
    }

    @Override
    public void exportInstances(String destinationAETitle, List<String> instanceUids) {
        CStoreSCUContext ctx = initializeContext(destinationAETitle);
        if(ctx !=null)
        cstoreSCUService.scheduleStoreSCU(UUID.randomUUID().toString(), ctx,
                toInstanceLocators(null, instanceUids, ctx.getLocalAE()), 1, 1, 0);

    }

    @Override
    public void exportKeyImages(String destinationAETitle, List<String> studyUIDs,
            List<String> keyObjectDocumentTitles) {
        ArrayList<ArchiveInstanceLocator> referencedInstances = new ArrayList<ArchiveInstanceLocator>();
        CStoreSCUContext ctx = initializeContext(destinationAETitle);
        if(ctx ==null) {
            LOG.error("Error initializing cstore context for export to AE {}", destinationAETitle);
            return;
        }
        
        ArrayList<ArchiveInstanceLocator> locators = (ArrayList<ArchiveInstanceLocator>)
                toInstanceLocators(studyUIDs, null, ctx.getLocalAE());
        for(ArchiveInstanceLocator locator : locators) {
            Attributes objectAttrs = (Attributes) locator.getObject();
            if(objectAttrs != null) {
                Sequence conceptNameCodeSequence = objectAttrs
                        .getSequence(Tag.ConceptNameCodeSequence);
                Attributes titleItem = conceptNameCodeSequence.get(1);
                String title = titleItem.getString(Tag.CodeMeaning);
                if (keyObjectDocumentTitles.contains(title)) {
                    Sequence crpEvidenceSequence = objectAttrs
                            .getSequence(Tag.CurrentRequestedProcedureEvidenceSequence);
                    for (Iterator<Attributes> iter = crpEvidenceSequence
                            .iterator(); iter.hasNext();) {
                       ArrayList<String> iuids = getReferencedInstanceUIDs(iter.next());
                       referencedInstances.addAll(toInstanceLocators(null, iuids, ctx.getLocalAE()));
                    }
                }
            }
            else {
                LOG.error("Error reading blob attributes for object {}, "
                        + "No Export will take place", locator.iuid);
                return;
            }
        }
        cstoreSCUService.scheduleStoreSCU(UUID.randomUUID().toString(), ctx,
                referencedInstances, 1, 1, 0);
    }

    private ArrayList<String> getReferencedInstanceUIDs(Attributes item) {
        ArrayList<String> referencedSopUIDs = new ArrayList<String>();
        Sequence seriesSequence = item.getSequence(Tag.ReferencedSeriesSequence);
        for(Iterator<Attributes> iter = seriesSequence.iterator(); iter.hasNext();) {
            Attributes seriesSeqItems = iter.next();
            Sequence sopSequence = seriesSeqItems.getSequence(Tag.ReferencedSOPSequence);
            for(Iterator<Attributes> iterSops = sopSequence.iterator(); iterSops.hasNext();) {
                referencedSopUIDs.add(iterSops.next().getString(Tag.ReferencedSOPInstanceUID));
            }
        }
        return referencedSopUIDs;
    }

    private CStoreSCUContext initializeContext(String destinationAETitle) {
        ApplicationEntity remoteAE = null;
        try {
            remoteAE = aeCache.findApplicationEntity(destinationAETitle);
        } catch (ConfigurationException e) {
            LOG.error("Error looking up {} AE from configuration ",
                    destinationAETitle);
        }
        ApplicationEntity localAE = pickSuitableLocalAE();
        if (remoteAE == null || localAE == null) {
            LOG.error(
                    "Error retrieveing localAE or destination {} AE from configuration ",
                    destinationAETitle);
            return null;
        }

       return new CStoreSCUContext(pickSuitableLocalAE(),
                remoteAE, ServiceType.DICOMEXPORT);
    }

    private List<ArchiveInstanceLocator> toInstanceLocators(
            List<String> studyUIDs, List<String> iuids, 
            ApplicationEntity localAE) {
        
        ArchiveAEExtension arcAE = localAE
                .getAEExtension(ArchiveAEExtension.class);
        ArrayList<ArchiveInstanceLocator> matches = new ArrayList<>();
        QueryParam queryParam = arcAE.getQueryParam(
                EnumSet.noneOf(QueryOption.class), new String[0]);
        if(studyUIDs != null)
        for (String studyUID : studyUIDs) {
            matches.addAll(retrieveService.calculateMatches(studyUID, null,
                    null, queryParam, false));
        }
        else
            for (String iuid : iuids) {
                matches.addAll(retrieveService.calculateMatches(null, null,
                        iuid, queryParam, false));
            }
        return matches;
    }

    private ApplicationEntity pickSuitableLocalAE() {
        ArchiveDeviceExtension arcDevExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        ApplicationEntity localAE = null;
        try {
            localAE = aeCache.findApplicationEntity(arcDevExt
                    .getDefaultAETitle());
        } catch (ConfigurationException e) {
            LOG.error(
                    "Default AE for device {} can not be loaded from configuration",
                    device);
        }
        return localAE;
    }

}
