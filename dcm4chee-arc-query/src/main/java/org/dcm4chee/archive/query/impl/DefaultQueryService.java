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
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.query.QueryService;
import org.hibernate.Session;
import org.hibernate.StatelessSession;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@ApplicationScoped
public class DefaultQueryService implements QueryService {

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    @Inject
    QueryServiceEJB ejb;
    
    @Inject
    private IApplicationEntityCache aeCache;
	
    StatelessSession openStatelessSession() {
        return em.unwrap(Session.class).getSessionFactory().openStatelessSession();
    }

    @Override
    public QueryContext createQueryContext(QueryRetrieveLevel qrlevel,
            QueryService queryService){
        switch (qrlevel) {
        case PATIENT:
            return queryService.createPatientQueryContext(queryService);
        case STUDY:
            return queryService.createStudyQueryContext(queryService);
        case SERIES:
            return queryService.createSeriesQueryContext(queryService);
        case IMAGE:
            return queryService.createInstanceQueryContext(queryService);
        default:
            throw new IllegalArgumentException("qrlevel: " + qrlevel);
        }
    }

    @Override
    public QueryContext createPatientQueryContext(QueryService queryService) {
        return new PatientQueryContext(queryService, openStatelessSession());
    }

    @Override
    public QueryContext createStudyQueryContext(QueryService queryService) {
        return new StudyQueryContext(queryService, openStatelessSession());
    }

    @Override
    public QueryContext createSeriesQueryContext(QueryService queryService) {
        return new SeriesQueryContext(queryService, openStatelessSession());
    }

    @Override
    public QueryContext createInstanceQueryContext(QueryService queryService) {
        return new InstanceQueryContext(queryService, openStatelessSession());
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
            ArchiveAEExtension aeExt, EnumSet<QueryOption> queryOpts) {
        return aeExt.getQueryParam(queryOpts, accessControlIDs(source));
    }

    @Override
    public IDWithIssuer[] queryPatientIDs(
            ArchiveAEExtension aeExt, Attributes keys, QueryParam queryParam) {
        IDWithIssuer pid = IDWithIssuer.fromPatientIDWithIssuer(keys);
        return pid == null ? IDWithIssuer.EMPTY : new IDWithIssuer[] { pid };
    }

    private String[] accessControlIDs(Object source) {
        return StringUtils.EMPTY_STRING;
    }

    @Override
    public void adjustMatch(QueryContext query, Attributes match) {
        
    }

    /* coerceAttributesForRequest
     * applies a loaded XSL stylesheet on the keys per request if given
     *  currently 17/4/2014 modifies date and time attributes in the keys per request
     */
    @Override
    public void coerceAttributesForRequest(QueryContext context, String sourceAET)
	    throws DicomServiceException {
	
	 ApplicationEntity sourceAE = null;
	try {
	    sourceAE = aeCache.findApplicationEntity(sourceAET);
	} catch (ConfigurationException e1) {
	    e1.printStackTrace();
	}
	
	try {
            
            ArchiveAEExtension arcAE = context.getArchiveAEExtension();
            Attributes attrs = context.getKeys();
            TimeZone archiveTimeZone = arcAE.getApplicationEntity().getDevice().getTimeZoneOfDevice();
            TimeZone sourceTimeZone = sourceAE.getDevice().getTimeZoneOfDevice();
            Templates tpl = arcAE.getAttributeCoercionTemplates(
                    attrs.getString(Tag.SOPClassUID),
                    Dimse.C_FIND_RQ, TransferCapability.Role.SCP, 
                    sourceAET);
            if (tpl != null) {
		attrs.addAll(SAXTransformer.transform(attrs, tpl, false, false));
            }
            //Time zone query req adjustments
            if(archiveTimeZone!=null){
                if(attrs.contains(Tag.TimezoneOffsetFromUTC))
                {
            	attrs.setTimezone(archiveTimeZone);
            	context.setCachedTimeZoneFromTag(attrs.getString(Tag.TimezoneOffsetFromUTC));
                }
                else if(sourceTimeZone!=null)
                {            	
            	attrs.setDefaultTimeZone(sourceTimeZone);
            	attrs.setTimezone(sourceTimeZone);
            	attrs.setTimezone(archiveTimeZone);
                }
                //else assume archive time
            }
            
           
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
	
    }
    /* coerceAttributesForResponse
     * applies a loaded XSL stylesheet on the keys per response if given
     *  currently 17/4/2014 modifies date and time attributes in the keys per response
     */
    @Override
    public void coerceAttributesForResponse(Attributes match,QueryContext context, String sourceAET)
	    throws DicomServiceException {
	 
	 ApplicationEntity sourceAE = null;
		try {
		    sourceAE = aeCache.findApplicationEntity(sourceAET);
		} catch (ConfigurationException e1) {
		    e1.printStackTrace();
		}
		try {
	            
	            ArchiveAEExtension arcAE = context.getArchiveAEExtension();
	            Attributes attrs = match;
	            TimeZone archiveTimeZone = arcAE.getApplicationEntity().getDevice().getTimeZoneOfDevice();
	            TimeZone sourceTimeZone = sourceAE.getDevice().getTimeZoneOfDevice();
	            Templates tpl = arcAE.getAttributeCoercionTemplates(
	                    attrs.getString(Tag.SOPClassUID),
	                    Dimse.C_FIND_RSP, TransferCapability.Role.SCU, 
	                    sourceAET);
	            if (tpl != null) {
			attrs.addAll(SAXTransformer.transform(attrs, tpl, false, false));
	            }
	            //Time zone query req adjustments
	            if(archiveTimeZone!=null){
	        	String tmpTagValue=context.getCachedTimeZoneFromTag();
	                if(tmpTagValue!=null)
	                {
	                attrs.setDefaultTimeZone(archiveTimeZone);
	            	attrs.setTimezoneOffsetFromUTC(context.getCachedTimeZoneFromTag());
	                }
	                else if(sourceTimeZone!=null)
	                {    
	                    int offsetFromUTC = sourceTimeZone.getRawOffset();
	                    if(attrs.contains(Tag.StudyDate))
	                    {
	                	offsetFromUTC = sourceTimeZone.getOffset(attrs.getDate(Tag.StudyDate).getTime());
	                    }
	            	attrs.setDefaultTimeZone(archiveTimeZone);
	            	attrs.setTimezone(archiveTimeZone);
	            	attrs.setTimezone(sourceTimeZone);
	                String offsetString = timeOffsetInMillisToDICOMTimeOffset(offsetFromUTC);
	            	attrs.setString(Tag.TimezoneOffsetFromUTC, VR.SH, ""+offsetString);
	                }
	                else //assumed in archive time
	                {
	                    int offsetFromUTC = archiveTimeZone.getRawOffset();
	                    if(attrs.contains(Tag.StudyDate))
	                    {
	                	offsetFromUTC = archiveTimeZone.getOffset(attrs.getDate(Tag.StudyDate).getTime());
	                    }
	                    String offsetString = timeOffsetInMillisToDICOMTimeOffset(offsetFromUTC);
	                    attrs.setString(Tag.TimezoneOffsetFromUTC, VR.SH, ""+offsetString);
	                }
	            }
	            
	           
	        } catch (Exception e) {
	            throw new DicomServiceException(Status.UnableToProcess, e);
	        }
    }
    public String timeOffsetInMillisToDICOMTimeOffset(int millis)
    {
    int mns = millis/(1000*60) ;
    String h = ""+(int) mns/60;
    if(h.length()==1)
    {
        String tmp = h;
        h="0"+tmp;
    }
    String m = ""+(int) (mns%60); 
    if(m.length()==1)
    {
        String tmp = m;
        m="0"+tmp;
    }
    String sign = (int) Math.signum(mns)>0?"+":"-";
	return sign+h+m;
    }

}
