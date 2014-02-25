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

package org.dcm4chee.archive.wado;

import java.util.EnumSet;

import javax.inject.Inject;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.QueryOption;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.retrieve.RetrieveService;

/**
 * Common Superclass for WADO services
 * 
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 */
public class Wado {
    
    protected String aetitle;
    
    protected ArchiveAEExtension arcAE;
    
    protected QueryParam queryParam;
    
    @Inject
    protected Device device;

    @Inject
    protected RetrieveService retrieveService;
    
    /**
     * Setter for the AETitle property, automatically invoked
     * by the CDI container. Setter initializes ArchiveAEExtension and
     * queryParam too.
     */
    @PathParam("AETitle")
    public void setAETitle(String aet) {
        this.aetitle=aet;
        ApplicationEntity ae = device.getApplicationEntity(aet);
        
        if (ae == null || !ae.isInstalled()
                || (arcAE = ae.getAEExtension(ArchiveAEExtension.class)) == null) {
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
        else
        {
            queryParam = arcAE.
                    getQueryParam(EnumSet.noneOf(QueryOption.class), getAccessControlIDs());
        }
    }
    
    private String[] getAccessControlIDs()
    {
        //TODO Access Control to be implemented
        return new String[0];
    }
}
