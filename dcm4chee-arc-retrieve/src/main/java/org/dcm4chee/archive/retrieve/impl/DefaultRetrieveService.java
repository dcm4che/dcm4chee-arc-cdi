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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.io.SAXWriter;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.SAXTransformer.SetupTransformer;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.entity.PatientStudySeriesAttributes;
import org.dcm4chee.archive.entity.QFileRef;
import org.dcm4chee.archive.entity.QFileSystem;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.QPatient;
import org.dcm4chee.archive.entity.QSeries;
import org.dcm4chee.archive.entity.QStudy;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.query.util.QueryBuilder;
import org.dcm4chee.archive.retrieve.RetrieveContext;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.hibernate.Session;
import org.jboss.logging.Logger;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.hibernate.HibernateQuery;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@ApplicationScoped
public class DefaultRetrieveService implements RetrieveService {

    private static final Logger LOG = Logger.getLogger(DefaultRetrieveService.class);

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    @Override
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

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QueryBuilder.pids(pids, 
                queryParam.isMatchLinkedPatientIDs(), false));
        builder.and(QueryBuilder.uids(QStudy.study.studyInstanceUID,
                keys.getStrings(Tag.StudyInstanceUID), false));
        builder.and(QueryBuilder.uids(QSeries.series.seriesInstanceUID,
                keys.getStrings(Tag.SeriesInstanceUID), false));
        builder.and(QueryBuilder.uids(QInstance.instance.sopInstanceUID,
                keys.getStrings(Tag.SOPInstanceUID), false));

        builder.and(QInstance.instance.replaced.isFalse());
        builder.and(QueryBuilder.hideRejectedInstance(queryParam));
        return query(builder);
    }

    /**
     * Given study and/or series and/or object uids, performs the query and
     * returns references to the instances.
     */
    public List<ArchiveInstanceLocator> calculateMatches(String studyUID,
            String seriesUID, String objectUID, QueryParam queryParam) {

        BooleanBuilder builder = new BooleanBuilder();

        if (studyUID != null)
            builder.and(QueryBuilder.uids(QStudy.study.studyInstanceUID,
                    new String[] { studyUID }, false));

        if (seriesUID != null)
            builder.and(QueryBuilder.uids(QSeries.series.seriesInstanceUID,
                    new String[] { seriesUID }, false));

        if (objectUID != null)
            builder.and(QueryBuilder.uids(QInstance.instance.sopInstanceUID,
                    new String[] { objectUID }, false));

        builder.and(QInstance.instance.replaced.isFalse());
        builder.and(QueryBuilder.hideRejectedInstance(queryParam));
        return query(builder);
    }

    /**
     * Executes the query.
     */
    private List<ArchiveInstanceLocator> query(BooleanBuilder builder) {
        List<Tuple> query = new HibernateQuery(em.unwrap(Session.class))
                .from(QInstance.instance)
                .leftJoin(QInstance.instance.fileRefs, QFileRef.fileRef)
                .leftJoin(QFileRef.fileRef.fileSystem, QFileSystem.fileSystem)
                .innerJoin(QInstance.instance.series, QSeries.series)
                .innerJoin(QSeries.series.study, QStudy.study)
                .innerJoin(QStudy.study.patient, QPatient.patient)
                .where(builder)
                .list(QFileRef.fileRef.transferSyntaxUID,
                        QFileRef.fileRef.filePath, QFileSystem.fileSystem.uri,
                        QSeries.series.pk, QInstance.instance.pk,
                        QInstance.instance.sopClassUID,
                        QInstance.instance.sopInstanceUID,
                        QInstance.instance.retrieveAETs,
                        QInstance.instance.externalRetrieveAET,
                        QInstance.instance.encodedAttributes,
                        QFileRef.fileRef.fileTimeZone);

        return locate(query);
    }

    /**
     * Given the query result, constructs response.
     */
    private List<ArchiveInstanceLocator> locate(List<Tuple> tuples) {
        List<ArchiveInstanceLocator> locators = new ArrayList<ArchiveInstanceLocator>(
                tuples.size());
        long instPk = -1;
        long seriesPk = -1;
        Attributes seriesAttrs = null;
        for (Tuple tuple : tuples) {
            String tsuid = tuple.get(0, String.class);
            String filePath = tuple.get(1, String.class);
            String fsuri = tuple.get(2, String.class);
            String ftz = tuple.get(10, String.class);
            long nextSeriesPk = tuple.get(3, long.class);
            long nextInstPk = tuple.get(4, long.class);
            if (seriesPk != nextSeriesPk) {
                seriesAttrs = getSeriesAttributes(nextSeriesPk);
                seriesPk = nextSeriesPk;
            }
            if (instPk != nextInstPk) {
                String cuid = tuple.get(5, String.class);
                String iuid = tuple.get(6, String.class);
                String retrieveAETs = tuple.get(7, String.class);
                String externalRetrieveAET = tuple.get(8, String.class);
                String uri;
                Attributes attrs;
                if (fsuri != null) {
                    uri = fsuri + '/' + filePath;
                    byte[] instByteAttrs = tuple.get(9, byte[].class);
                    Attributes instanceAttrs = new Attributes();
                    Utils.decodeAttributes(instanceAttrs, instByteAttrs);
                    attrs = Utils.mergeAndNormalize(seriesAttrs, instanceAttrs);
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("aet:");
                    if (retrieveAETs != null) {
                        sb.append(retrieveAETs);
                    }
                    if (externalRetrieveAET != null) {
                        if (retrieveAETs != null)
                            sb.append('\\');
                        sb.append(externalRetrieveAET);
                    }
                    uri = sb.toString();
                    attrs = null;
                }
                locators.add((ArchiveInstanceLocator) new ArchiveInstanceLocator(
                        cuid, iuid, tsuid, uri, ftz).setObject(attrs));
                instPk = nextInstPk;
            }
        }
        return locators;
    }

    private Attributes getSeriesAttributes(Long seriesPk) {
        PatientStudySeriesAttributes result = (PatientStudySeriesAttributes) em
                .createNamedQuery(Series.PATIENT_STUDY_SERIES_ATTRIBUTES)
                .setParameter(1, seriesPk).getSingleResult();
        return result.getAttributes();
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

    public List<ArchiveInstanceLocator> applySuppressionCriteria(
            List<ArchiveInstanceLocator> refs,
            String supressionCriteriaTemplateURI,
            List<ArchiveInstanceLocator> instsfailed,
            final RetrieveContext retrieveContext) {

        List<ArchiveInstanceLocator> adjustedRefs = new ArrayList<ArchiveInstanceLocator>();

        for (ArchiveInstanceLocator ref : refs) {
            Attributes attrs = getFileAttributes(ref);
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
                        adjustedRefs.add(ref);
                    } else {
                        instsfailed.add(ref);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Applying Suppression Criteria on WADO request, using template: "
                                    + StringUtils
                                    .replaceSystemProperties(supressionCriteriaTemplateURI)
                                    + "\nRemoving Referenced Instance: "
                                    + ref.iuid + " from response");
                        }
                    }
                }

            } catch (Exception e) {
                LOG.error("Error applying supression criteria, {}", e);
            }
        }
        return adjustedRefs;
    }

    public List<ArchiveInstanceLocator> eliminateUnSupportedSOPClasses(
            List<ArchiveInstanceLocator> refs,
            List<ArchiveInstanceLocator> instsfailed,
            RetrieveContext retrieveContext)
            throws ConfigurationNotFoundException {

        List<ArchiveInstanceLocator> adjustedRefs = new ArrayList<ArchiveInstanceLocator>();

        try {
            // here in wado source and destination are the same
            ArrayList<TransferCapability> aeTCs = new ArrayList<TransferCapability>(
                    retrieveContext.getDestinationAE().getTransferCapabilitiesWithRole(
                            Role.SCU));
            for (ArchiveInstanceLocator ref : refs) {
                for (TransferCapability supportedTC : aeTCs)
                    if (supportedTC.getSopClass().compareTo(ref.cuid) == 0) {
                        if (supportedTC.containsTransferSyntax(ref.tsuid)) {
                            adjustedRefs.add(ref);
                        }
                    }
                if (!adjustedRefs.contains(ref)) {
                    instsfailed.add(ref);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Applying UnSupported SOP Class Elimination on WADO request"
                                + "\nRemoving Referenced Instance: "
                                + ref.iuid
                                + " from response");
                    }
                }
            }

            return adjustedRefs;
        } catch (Exception e) {
            LOG.error("Exception while applying elimination, {}", e);
            return refs;
        }
    }

    private Attributes getFileAttributes(InstanceLocator ref) {
        DicomInputStream dis = null;
        try {
            dis = new DicomInputStream(ref.getFile());
            dis.setIncludeBulkData(IncludeBulkData.URI);
            Attributes dataset = dis.readDataset(-1, -1);
            return dataset;
        } catch (IOException e) {
            LOG.error(
                    "Unable to read file, Exception {}, using the blob for coercion - (Incomplete Coercion)",
                    e);
            return (Attributes) ref.getObject();
        } finally {
            SafeClose.close(dis);
        }
    }
}
