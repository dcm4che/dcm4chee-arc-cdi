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
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.transform.Templates;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.InstanceLocator;
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

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    @Inject
    private IApplicationEntityCache aeCache;

    @Override
    public RetrieveContext createRetrieveContext(RetrieveService service,
            String sourceAET, ArchiveAEExtension arcAE) {
        return new RetrieveContextImpl(service, sourceAET, arcAE);
    }

    @Override
    public IDWithIssuer[] queryPatientIDs(RetrieveContext context,
            Attributes keys) {
        IDWithIssuer pid = IDWithIssuer.fromPatientIDWithIssuer(keys);
        return pid == null ? IDWithIssuer.EMPTY : new IDWithIssuer[] { pid };
    }

    @Override
    public List<ArchiveInstanceLocator> calculateMatches(IDWithIssuer[] pids,
            Attributes keys, QueryParam queryParam) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QueryBuilder.pids(pids, false));
        builder.and(QueryBuilder.uids(QStudy.study.studyInstanceUID,
                keys.getStrings(Tag.StudyInstanceUID), false));
        builder.and(QueryBuilder.uids(QSeries.series.seriesInstanceUID,
                keys.getStrings(Tag.SeriesInstanceUID), false));
        builder.and(QueryBuilder.uids(QInstance.instance.sopInstanceUID,
                keys.getStrings(Tag.SOPInstanceUID), false));

        builder.and(QInstance.instance.replaced.isFalse());
        builder.and(QueryBuilder.hideRejectedInstance(queryParam));
        builder.and(QueryBuilder.hideRejectionNotes(queryParam));
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
        builder.and(QueryBuilder.hideRejectionNotes(queryParam));
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
                    byte[] instAttrs = tuple.get(9, byte[].class);
                    attrs = new Attributes(seriesAttrs);
                    Utils.decodeAttributes(attrs, instAttrs);
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

    @Override
    public void coerceRetrievedObject(RetrieveContext retrieveContext,
            String remoteAET, Attributes attrs) throws DicomServiceException {
        ArchiveAEExtension aeExt = retrieveContext.getArchiveAEExtension();
        ApplicationEntity sourceAE = null;
        try {
            Templates tpl = aeExt.getAttributeCoercionTemplates(
                    attrs.getString(Tag.SOPClassUID), Dimse.C_STORE_RQ,
                    Role.SCU, remoteAET);
            if (tpl != null)
                attrs.update(
                        SAXTransformer.transform(attrs, tpl, false, false),
                        null);

            try {
                sourceAE = aeCache.findApplicationEntity(remoteAET);
            } catch (ConfigurationException e1) {
                e1.printStackTrace();
            }
            TimeZone archiveTimeZone = aeExt.getApplicationEntity().getDevice()
                    .getTimeZoneOfDevice();
            TimeZone sourceTimeZone = sourceAE.getDevice()
                    .getTimeZoneOfDevice();
            if (sourceTimeZone != null) {
                attrs.setDefaultTimeZone(archiveTimeZone);
                attrs.setTimezone(sourceTimeZone);
                // in device time - add tag with offset from device
                int offsetFromUTC = sourceTimeZone.getRawOffset();
                if (attrs.contains(Tag.StudyDate)) {
                    offsetFromUTC = sourceTimeZone.getOffset(attrs.getDate(
                            Tag.StudyDate).getTime());
                }
                else if(attrs.contains(Tag.ContentDate))
                {
                    offsetFromUTC = archiveTimeZone.getOffset(attrs
                            .getDate(Tag.ContentDate).getTime());
                }
                String offsetString = timeOffsetInMillisToDICOMTimeOffset(offsetFromUTC);
                attrs.setString(Tag.TimezoneOffsetFromUTC, VR.SH, ""
                        + offsetString);
            } else {
                // in archive time - add tag with offset from archive
                int offsetFromUTC = archiveTimeZone.getRawOffset();
                if (attrs.contains(Tag.StudyDate)) {
                    offsetFromUTC = archiveTimeZone.getOffset(attrs.getDate(
                            Tag.StudyDate).getTime());
                }
                else if(attrs.contains(Tag.ContentDate))
                {
                    offsetFromUTC = archiveTimeZone.getOffset(attrs
                            .getDate(Tag.ContentDate).getTime());
                }
                String offsetString = timeOffsetInMillisToDICOMTimeOffset(offsetFromUTC);
                attrs.setString(Tag.TimezoneOffsetFromUTC, VR.SH, ""
                        + offsetString);
            }

        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    @Override
    public void coerceFileBeforeMerge(ArchiveInstanceLocator inst,
            RetrieveContext retrieveContext, String remoteAET, Attributes attrs)
            throws DicomServiceException {
        // here the source time zone is the one in the db
        try {

            ArchiveAEExtension arcAE = retrieveContext.getArchiveAEExtension();
            TimeZone archiveTimeZone = arcAE.getApplicationEntity().getDevice()
                    .getTimeZoneOfDevice();
            TimeZone sourceTimeZone = TimeZone.getTimeZone(inst
                    .getFileTimeZoneID());
            if (sourceTimeZone != null) {
                attrs.setDefaultTimeZone(sourceTimeZone);
                attrs.setTimezone(archiveTimeZone);
            }

        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    public String timeOffsetInMillisToDICOMTimeOffset(int millis) {
        int mns = millis / (1000 * 60);
        String h = "" + (int) mns / 60;
        if (h.length() == 1) {
            String tmp = h;
            h = "0" + tmp;
        }
        String m = "" + (int) (mns % 60);
        if (m.length() == 1) {
            String tmp = m;
            m = "0" + tmp;
        }
        String sign = (int) Math.signum(mns) > 0 ? "+" : "-";
        return sign + h + m;
    }
}
