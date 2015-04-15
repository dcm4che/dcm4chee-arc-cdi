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

package org.dcm4chee.archive.store.scu;

import java.util.List;

import org.dcm4che3.net.service.BasicCStoreSCUResp;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 *
 */
public class CStoreSCUResponse extends BasicCStoreSCUResp {

    private String messageID;
    private List<ArchiveInstanceLocator> insts;
    private String localAET, remoteAET;

    public CStoreSCUResponse(BasicCStoreSCUResp basic,
            List<ArchiveInstanceLocator> insts, String messageID,
            String localAET, String remoteAET) {
        this.setCompleted(basic.getCompleted());
        this.setFailed(basic.getFailed());
        this.setFailedUIDs(basic.getFailedUIDs());
        this.setStatus(basic.getStatus());
        this.setWarning(basic.getWarning());
        this.setMessageID(messageID);
        this.setInstances(insts);
        this.setLocalAET(localAET);
        this.setRemoteAET(remoteAET);
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public List<ArchiveInstanceLocator> getInstances() {
        return insts;
    }

    public void setInstances(List<ArchiveInstanceLocator> insts) {
        this.insts = insts;
    }

    public String getLocalAET() {
        return localAET;
    }

    public void setLocalAET(String localAET) {
        this.localAET = localAET;
    }

    public String getRemoteAET() {
        return remoteAET;
    }

    public void setRemoteAET(String remoteAET) {
        this.remoteAET = remoteAET;
    }
}
