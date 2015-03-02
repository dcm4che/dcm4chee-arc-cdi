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

import java.util.HashMap;
import java.util.TimeZone;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.query.QueryService;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class QueryContextImpl implements QueryContext {

    private final QueryService service;

    private ArchiveAEExtension arcAE;

    private String remoteAET;

    private String serviceSOPClassUID;

    private IDWithIssuer[] pids;

    private Attributes keys;

    private QueryParam queryParam;

    private Attributes keysOriginal;
    
    private TimeZone requestedTimeZone;
    
    private ApplicationEntity remoteAE;
    
    private final HashMap<String,Object> properties = new HashMap<String,Object>();
    
    public QueryContextImpl(QueryService service) {
        this.service = service;
    }

    @Override
    public QueryService getQueryService() {
        return service;
    }

    @Override
    public ArchiveAEExtension getArchiveAEExtension() {
        return arcAE;
    }

    @Override
    public void setArchiveAEExtension(ArchiveAEExtension arcAE) {
        this.arcAE = arcAE;
    }

    @Override
    public String getRemoteAET() {
        return remoteAET;
    }

    @Override
    public void setRemoteAET(String remoteAET) {
        this.remoteAET = remoteAET;
    }

    @Override
    public String getServiceSOPClassUID() {
        return serviceSOPClassUID;
    }

    @Override
    public void setServiceSOPClassUID(String serviceSOPClassUID) {
        this.serviceSOPClassUID = serviceSOPClassUID;
    }

    @Override
    public Attributes getKeys() {
        return keys;
    }
    
    @Override
    public void setKeys(Attributes keys) {
        this.keys = keys;
        this.keysOriginal = new Attributes(keys);
    }

    @Override
    public Attributes getKeysOriginal() {
        return keysOriginal;
    }
    
    @Override
    public IDWithIssuer[] getPatientIDs() {
        return pids;
    }

    @Override
    public void setPatientIDs(IDWithIssuer[] pids) {
        this.pids = pids;
    }

    @Override
    public QueryParam getQueryParam() {
        return queryParam;
    }

    @Override
    public void setQueryParam(QueryParam queryParam) {
        this.queryParam = queryParam;
    }

    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public Object removeProperty(String key) {
        return properties.remove(key);
    }

    @Override
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    @Override
    public void setRequestedTimeZone(TimeZone tz)
    {
	this.requestedTimeZone=tz;
    }

    @Override
    public TimeZone getRequestedTimeZone() {
        return this.requestedTimeZone;
    }

    @Override
    public void setRemoteApplicationEntity(ApplicationEntity ae) {
        this.remoteAE = ae;
    }

    @Override
    public Device getRemoteDevice() {
        return this.remoteAE != null ? this.remoteAE.getDevice() : null;
    }

    @Override
    public TimeZone getRemoteDeviceTimeZone() {
       return this.remoteAE != null? this.remoteAE.getDevice().getTimeZoneOfDevice() : null;
    }
}
