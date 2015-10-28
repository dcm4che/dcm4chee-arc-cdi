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

package org.dcm4chee.archive.noneiocm.impl;

import java.util.List;

import javax.inject.Inject;

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.noneiocm.NoneIOCMChangeRequestorQRService;
import org.dcm4chee.archive.noneiocm.NoneIOCMChangeRequestorService;
import org.dcm4chee.archive.retrieve.RetrieveContext;
import org.dcm4chee.archive.retrieve.decorators.DelegatingRetrieveService;
import org.dcm4chee.conf.decorators.DynamicDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator to change original UIDs in a retrieve request from a NONE IOCM Requestor to the current ones.
 * 
 * @author Franz Willer <franz.willer@gmail.com>
 *
 */
@DynamicDecorator
public class RetrieveServiceNoneIOCMDecorator extends DelegatingRetrieveService {

    private static Logger LOG =
            LoggerFactory.getLogger(RetrieveServiceNoneIOCMDecorator.class);

    private static final ThreadLocal<String> sourceAET = new ThreadLocal<String>();
    
    @Inject
    NoneIOCMChangeRequestorService noneIocmService;

    @Inject
    NoneIOCMChangeRequestorQRService noneIocmQRService;
    
    @Inject
    private IApplicationEntityCache aeCache;

	@Override
	public IDWithIssuer[] queryPatientIDs(RetrieveContext context, Attributes keys) {
		sourceAET.set(context.getSourceAET());
		return getNextDecorator().queryPatientIDs(context, keys);
	}

	@Override
	public List<ArchiveInstanceLocator> calculateMatches(IDWithIssuer[] pids,
			Attributes keys, QueryParam queryParam, boolean withoutBulkData) {
        if (noneIocmService.isNoneIOCMChangeRequestor(sourceAET.get())) {
            LOG.info("Is NoneIOCM Change Requestor Device");
			try {
				ApplicationEntity sourceAE = aeCache.findApplicationEntity(sourceAET.get());
	            if (sourceAE != null)
	            	noneIocmQRService.updateQueryRequestAttributes(keys, sourceAE.getDevice().getApplicationAETitles());
			} catch (ConfigurationException ignore) {}
        }
		return getNextDecorator().calculateMatches(pids, keys, queryParam, withoutBulkData);
	}

	@Override
	public List<ArchiveInstanceLocator> calculateMatches(String studyUID, String seriesUID,
			String objectUID, QueryParam queryParam, boolean withoutBulkData) {
        if (noneIocmService.isNoneIOCMChangeRequestor(sourceAET.get())) {
            LOG.info("Is NoneIOCM Change Requestor Device");
			try {
				ApplicationEntity sourceAE = aeCache.findApplicationEntity(sourceAET.get());
	            if (sourceAE != null) {
	            	Attributes keys = new Attributes();
	            	keys.setString(Tag.QueryRetrieveLevel, VR.CS, "IMAGE");
	            	keys.setString(Tag.StudyInstanceUID, VR.UI, studyUID);
	            	keys.setString(Tag.SeriesInstanceUID, VR.UI, seriesUID);
	            	keys.setString(Tag.SOPInstanceUID, VR.UI, objectUID);
	            	noneIocmQRService.updateQueryRequestAttributes(keys, sourceAE.getDevice().getApplicationAETitles());
	            	studyUID = keys.getString(Tag.StudyInstanceUID);
	            	seriesUID = keys.getString(Tag.SeriesInstanceUID);
	            	objectUID = keys.getString(Tag.SOPInstanceUID);
	            }
			} catch (ConfigurationException ignore) {}
        }
		return getNextDecorator().calculateMatches(studyUID, seriesUID, objectUID, queryParam, withoutBulkData);
	}

}
