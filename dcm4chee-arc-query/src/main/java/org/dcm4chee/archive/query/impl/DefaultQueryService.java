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

package org.dcm4chee.archive.query.impl;

import java.util.EnumSet;
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
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.query.Query;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.query.QueryService;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@ApplicationScoped
public class DefaultQueryService implements QueryService {

    static Logger LOG = LoggerFactory.getLogger(DefaultQueryService.class);

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    @Inject
    QueryServiceEJB ejb;

    @Inject
    private IApplicationEntityCache aeCache;

    StatelessSession openStatelessSession() {
        return em.unwrap(Session.class).getSessionFactory()
                .openStatelessSession();
    }

    public QueryContext createQueryContext(QueryService queryService) {
        return new QueryContextImpl(queryService);
    }

    @Override
    public Query createQuery(QueryRetrieveLevel qrlevel, QueryContext ctx) {
        switch (qrlevel) {
        case PATIENT:
            return ctx.getQueryService().createPatientQuery(ctx);
        case STUDY:
            return ctx.getQueryService().createStudyQuery(ctx);
        case SERIES:
            return ctx.getQueryService().createSeriesQuery(ctx);
        case IMAGE:
            return ctx.getQueryService().createInstanceQuery(ctx);
        default:
            throw new IllegalArgumentException("qrlevel: " + qrlevel);
        }
    }

    @Override
    public Query createPatientQuery(QueryContext ctx) {
        return new PatientQuery(ctx, openStatelessSession());
    }

    @Override
    public Query createStudyQuery(QueryContext ctx) {
        return new StudyQuery(ctx, openStatelessSession());
    }

    @Override
    public Query createSeriesQuery(QueryContext ctx) {
        return new SeriesQuery(ctx, openStatelessSession());
    }

    @Override
    public Query createInstanceQuery(QueryContext ctx) {
        return new InstanceQuery(ctx, openStatelessSession());
    }

    public Attributes getSeriesAttributes(Long seriesPk, QueryParam queryParam) {
        return ejb.getSeriesAttributes(seriesPk, queryParam);
    }

    public int calculateNumberOfSeriesRelatedInstance(Long seriesPk,
            QueryParam queryParam) {
        return ejb.calculateNumberOfSeriesRelatedInstance(seriesPk, queryParam);
    }

    public int calculateNumberOfStudyRelatedSeries(Long studyPk,
            QueryParam queryParam) {
        return ejb.calculateNumberOfStudyRelatedSeries(studyPk, queryParam);
    }

    public int calculateNumberOfStudyRelatedInstance(Long studyPk,
            QueryParam queryParam) {
        return ejb.calculateNumberOfStudyRelatedInstance(studyPk, queryParam);
    }

    @Override
    public QueryParam getQueryParam(Object source, String sourceAET,
            ArchiveAEExtension aeExt, EnumSet<QueryOption> queryOpts,
            String[] accessControlIDs) {
        return aeExt.getQueryParam(queryOpts, accessControlIDs);
    }

    @Override
    public void initPatientIDs(QueryContext ctx) {
        IDWithIssuer pid = IDWithIssuer.fromPatientIDWithIssuer(ctx.getKeys());
        ctx.setPatientIDs(pid == null 
                ? IDWithIssuer.EMPTY
                : new IDWithIssuer[] { pid });
    }

    @Override
    public void adjustMatch(QueryContext query, Attributes match) {

    }

    /*
     * coerceAttributesForRequest applies a loaded XSL stylesheet on the keys
     * per request if given currently 17/4/2014 modifies date and time
     * attributes in the keys per request
     */
    @Override
    public void coerceRequestAttributes(QueryContext context)
            throws DicomServiceException {

        try {
            ArchiveAEExtension arcAE = context.getArchiveAEExtension();
            Attributes keys = context.getKeys();
            Templates tpl = arcAE.getAttributeCoercionTemplates(
                    context.getServiceSOPClassUID(),
                    Dimse.C_FIND_RQ,
                    TransferCapability.Role.SCP,
                    context.getRemoteAET());
            if (tpl != null) {
                keys.addAll(SAXTransformer.transform(keys, tpl, false, false));
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
        //time zone support moved to decorator

    }

    /*
     * coerceAttributesForResponse applies a loaded XSL stylesheet on the keys
     * per response if given currently 17/4/2014 modifies date and time
     * attributes in the keys per response
     */
    @Override
    public void coerceResponseAttributes(QueryContext context, Attributes match)
            throws DicomServiceException {
        try {
            ArchiveAEExtension arcAE = context.getArchiveAEExtension();
            Attributes attrs = match;
            Templates tpl = arcAE.getAttributeCoercionTemplates(
                    context.getServiceSOPClassUID(),
                    Dimse.C_FIND_RSP,
                    TransferCapability.Role.SCU,
                    context.getRemoteAET());
            if (tpl != null) {
                attrs.addAll(SAXTransformer.transform(attrs, tpl, false, false));
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
       //time zone support moved to decorator
    }

}
