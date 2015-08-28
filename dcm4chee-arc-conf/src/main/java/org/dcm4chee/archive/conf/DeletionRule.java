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
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
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
package org.dcm4chee.archive.conf;

import java.io.Serializable;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;

/**
 * @author Steve Kroetsch <stevekroetsch@hotmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */
@LDAP(objectClasses = "dcmDeletionRule", distinguishingField = "cn")
@ConfigurableClass
public final class DeletionRule implements Serializable {

    private static final long serialVersionUID = 8031739057252888989L;

    @ConfigurableProperty(name = "cn")
    private String commonName;

    @ConfigurableProperty(name = "dcmStorageSystemGroupID")
    private String storageSystemGroupID;

    @ConfigurableProperty(name = "dcmNumberOfArchivedCopies")
    private String numberOfArchivedCopies;

    @ConfigurableProperty(name = "dcmSafeArchivingType")
    private String safeArchivingType;

    @ConfigurableProperty(name = "dcmArchivedOnGroups")
    private String[] archivedOnGroups;

    @ConfigurableProperty(name = "dcmArchivedOnExternalSystems")
    private String[] archivedOnExternalSystems;

    @ConfigurableProperty(name = "dcmMinTimeStudyNotAccessed", defaultValue = "0")
    private int minTimeStudyNotAccessed;

    @ConfigurableProperty(name = "dcmMinTimeStudyNotAccessedUnit")
    private String minTimeStudyNotAccessedUnit;

    @ConfigurableProperty(name = "dcmDeleteAsMuchAsPossible", defaultValue = "false")
    private boolean deleteAsMuchAsPossible;

    @ConfigurableProperty(name = "dcmDeletionThreshold")
    private String deletionThreshold;

    

    @Deprecated
    @ConfigurableProperty(name = "dcmArchivedAnyWhere", defaultValue = "false")
    private boolean archivedAnyWhere;

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getStorageSystemGroupID() {
        return storageSystemGroupID;
    }

    public void setStorageSystemGroupID(String storageSystemGroupID) {
        this.storageSystemGroupID = storageSystemGroupID;
    }

    @Deprecated
    public boolean isArchivedAnyWhere() {
        return archivedAnyWhere;
    }

    @Deprecated
    public void setArchivedAnyWhere(boolean archivedAnyWhere) {
        this.archivedAnyWhere = archivedAnyWhere;
    }

    public String[] getArchivedOnGroups() {
        return archivedOnGroups;
    }

    public void setArchivedOnGroups(String[] archivedOnGroups) {
        this.archivedOnGroups = archivedOnGroups;
    }

    public String[] getArchivedOnExternalSystems() {
        return archivedOnExternalSystems;
    }

    public void setArchivedOnExternalSystems(String[] archivedOnExternalSystems) {
        this.archivedOnExternalSystems = archivedOnExternalSystems;
    }

    public String getNumberOfArchivedCopies() {
		return numberOfArchivedCopies;
	}

	public void setNumberOfArchivedCopies(String numberOfArchivedCopies) {
		this.numberOfArchivedCopies = numberOfArchivedCopies;
	}

	public String getSafeArchivingType() {
		return safeArchivingType;
	}

	public void setSafeArchivingType(String safeArchivingType) {
		this.safeArchivingType = safeArchivingType;
	}

	public int getMinTimeStudyNotAccessed() {
        return minTimeStudyNotAccessed;
    }

    public void setMinTimeStudyNotAccessed(int minTimeStudyNotAccessed) {
        this.minTimeStudyNotAccessed = minTimeStudyNotAccessed;
    }

    public String getMinTimeStudyNotAccessedUnit() {
        return minTimeStudyNotAccessedUnit;
    }

    public void setMinTimeStudyNotAccessedUnit(String minTimeStudyNotAccessedUnit) {
        this.minTimeStudyNotAccessedUnit = minTimeStudyNotAccessedUnit;
    }

    public boolean isDeleteAsMuchAsPossible() {
        return deleteAsMuchAsPossible;
    }

    public void setDeleteAsMuchAsPossible(boolean deleteAsMuchAsPossible) {
        this.deleteAsMuchAsPossible = deleteAsMuchAsPossible;
    }

    public String getDeletionThreshold() {
        return deletionThreshold;
    }

    public void setDeletionThreshold(String deletionThreshold) {
        this.deletionThreshold = deletionThreshold;
    }

    public boolean validate() {
        return (checkDeletionConstraints() && checkStudyRetentionContraints());
    }

    private boolean checkStudyRetentionContraints() {
        return getMinTimeStudyNotAccessed() > 0
                && getMinTimeStudyNotAccessedUnit() != null;
    }

    private boolean checkDeletionConstraints() {
        return getDeletionThreshold() != null || isDeleteAsMuchAsPossible();
    }

}