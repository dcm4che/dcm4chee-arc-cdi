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

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4chee.archive.retrieve.RetrieveContext;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator to apply MIMA specifications to the Retrieve Service.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Decorator
public abstract class RetrieveServiceMIMADecorator implements RetrieveService {

    private static Logger LOG =
            LoggerFactory.getLogger(RetrieveServiceMIMADecorator.class);

    @Inject @Delegate
    private RetrieveService retrieveService;

    @Inject
    private IApplicationEntityCache aeCache;

    @Inject
    private PIXConsumer pixConsumer;

    /*
     * Extends default queryPatientIDs method associating an issuer to the 
     * queried id (if any) and performing a PIX Query. The pids resulting
     * from the PIX query are then used for the internal DICOM query along
     * the original queried id.
     */
    @Override
    public IDWithIssuer[] queryPatientIDs(RetrieveContext context, Attributes keys) {
        IDWithIssuer pid = IDWithIssuer.pidOf(keys);
        if (pid == null)
            return IDWithIssuer.EMPTY;

        if (pid.getIssuer() == null) {
            ApplicationEntity sourceAE = findApplicationEntity(context.getSourceAET());
            if (sourceAE != null) {
                if (context.getDestinationAE() == null) { // C-GET
                    context.setDestinationAE(sourceAE);
                }
                pid.setIssuer(sourceAE.getDevice().getIssuerOfPatientID());
            }
            if (pid.getIssuer() == null) {
                LOG.info("No Issuer of Patient ID associated with AE {} - cannot query for Other Patient IDs",
                        context.getSourceAET());
                return new IDWithIssuer[]{ pid };
            }
        }
        return pixConsumer.pixQuery(context.getArchiveAEExtension(), pid);
    }

    private ApplicationEntity findApplicationEntity(String aet) {
        try {
            return aeCache.findApplicationEntity(aet);
        } catch (ConfigurationException e) {
            LOG.warn("Failed to access configuration for AE {}:", aet, e);
            return null;
        }
    }

}
