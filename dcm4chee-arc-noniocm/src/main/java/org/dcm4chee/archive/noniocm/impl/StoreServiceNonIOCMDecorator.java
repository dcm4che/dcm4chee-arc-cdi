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

package org.dcm4chee.archive.noniocm.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.StoreAction;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.QCInstanceHistory;
import org.dcm4chee.archive.noniocm.NonIOCMChangeRequestorService;
import org.dcm4chee.archive.noniocm.NonIOCMChangeRequestorService.NonIOCMChangeType;
import org.dcm4chee.archive.qc.StructuralChangeService;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.decorators.DelegatingStoreService;
import org.dcm4chee.conf.decorators.DynamicDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator to accommodate none-IOCM changes.
 * 
 * @author Franz Willer <franz.willer@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@DynamicDecorator
public class StoreServiceNonIOCMDecorator extends DelegatingStoreService {

    private static final Logger LOG = LoggerFactory.getLogger(StoreServiceNonIOCMDecorator.class);

    protected static final String NONE_IOCM_HIDE_NEW_INSTANCE = "NONE_IOCM_HIDE_NEW_INSTANCE";

    @Inject
    private NonIOCMChangeRequestorService nonIocmService;

    @Inject
    private StructuralChangeService scService;
    
    @Override
    public Instance findOrCreateInstance(EntityManager em, StoreContext context) throws DicomServiceException {
    	if (nonIocmService.isNonIOCMChangeRequestor(context.getStoreSession().getRemoteAET())) {
    		Attributes attrs = context.getAttributes();
    		String origSopIUID = attrs.getString(Tag.SOPInstanceUID);
    		QCInstanceHistory h = nonIocmService.getLastQCInstanceHistory(origSopIUID);
    		if (h != null) {
    			Collection<Instance> instances = scService.locateInstances(origSopIUID);
    			if (instances.size() > 0) {
    				String sourceAET = instances.iterator().next().getSeries().getSourceAET();
    				if (nonIocmService.isNonIOCMChangeRequest(context.getStoreSession().getRemoteAET(), sourceAET)) {
    					LOG.info("Instance already QCed! Change SOP Instance UID {} to current UID {}", origSopIUID, h.getCurrentUID());
    					attrs.setString(Tag.SOPInstanceUID, VR.UI, h.getCurrentUID());
    					if (h.getSeries().getOldSeriesUID().equals(attrs.getString(Tag.SeriesInstanceUID))) {
        					LOG.info("Change original Series Instance UID to current SeriesIUID {}", h.getCurrentSeriesUID());
        					attrs.setString(Tag.SeriesInstanceUID, VR.UI, h.getCurrentSeriesUID());
    					}
    					if (h.getSeries().getStudy().getOldStudyUID().equals(attrs.getString(Tag.StudyInstanceUID))) {
        					LOG.info("Change original Study Instance UID to current StudyIUID {}", h.getCurrentStudyUID());
        					attrs.setString(Tag.StudyInstanceUID, VR.UI, h.getCurrentStudyUID());
    					}
    				}
    			}
    		}
    	}
    	return getNextDecorator().findOrCreateInstance(em, context);
    }
    @Override
    public StoreAction instanceExists(EntityManager em, StoreContext context, Instance inst)
            throws DicomServiceException {
        if (nonIocmService.isNonIOCMChangeRequest(context.getStoreSession().getRemoteAET(), inst.getSeries().getSourceAET())) {
            LOG.debug("{} is a None IOCM Change Requestor! check for changes.", context.getStoreSession().getRemoteAET());
            NonIOCMChangeType chgType = nonIocmService.performChange(inst, context);
            if (chgType == NonIOCMChangeType.INSTANCE_CHANGE)
                return StoreAction.STORE;
            //TODO: how to handle NoneIOCMChangeType.ILLEGAL_CHANGE?
        }
        return getNextDecorator().instanceExists(em, context, inst);
    }
    
    @Override
    public Instance adjustForNoneIOCM(Instance instanceToStore, StoreContext context) {
    	String hideNewInstanceUID = (String) context.getProperty(StoreServiceNonIOCMDecorator.NONE_IOCM_HIDE_NEW_INSTANCE);
    	context.setProperty(StoreServiceNonIOCMDecorator.NONE_IOCM_HIDE_NEW_INSTANCE, null);
    	if (hideNewInstanceUID == null) {
	        String callingAET = context.getStoreSession().getRemoteAET();
	        int gracePeriodInSeconds = nonIocmService.getNonIOCMModalityGracePeriod(callingAET);
	        if (gracePeriodInSeconds > 0) {
	            LOG.info("{}: {} is a None IOCM Change Requestor Modality! check for non structural changes.",
	                    context.getStoreSession(), callingAET);
	            nonIocmService.handleModalityChange(instanceToStore, context, gracePeriodInSeconds);
	        }
    	} else {
    		nonIocmService.hideOrUnhideInstance(instanceToStore, NonIOCMChangeRequestorService.REJ_CODE_QUALITY_REASON);
    	}
        return getNextDecorator().adjustForNoneIOCM(instanceToStore, context);
    }
    
    public void store(StoreContext context) throws DicomServiceException {
        getNextDecorator().store(context);
    }

}
