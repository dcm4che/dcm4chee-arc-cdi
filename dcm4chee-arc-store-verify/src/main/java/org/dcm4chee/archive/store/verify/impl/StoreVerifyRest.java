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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4chee.archive.store.verify.impl;
import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.verify.StoreVerifyService;
import org.dcm4chee.archive.stow.client.StowContext;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
@Path("/emulation")
@RequestScoped
public class StoreVerifyRest {

    @PersistenceContext(name = "dcm4chee-arc")
    EntityManager em;

    private static StorageDeviceExtension archStorageDevExt;

    @Inject
    private Device device;


    @Inject
    private IApplicationEntityCache aeCache;
    
    @Context
    private HttpServletRequest request;

    @Inject
    private StoreVerifyService service;

    
    @GET
    @Path("emulateStoreRemember")
    public void emulateStoreRemember() throws Exception {
        archStorageDevExt = device.getDeviceExtension(
                StorageDeviceExtension.class);
        String sopUID = "1.2.3.4";
        ApplicationEntity arcAE = device.getApplicationEntity("DCM4CHEE");
        StowContext ctx = new StowContext(arcAE, arcAE, ServiceType.STOREREMEMBER);
        ctx.setStowRemoteBaseURL("http://localhost:8080/dcm4chee-arc/");
        ctx.setQidoRemoteBaseURL("http://localhost:8080/dcm4chee-arc/");
        ArrayList<ArchiveInstanceLocator> locators = 
                new ArrayList<ArchiveInstanceLocator>();
        locators.add(locateInstance(sopUID));
        service.store(ctx, locators);
    }

    @GET
    @Path("emulateStoreVerify")
    public void emulateStoreVerify() throws Exception {
        archStorageDevExt = device.getDeviceExtension(
                StorageDeviceExtension.class);
        String sopUID = "1.2.3.4";
        ApplicationEntity arcAE = device.getApplicationEntity("DCM4CHEE");
        StowContext ctx = new StowContext(arcAE, arcAE, ServiceType.STOREVERIFY);
        ctx.setStowRemoteBaseURL("http://localhost:8080/dcm4chee-arc/");
        ctx.setQidoRemoteBaseURL("http://localhost:8080/dcm4chee-arc/");
        
        ArrayList<ArchiveInstanceLocator> locators = 
                new ArrayList<ArchiveInstanceLocator>();
        locators.add(locateInstance(sopUID));
        service.store(ctx, locators);
    }

    @GET
    @Path("emulateStoreRememberDimse")
    public void emulateStoreRememberDimse() throws Exception {
        archStorageDevExt = device.getDeviceExtension(
                StorageDeviceExtension.class);
        String sopUID = "1.2.3.4";
        ApplicationEntity arcAE = device.getApplicationEntity("DCM4CHEE");
        ApplicationEntity dcmqrSCP = aeCache.findApplicationEntity("DCMQRSCP");
        CStoreSCUContext ctx = new CStoreSCUContext(arcAE, dcmqrSCP, ServiceType.STOREREMEMBER);
        ArrayList<ArchiveInstanceLocator> locators = 
                new ArrayList<ArchiveInstanceLocator>();
        locators.add(locateInstance(sopUID));
        service.store(ctx, locators);
    }

    @GET
    @Path("emulateStoreVerifyDimse")
    public void emulateStoreVerifyDimse() throws Exception {
        archStorageDevExt = device.getDeviceExtension(
                StorageDeviceExtension.class);
        String sopUID = "1.2.3.4";
        ApplicationEntity arcAE = device.getApplicationEntity("DCM4CHEE");
        CStoreSCUContext ctx = new CStoreSCUContext(arcAE, arcAE, ServiceType.STOREVERIFY);
        ArrayList<ArchiveInstanceLocator> locators = 
                new ArrayList<ArchiveInstanceLocator>();
        locators.add(locateInstance(sopUID));
        service.store(ctx, locators);
    }

    private Collection<Location> getFileAliasRefs(Instance instance) {
        Query query = em.createQuery("SELECT i.locations FROM Instance"
                + " i where i.sopInstanceUID = ?1");
        query.setParameter(1, instance.getSopInstanceUID());
        return query.getResultList();
    }

    private ArchiveInstanceLocator locateInstance(String sopInstanceUID) {
        ArrayList<Instance> list = new ArrayList<Instance>();
        ArrayList<String> uids = new ArrayList<String>();
        uids.add(sopInstanceUID);
        Query query  = em.createNamedQuery(
                Instance.FIND_BY_SOP_INSTANCE_UID_EAGER_MANY);
        query.setParameter("uids", uids);
        list = (ArrayList<Instance>) query.getResultList();
        Instance inst = list.get(0);
        String sopClassUID = inst.getSopClassUID(); 
        Location location = ((ArrayList<Location>)getFileAliasRefs(inst)).get(0);
        StorageSystem system = archStorageDevExt.getStorageSystem(
                location.getStorageSystemGroupID(), location.getStorageSystemID());
        ArchiveInstanceLocator instanceLocator = new ArchiveInstanceLocator.Builder(
                sopClassUID, sopInstanceUID, location.getTransferSyntaxUID())
        .storageSystem(system)
        .storagePath(location.getStoragePath())
        .entryName(location.getEntryName())
        .fileTimeZoneID(location.getTimeZone())
        .withoutBulkdata(true)
        .build();
        if(instanceLocator.getObject() == null)
            instanceLocator.setObject(new Attributes());
        return instanceLocator;
    }
}
