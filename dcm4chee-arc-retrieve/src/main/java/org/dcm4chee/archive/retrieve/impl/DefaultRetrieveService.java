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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.io.SAXTransformer.SetupTransformer;
import org.dcm4che3.io.SAXWriter;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.entity.QLocation;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.QSeries;
import org.dcm4chee.archive.query.util.QueryBuilder;
import org.dcm4chee.archive.retrieve.RetrieveContext;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
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
        QSeries.series.pk,
        QInstance.instance.pk,
        QInstance.instance.sopClassUID,
        QInstance.instance.sopInstanceUID,
        QInstance.instance.retrieveAETs,
        QInstance.instance.externalRetrieveAET,
        QueryBuilder.instanceAttributesBlob.encodedAttributes
    };

    @Inject
    private Device device;

    @Inject
    private RetrieveServiceEJB ejb;

    @Inject
    private org.dcm4chee.storage.service.RetrieveService storageRetrieveService;
    
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
            Attributes keys, QueryParam queryParam) {

        return locate(ejb.query(SELECT,
                pids,
                keys.getStrings(Tag.StudyInstanceUID),
                keys.getStrings(Tag.SeriesInstanceUID),
                keys.getStrings(Tag.SOPInstanceUID), queryParam));
    }

    /**
     * Given study and/or series and/or object uids, performs the query and
     * returns references to the instances.
     */
    @Override
    public List<ArchiveInstanceLocator> calculateMatches(String studyIUID,
            String seriesIUID, String objectIUID, QueryParam queryParam) {

        return locate(ejb.query(SELECT,
                null,
                studyIUID  == null ? null: new String[] { studyIUID }
              , seriesIUID == null ? null: new String[] { seriesIUID }
              , objectIUID == null ? null: new String[] { objectIUID }, queryParam));
    }

    private List<ArchiveInstanceLocator> locate(List<Tuple> tuples) {

        List<ArchiveInstanceLocator> locators =
                new ArrayList<ArchiveInstanceLocator>(tuples.size());
        long instPk = -1;
        long seriesPk = -1;
        Attributes seriesAttrs = null;
        ArchiveInstanceLocatorBuilder builder = null;

        for (Tuple tuple : tuples) {
            long nextSeriesPk = tuple.get(QSeries.series.pk);
            long nextInstPk = tuple.get(QInstance.instance.pk);

            if (seriesPk != nextSeriesPk) {
                seriesAttrs = ejb.getSeriesAttributes(nextSeriesPk);
                seriesPk = nextSeriesPk;
            }
            if (instPk != nextInstPk) {
                if (builder != null)
                    locators.add(builder.build());
                builder = new ArchiveInstanceLocatorBuilder(
                        device.getDeviceExtensionNotNull(StorageDeviceExtension.class),
                        tuple, seriesAttrs);
            }
            
            builder.addFileRefs(tuple);
            instPk = nextInstPk;
        }
        if (builder != null)
            locators.add(builder.build());
        return locators;
    }

    private void setParameters(Transformer tr, RetrieveContext retrieveContext) {
        Date date = new Date();
        String currentDate = DateUtils.formatDA(null, date);
        String currentTime = DateUtils.formatTM(null, date);
        tr.setParameter("date", currentDate);
        tr.setParameter("time", currentTime);
        tr.setParameter("calling", retrieveContext.getSourceAET());
        tr.setParameter("called", retrieveContext.getDestinationAE()
                .getAETitle());
    }

    @Override
    public void coerceRetrievedObject(final RetrieveContext retrieveContext,
            String remoteAET, Attributes attrs) throws DicomServiceException {
        ArchiveAEExtension aeExt = retrieveContext.getArchiveAEExtension();
        
        
        try {
            Templates tpl = aeExt.getAttributeCoercionTemplates(
                    attrs.getString(Tag.SOPClassUID), Dimse.C_STORE_RQ,
                    Role.SCU, remoteAET);
            if (tpl != null)
                attrs.addAll(
                        SAXTransformer.transform(attrs, tpl, false, false, new SetupTransformer() {
                            
                            @Override
                            public void setup(Transformer transformer) {
                                setParameters(transformer, retrieveContext);
                            }
                        }));

        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    @Override
    public void coerceFileBeforeMerge(ArchiveInstanceLocator inst,
            RetrieveContext retrieveContext, String remoteAET, Attributes attrs)
            throws DicomServiceException {
    }

    //supression criteria and elimination of objects by sop class and transfer syntax methods (Can be decorated)
    //sop class and transfer syntax filter

    public ArchiveInstanceLocator applySuppressionCriteria(
            ArchiveInstanceLocator ref,
            Attributes attrs,
            String supressionCriteriaTemplateURI,
            final RetrieveContext retrieveContext) {

            try {
                Templates tpl = SAXTransformer
                        .newTemplates(new StreamSource(
                                StringUtils
                                        .replaceSystemProperties(supressionCriteriaTemplateURI)));
                if (tpl != null) {
                    boolean eliminate;
                    StringWriter resultWriter = new StringWriter();
                    SAXWriter wr = SAXTransformer.getSAXWriter(tpl,
                            new StreamResult(resultWriter), new SetupTransformer() {
                                
                                @Override
                                public void setup(Transformer transformer) {
                                    setParameters(transformer, retrieveContext);
                                }
                            });
                    wr.write(attrs);
                    eliminate = (resultWriter.toString().compareToIgnoreCase(
                            "true") == 0 ? true : false);
                    if (!eliminate) {
                        return ref;
                    } 

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Applying Suppression Criteria on retrieve , using template: "
                                    + StringUtils
                                    .replaceSystemProperties(supressionCriteriaTemplateURI)
                                    + "\nRemoving Referenced Instance: "
                                    + ref.iuid + " from response");
                        }
                        return null;
                }

            } catch (Exception e) {
                LOG.error("Error applying supression criteria, {}", e);
                return ref;
            }
            return ref;
    }

    public ArchiveInstanceLocator eliminateUnSupportedSOPClasses(
            ArchiveInstanceLocator ref,
            RetrieveContext retrieveContext) {
        if(retrieveContext.getDestinationAE()!=null)
        try {
            // here in wado source and destination are the same
            ArrayList<TransferCapability> aeTCs = new ArrayList<TransferCapability>(
                    retrieveContext.getDestinationAE().getTransferCapabilitiesWithRole(
                            Role.SCU));

                for (TransferCapability supportedTC : aeTCs){
                    if (supportedTC.getSopClass().compareTo(ref.cuid) == 0) {
                        return ref;
                    }
                }

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Applying UnSupported SOP Class Elimination on retrieve"
                                + "\nRemoving Referenced Instance: "
                                + ref.iuid
                                + " from response");
                    }
                    return null;
        } catch (Exception e) {
            LOG.error("Exception while applying elimination, {}", e);
            return ref;
        }
            return ref;
    }

    @Override
    public Path getFile(ArchiveInstanceLocator inst) throws IOException {
        org.dcm4chee.storage.RetrieveContext ctx =
                storageRetrieveService.createRetrieveContext(inst.getStorageSystem());
        try {
			return inst.getEntryName() == null
			        ? storageRetrieveService.getFile(ctx, inst.getFilePath())
			        : storageRetrieveService.getFile(ctx, inst.getFilePath(), inst.getEntryName());
		} catch (InterruptedException e) {
			throw new InterruptedIOException();
		}
    }
}
