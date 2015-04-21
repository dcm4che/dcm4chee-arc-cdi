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
package org.dcm4chee.archive.store.verify;

import java.util.ArrayList;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.entity.ExternalRetrieveLocation;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.StoreVerifyDimse;
import org.dcm4chee.archive.entity.StoreVerifyStatus;
import org.dcm4chee.archive.entity.StoreVerifyWeb;
import org.dcm4chee.storage.conf.Availability;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
@Stateless
public class StoreVerifyEJB {

    @PersistenceContext(unitName = "dcm4chee-arc")
    EntityManager em;

    public void addWebEntry(String transactionID
            , String qidoBaseURL ,String remoteAET, String localAET, ServiceType service) {
        StoreVerifyWeb webEntry = new StoreVerifyWeb();
        webEntry.setLocalAET(localAET);
        webEntry.setRemoteAET(remoteAET);
        webEntry.setQidoBaseURL(qidoBaseURL);
        webEntry.setStatus(StoreVerifyStatus.PENDING);
        webEntry.setTransactionID(transactionID);
        webEntry.setService(service.toString());
        em.persist(webEntry);
    }

    public void removeWebEntry(String transactionID) {
        em.remove(getWebEntry(transactionID));
    }

    public StoreVerifyWeb getWebEntry(String transactionID) {
        Query query  = em.createNamedQuery(StoreVerifyWeb
                .GET_STORE_VERIFY_WEB_ENTRY);
        query.setParameter(1, transactionID);
        StoreVerifyWeb webEntry = (StoreVerifyWeb) 
                query.getSingleResult();
        
        if(webEntry == null) {
            throw new EntityNotFoundException("Unable to find "
                    + "StoreVerifyWebEntry for transaction "+transactionID);
        }
        
        return webEntry;
    }

    public void addDimseEntry(String transactionID
            , String remoteAET, String localAET, ServiceType service) {
        StoreVerifyDimse dimseEntry = new StoreVerifyDimse();
        dimseEntry.setLocalAET(localAET);
        dimseEntry.setRemoteAET(remoteAET);
        dimseEntry.setStatus(StoreVerifyStatus.PENDING);
        dimseEntry.setTransactionID(transactionID);
        dimseEntry.setService(service.toString());
        em.persist(dimseEntry);
    }

    public void removeDimseEntry(String transactionID) {
        em.remove(getDimseEntry(transactionID));
    }

    public StoreVerifyDimse getDimseEntry(String transactionID) {
        Query query  = em.createNamedQuery(StoreVerifyDimse
                .GET_STORE_VERIFY_DIMSE_ENTRY);
        query.setParameter(1, transactionID);
        StoreVerifyDimse dimseEntry = (StoreVerifyDimse) 
                query.getSingleResult();
        
        if(dimseEntry == null) {
            throw new EntityNotFoundException("Unable to find "
                    + "StoreVerifyDimseEntry for transaction "+transactionID);
        }
        
        return dimseEntry;
    }

    public void updateStatus(String transactionID
            , StoreVerifyStatus status) {

        if (transactionID.startsWith("dimse")) {
            StoreVerifyDimse dimseEntry = getDimseEntry(transactionID);
            dimseEntry.setStatus(status);
            em.merge(dimseEntry);
        }
        else {
            StoreVerifyWeb webEntry = getWebEntry(transactionID);
            webEntry.setStatus(status);
            em.merge(webEntry);
        }
    }

    /*
     * External Location Service methods
     */

    public void addExternalLocation(String iuid, String retrieveAET,
            Availability availability) {
        ExternalRetrieveLocation location = new ExternalRetrieveLocation(retrieveAET, availability);
        
        Query query = em.createQuery("select i from Instance i where"
                + " i.sopInstanceUID = ?1");
        query.setParameter(1, iuid);
        Instance instance = (Instance) query.getSingleResult();
        location.setInstance(instance);
        em.persist(location);
    }

    public void removeExternalLocation(String iuid, String retrieveAET) {
        
        Query query = em.createNamedQuery(ExternalRetrieveLocation
                .FIND_EXT_LOCATIONS_BY_IUID_RETRIEVE_AET);
        query.setParameter(1, iuid); //sop UID
        query.setParameter(2, retrieveAET); //retrieve AETitle
        ArrayList<ExternalRetrieveLocation> list = 
                (ArrayList<ExternalRetrieveLocation>) query.getResultList();
        for(ExternalRetrieveLocation extLocation : list)
            em.remove(extLocation);
    }

    public void removeExternalLocation(String iuid, Availability availability) {
        Query query = em.createNamedQuery(ExternalRetrieveLocation
                .FIND_EXT_LOCATIONS_BY_IUID_AVAILABILITY);
        query.setParameter(1, iuid); //sop UID
        query.setParameter(2, availability); //availability
        ArrayList<ExternalRetrieveLocation> list = 
                (ArrayList<ExternalRetrieveLocation>) query.getResultList();
        for(ExternalRetrieveLocation extLocation : list)
            em.remove(extLocation);
    }
}
