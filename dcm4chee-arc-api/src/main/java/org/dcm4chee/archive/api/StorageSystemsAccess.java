//
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

package org.dcm4chee.archive.api;

/**
 * API for Storage Systems and Storage System Groups.
 * 
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public interface StorageSystemsAccess {
    
    public static final String JNDI_NAME = "java:global/org.dcm4chee.archive.api.StorageSystemsAccess";

    /**
     * Check whether the storage system group is empty, i.e. no storage system
     * within the group contains objects.
     * 
     * @param storageSystemGroupID
     *            id of storage system group
     * @return true if the storage system group is empty, false otherwise
     */
    public boolean isStorageSystemGroupEmpty(String storageSystemGroupID);
    
    /**
     * Check whether the storage system is empty, i.e. it does not contain any
     * objects.
     * 
     * @param storageSystemGroupID
     *            id of storage system group
     * @param storageSystemID
     *            id if storage system within the storage system group
     * @return true if the storage system is empty, false otherwise
     */
    public boolean isStorageSystemEmpty(String storageSystemGroupID, String storageSystemID);

}
