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

package org.dcm4chee.archive.mima.impl;

import java.util.EnumSet;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.QueryOption;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.query.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator to apply MIMA specifications to the Query Service.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * 
 */
@Decorator
public abstract class QueryServiceMIMADecorator implements QueryService {

    private static Logger LOG = LoggerFactory
            .getLogger(QueryServiceMIMADecorator.class);

    @Inject
    @Delegate
    private QueryService queryService;

    @Inject
    private IApplicationEntityCache aeCache;

    @Inject
    private PIXConsumer pixConsumer;

    @Inject
    private MIMAAttributeCoercion coercion;

    /*
     * Extends default getQueryParam method to add to the QueryParam the
     * configured issuer of PatID and AccessionNumber associated to the query
     * source (if existing).
     */
    @Override
    public QueryParam getQueryParam(Object source, String sourceAET,
            ArchiveAEExtension aeExt, EnumSet<QueryOption> queryOpts,
            String[] accessControlIDs) {
        QueryParam queryParam = queryService.getQueryParam(source, sourceAET,
                aeExt, queryOpts, accessControlIDs);
        try {
            ApplicationEntity scrAE = aeCache.get(sourceAET);
            if (scrAE != null) {
                Device srcDev = scrAE.getDevice();
                queryParam.setDefaultIssuerOfPatientID(srcDev
                        .getIssuerOfPatientID());
                queryParam.setDefaultIssuerOfAccessionNumber(srcDev
                        .getIssuerOfAccessionNumber());
            }
        } catch (ConfigurationException e) {
            LOG.warn(
                    "Failed to access configuration for query source {} - no MIMA support:",
                    source, e);
        }
        return queryParam;
    }

    /*
     * Extends the default initPatientIDs, performing a PIX query having as
     * argument the original queried PatientID (if any). Note that if the DICOM
     * Query contains a PatientID, this will be placed in the first element of
     * the PatientIDs array in the context (ctx.getPatientIDs()[0]) .
     * Afterwards, the PatientIDs will be filled up with all the extra IDs
     * returned in the PIX response. If the queried PatientID has no issuer, the
     * issuer associate to the query source is automatically added and used for
     * the PIX query.
     */
    @Override
    public void initPatientIDs(QueryContext ctx) {
        queryService.initPatientIDs(ctx);
        IDWithIssuer[] pids = ctx.getPatientIDs();
        if (pids.length > 0) {
            if (pids[0].getIssuer() == null)
                pids[0].setIssuer(ctx.getQueryParam()
                        .getDefaultIssuerOfPatientID());
            ctx.setPatientIDs(pixConsumer.pixQuery(ctx.getArchiveAEExtension(),
                    pids[0]));
        }
    }

    /*
     * Extends the default adjustMatch method, coercing (according to MIMA
     * specs) attributes of the matched instances returned by the query.
     */
    @Override
    public void adjustMatch(QueryContext context, Attributes match) {
        MIMAInfo info = (MIMAInfo) context
                .getProperty(MIMAInfo.class.getName());
        if (info == null) {
            info = new MIMAInfo();
            init(context, info);
            context.setProperty(MIMAInfo.class.getName(), info);
        }
        coercion.coerce(context.getArchiveAEExtension(), info, match);
    }

    private void init(QueryContext context, MIMAInfo info) {
        ArchiveAEExtension arcAE = context.getArchiveAEExtension();
        Attributes keys = context.getKeys();
        QueryParam queryParam = context.getQueryParam();
        info.setReturnOtherPatientIDs(arcAE.isReturnOtherPatientIDs()
                && keys.contains(Tag.OtherPatientIDsSequence));
        info.setReturnOtherPatientNames(arcAE.isReturnOtherPatientNames()
                && keys.contains(Tag.OtherPatientNames));
        info.setRequestedIssuerOfPatientID(keys.contains(Tag.PatientID) ? keys
                .contains(Tag.IssuerOfPatientID) ? Issuer
                .fromIssuerOfPatientID(keys) : queryParam
                .getDefaultIssuerOfPatientID() : null);
        info.setRequestedIssuerOfAccessionNumber((keys
                .contains(Tag.AccessionNumber) || keys
                .contains(Tag.RequestAttributesSequence)) ? keys
                .contains(Tag.IssuerOfAccessionNumberSequence) ? Issuer
                .valueOf(keys
                        .getNestedDataset(Tag.IssuerOfAccessionNumberSequence))
                : queryParam.getDefaultIssuerOfAccessionNumber() : null);
        IDWithIssuer[] pids = context.getPatientIDs();
        if (pixQueryAlreadyPerformed(pids)) {
            info.cachePixResponse(pids);
        }
    }

    private boolean pixQueryAlreadyPerformed(IDWithIssuer[] pids) {
        switch (pids.length) {
        case 0:
            return false;
        case 1:
            return !(containsWildcard(pids[0].getID()) || pids[0].getIssuer() == null);
        default:
            return true;
        }
    }

    private boolean containsWildcard(String s) {
        return s.indexOf('*') >= 0 || s.indexOf('?') >= 0;
    }
}
