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

import java.util.Date;
import java.util.EnumSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.io.SAXTransformer.SetupTransformer;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.DateUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.entity.SeriesQueryAttributes;
import org.dcm4chee.archive.entity.StudyQueryAttributes;
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

    StatelessSession openStatelessSession() {
        return em.unwrap(Session.class).getSessionFactory()
                .openStatelessSession();
    }

    @Override
    public QueryContext createQueryContext(QueryService queryService) {
        return new QueryContextImpl(queryService);
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

    @Override
    public Query createMWLItemQuery(QueryContext ctx) {

        // The MWL item query is currently not implemented and just always returns 0 results (DCMEEREQ-359)

        return new EmptyQuery(ctx);
    }

    @Override
    public Attributes getSeriesAttributes(Long seriesPk, QueryContext context) {
        return ejb.getSeriesAttributes(seriesPk, context);
    }

    @Override
    public QueryParam getQueryParam(Object source, String sourceAET,
            ArchiveAEExtension aeExt, EnumSet<QueryOption> queryOpts,
            String[] accessControlIDs) {
        return aeExt.getQueryParam(queryOpts, accessControlIDs);
    }

    @Override
    public void initPatientIDs(QueryContext ctx) {
        IDWithIssuer pid = IDWithIssuer.pidOf(ctx.getKeys());
        ctx.setPatientIDs(pid == null 
                ? IDWithIssuer.EMPTY
                : new IDWithIssuer[] { pid });
    }

    /*
     * coerceAttributesForRequest applies a loaded XSL stylesheet on the keys
     * per request if given currently 17/4/2014 modifies date and time
     * attributes in the keys per request
     */
    @Override
    public void coerceRequestAttributes(final QueryContext context)
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
                keys.addAll(
                        SAXTransformer.transform(keys, tpl, false, false, new SetupTransformer() {
                            
                            @Override
                            public void setup(Transformer transformer) {
                                setParameters(transformer,context);
                            }
                        }));
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
    public void coerceResponseAttributes(final QueryContext context, Attributes match)
            throws DicomServiceException {
        try {
            ArchiveAEExtension arcAE = context.getArchiveAEExtension();
            Attributes attrs = match;
            Templates tpl = arcAE.getAttributeCoercionTemplates(
                    context.getServiceSOPClassUID(),
                    Dimse.C_FIND_RSP,
                    TransferCapability.Role.SCP,
                    context.getRemoteAET());
            if (tpl != null) {
                attrs.addAll(SAXTransformer.transform(attrs, tpl, false, false, new SetupTransformer() {
                    
                    @Override
                    public void setup(Transformer transformer) {
                            setParameters(transformer,context);
                    }
                }));
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
       //time zone support moved to decorator
    }

    private void setParameters(Transformer transformer, QueryContext context) {
        Date date = new Date();
        String currentDate = DateUtils.formatDA(null, date);
        String currentTime = DateUtils.formatTM(null, date);
        transformer.setParameter("date", currentDate);
        transformer.setParameter("time", currentTime);
        transformer.setParameter("calling", context.getRemoteAET());
        transformer.setParameter("called", context.getArchiveAEExtension().getApplicationEntity().getAETitle());
    }

    @Override
    public StudyQueryAttributes createStudyView(Long studyPk, QueryParam queryParam) {
        if (queryParam == null || queryParam.getQueryRetrieveView() == null) throw new IllegalArgumentException("Cannot create study view - queryParam/queryRetrieveView cannot be null");
        return ejb.reCalculateStudyQueryAttributes(studyPk, queryParam);
    }

    @Override
    public SeriesQueryAttributes createSeriesView(Long seriesPk, QueryParam queryParam) {
        if (queryParam == null || queryParam.getQueryRetrieveView() == null) throw new IllegalArgumentException("Cannot create series view - queryParam/queryRetrieveView cannot be null");
        return ejb.reCalculateSeriesQueryAttributes(seriesPk, queryParam);
    }

}
