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

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.data.Code;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@LDAP(objectClasses = "dcmQueryRetrieveView", distinguishingField = "dcmQueryRetrieveViewID")
@ConfigurableClass
public class QueryRetrieveView implements Serializable{

    private static final long serialVersionUID = -8289508886431886274L;

    @ConfigurableProperty(name = "dcmQueryRetrieveViewID")
    private String viewID;

    @ConfigurableProperty(name = "dcmShowInstancesRejectedByCode")
    private Code[] showInstancesRejectedByCodes = {};

    @ConfigurableProperty(name = "dcmHideRejectionNoteWithCode")
    private Code[] hideRejectionNotesWithCodes = {};

    @ConfigurableProperty(name = "dcmHideNotRejectedInstances")
    private boolean hideNotRejectedInstances;

    public final String getViewID() {
        return viewID;
    }

    public final void setViewID(String viewID) {
        this.viewID = viewID;
    }

    public final Code[] getShowInstancesRejectedByCodes() {
        return showInstancesRejectedByCodes;
    }

    public final void setShowInstancesRejectedByCodes(Code... codes) {
        this.showInstancesRejectedByCodes = codes;
    }

    public final Code[] getHideRejectionNotesWithCodes() {
        return hideRejectionNotesWithCodes;
    }

    public final void setHideRejectionNotesWithCodes(Code... codes) {
        this.hideRejectionNotesWithCodes = codes;
    }

    public final boolean isHideNotRejectedInstances() {
        return hideNotRejectedInstances;
    }

    public final void setHideNotRejectedInstances(boolean hideNotRejectedInstances) {
        this.hideNotRejectedInstances = hideNotRejectedInstances;
    }

}
