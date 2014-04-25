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

package org.dcm4chee.archive.query.scp;

import java.util.EnumSet;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCFindSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.dcm4che3.net.service.QueryTask;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.dto.LocalAssociationParticipant;
import org.dcm4chee.archive.query.Query;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.query.QueryService;
import org.dcm4chee.archive.query.impl.QueryEvent;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class CFindSCP extends BasicCFindSCP {

    private final String[] qrLevels;
    private final QueryRetrieveLevel rootLevel;

    @Inject
    private QueryService queryService;
    
    @Inject
    private Event<QueryEvent> queryEvent; 
    
    public CFindSCP(String sopClass, String... qrLevels) {
        super(sopClass);
        this.qrLevels = qrLevels;
        this.rootLevel = QueryRetrieveLevel.valueOf(qrLevels[0]);
    }

    @Override
    protected QueryTask calculateMatches(Association as, PresentationContext pc,
            Attributes rq, Attributes keys) throws DicomServiceException {
        QueryRetrieveLevel qrlevel = QueryRetrieveLevel.valueOf(keys, qrLevels);
        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        ExtendedNegotiation extNeg = as.getAAssociateAC().getExtNegotiationFor(cuid);
        EnumSet<QueryOption> queryOpts = QueryOption.toOptions(extNeg);
        boolean relational = queryOpts.contains(QueryOption.RELATIONAL);
        qrlevel.validateQueryKeys(keys, rootLevel, relational);
        
        ApplicationEntity ae = as.getApplicationEntity();
        ArchiveAEExtension arcAE = ae.getAEExtension(ArchiveAEExtension.class);
        try {
            QueryParam queryParam =  queryService.getQueryParam(
                    as, as.getRemoteAET(), arcAE, queryOpts);
            QueryContext ctx = queryService.createQueryContext(queryService);
            ctx.setArchiveAEExtension(arcAE);
            ctx.setKeys(keys);
            ctx.setQueryParam(queryParam);
            queryService.coerceAttributesForRequest(ctx, as.getRemoteAET());
            IDWithIssuer[] pids = queryService.queryPatientIDs(arcAE, keys, queryParam);
            ctx.setPatientIDs(pids);
            Query query = queryService.createQuery(qrlevel, ctx);
            try {
                query.initQuery();
                query.executeQuery();
            } catch (Exception e) {
                query.close();
                throw e;
            } finally {
                queryEvent.fire(new QueryEvent(ctx.getKeysOriginal(), pids, 
                        rq.getString(Tag.AffectedSOPClassUID),
                        ae.getDevice(),
                        new LocalAssociationParticipant(as)));
            }
            //return the cached keys
            return new QueryTaskImpl(as, pc, rq, ctx.getKeysOriginal(), queryParam,
                    rootLevel, query, queryService);
        } catch (DicomServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

}
