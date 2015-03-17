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

package org.dcm4chee.archive.conf;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.data.Code;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@LDAP(objectClasses = "dcmRejectionNote", distinguishingField = "dcmRejectionNoteTitle")
@ConfigurableClass
public class RejectionParam implements Serializable{

    private static final long serialVersionUID = -8357808456831886274L;

    @ConfigurableProperty(name="dcmRejectionNoteTitle")
    private Code rejectionNoteTitle;

    @ConfigurableProperty(name="dcmRejectedObjectRetentionTime")
    private int retentionTime;

    @ConfigurableProperty(name="dcmRejectedObjectRetentionTimeUnit")
    private TimeUnit retentionTimeUnit;

    @ConfigurableProperty(name="dcmAcceptPreviousRejectedInstance")
    private StoreAction acceptPreviousRejectedInstance;

    @ConfigurableProperty(name="dcmOverwritePreviousRejection")
    private Code[] overwritePreviousRejection = {};

    @ConfigurableProperty(name="dcmRevokeRejection", defaultValue = "false")
    private boolean revokeRejection;

    public final Code getRejectionNoteTitle() {
        return rejectionNoteTitle;
    }

    public final void setRejectionNoteTitle(Code title) {
        this.rejectionNoteTitle = title;
    }

    public final StoreAction getAcceptPreviousRejectedInstance() {
        return acceptPreviousRejectedInstance;
    }

    public final void setAcceptPreviousRejectedInstance(
            StoreAction acceptPreviousRejectedInstance) {
        this.acceptPreviousRejectedInstance = acceptPreviousRejectedInstance;
    }

    public final Code[] getOverwritePreviousRejection() {
        return overwritePreviousRejection;
    }

    public final void setOverwritePreviousRejection(
            Code[] overwritePreviousRejection) {
        this.overwritePreviousRejection = overwritePreviousRejection;
    }

    public final boolean isRevokeRejection() {
        return revokeRejection;
    }

    public final void setRevokeRejection(boolean revokeRejection) {
        this.revokeRejection = revokeRejection;
    }

    @Override
    public String toString() {
        return "RejectionParam [rejectionNoteTitle=" + rejectionNoteTitle
                + ", revokeRejection=" + revokeRejection
                + ", acceptPreviousRejectedInstance="
                + acceptPreviousRejectedInstance
                + ", overwritePreviousRejection="
                + Arrays.toString(overwritePreviousRejection) + "]";
    }

    public static RejectionParam forRejectionNoteTitle(Code title,
            RejectionParam... from) {
        for (RejectionParam rejectionParam : from) {
            if (rejectionParam.getRejectionNoteTitle().equalsIgnoreMeaning(title))
                return rejectionParam;
        }
        return null;
    }

    public int getRetentionTime() {
        return retentionTime;
    }

    public void setRetentionTime(int retentionTime) {
        this.retentionTime = retentionTime;
    }

    public TimeUnit getRetentionTimeUnit() {
        return retentionTimeUnit;
    }

    public void setRetentionTimeUnit(TimeUnit retentionTimeUnit) {
        this.retentionTimeUnit = retentionTimeUnit;
    }
}
