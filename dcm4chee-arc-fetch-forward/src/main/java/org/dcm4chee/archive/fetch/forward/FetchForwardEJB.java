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

package org.dcm4chee.archive.fetch.forward;

import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.storage.conf.StorageDeviceExtension;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
@Stateless
public class FetchForwardEJB {

    @Inject
    private Device device;

    @PersistenceContext(name = "dcm4chee-arc", unitName = "dcm4chee-arc")
    private EntityManager em;

    @SuppressWarnings("unchecked")
    private ArchiveInstanceLocator relocateandBuild(ArchiveInstanceLocator loc) {
        ArrayList<Instance> list = new ArrayList<Instance>();
        ArrayList<String> uids = new ArrayList<String>();
        uids.add(loc.iuid);
        Query query = em
                .createNamedQuery(Instance.FIND_BY_SOP_INSTANCE_UID_EAGER_MANY);
        query.setParameter("uids", uids);
        list = (ArrayList<Instance>) query.getResultList();
        Instance inst = list.get(0);
        Location location = withBulkData(inst.getLocations());
        if (location == null)
            return null;
        return createArchiveInstanceLocator(inst, location);
    }

    private Location withBulkData(Collection<Location> locations) {
        for (Location loc : locations)
            if (!loc.isWithoutBulkData())
                return loc;
        return null;
    }

    private ArchiveInstanceLocator createArchiveInstanceLocator(Instance inst,
            Location loc) {
        StorageDeviceExtension ext = device
                .getDeviceExtension(StorageDeviceExtension.class);
        ArchiveInstanceLocator newLocator = new ArchiveInstanceLocator.Builder(
                inst.getSopClassUID(), inst.getSopInstanceUID(),
                loc.getTransferSyntaxUID())
                .storageSystem(
                        ext.getStorageSystem(loc.getStorageSystemGroupID(),
                                loc.getStorageSystemID()))
                .storagePath(loc.getStoragePath())
                .entryName(loc.getEntryName())
                .fileTimeZoneID(loc.getTimeZone())
                .retrieveAETs(inst.getRawRetrieveAETs())
                .withoutBulkdata(loc.isWithoutBulkData())
                .seriesInstanceUID(inst.getSeries().getSeriesInstanceUID())
                .studyInstanceUID(
                        inst.getSeries().getStudy().getStudyInstanceUID())
                .build();
        byte[] encodedInstanceAttrs = inst.getAttributesBlob()
                .getEncodedAttributes();
        Attributes instanceAttrs = Utils.decodeAttributes(encodedInstanceAttrs);
        newLocator.setObject(instanceAttrs);
        return newLocator;
    }

    public ArchiveInstanceLocator updateLocator(ArchiveInstanceLocator locator) {
        return relocateandBuild(locator);
    }
}
