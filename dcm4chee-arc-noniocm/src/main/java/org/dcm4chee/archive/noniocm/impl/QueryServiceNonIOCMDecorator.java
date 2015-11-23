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

import javax.inject.Inject;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.noniocm.NonIOCMChangeRequestorQRService;
import org.dcm4chee.archive.noniocm.NonIOCMChangeRequestorService;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.query.decorators.DelegatingQueryService;
import org.dcm4chee.conf.decorators.DynamicDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator to apply MIMA specifications to the Query Service.
 * 
 * @author Franz Willer <franz.willer@gmail.com>
 * 
 */
@DynamicDecorator
public class QueryServiceNonIOCMDecorator extends DelegatingQueryService {

    private static Logger LOG = LoggerFactory
            .getLogger(QueryServiceNonIOCMDecorator.class);

    @Inject
    NonIOCMChangeRequestorService noneIocmService;
    
    @Inject
    NonIOCMChangeRequestorQRService noneIocmQRService;
    

    @Override
    public void coerceRequestAttributes(QueryContext context) throws DicomServiceException {
            getNextDecorator().coerceRequestAttributes(context);            
            if (noneIocmService.isNonIOCMChangeRequestor(context.getRemoteAET())) {
                LOG.info("Is NoneIOCM Change Requestor Device");
                Device d = noneIocmQRService.getRemoteDevice(context);
                if (d != null)
                    noneIocmQRService.updateQueryRequestAttributes(context.getKeys(), d.getApplicationAETitles());
            }
    }

    /*
     * Extends the default coerceResponseAttributes method, update UID attributes of the matched instances returned by the query for NoneIOCM devices.
     */
    @Override
    public void coerceResponseAttributes(QueryContext context, Attributes match) throws DicomServiceException {
        getNextDecorator().coerceResponseAttributes(context, match);
        if (noneIocmService.isNonIOCMChangeRequestor(context.getRemoteAET())) {
            LOG.info("Is NoneIOCM Change Requestor Device");
            noneIocmQRService.updateQueryResponseAttributes(context, match);
        }
    }

}
