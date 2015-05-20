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

package org.dcm4chee.archive.retrieve.scu.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.Status;
import org.dcm4chee.archive.retrieve.scu.CMoveSCU;
import org.dcm4chee.archive.retrieve.scu.CMoveSCU.CmoveReturnState;
import org.dcm4chee.archive.retrieve.scu.CMoveSCUService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
@ApplicationScoped
public class CMoveSCUServiceImpl implements CMoveSCUService{

    private static final Logger LOG = LoggerFactory.getLogger(CMoveSCU.class);
    private CMoveSCU scu;

    @Override
    public CmoveReturnState cmove(ApplicationEntity localAE, ApplicationEntity remoteAE,
            Attributes keys, final DimseRSPHandler handler, String destinationAET, final int instancesToMove, 
            boolean relational) { 
        scu = new CMoveSCU(localAE, remoteAE);
        try {
            Association as = scu.open(relational);
            scu.cmove(keys, new DimseRSPHandler(as.nextMessageID()) {
                public void onDimseRSP(Association as, Attributes cmd,
                        Attributes data) {
                    if(cmd.getInt(Tag.Status, -1) != Status.Pending 
                            && cmd.getInt(Tag.Status, -1) != Status.PendingWarning){
                    if(cmd.getInt(Tag.NumberOfCompletedSuboperations, -1) > 0
                            || cmd.getInt(Tag.NumberOfWarningSuboperations, -1) > 0) {
                        if(cmd.getInt(Tag.NumberOfFailedSuboperations, -1) >= 0) {
                            scu.setReturnState(CmoveReturnState.PartiallyCompleted);
                        }
                        else {
                            scu.setReturnState(CmoveReturnState.Completed);
                        }
                    }
                }
                    if(handler!=null) {
                        handler.onDimseRSP(as, cmd, data);
                    }
                    else {
                        super.onDimseRSP(as, cmd, data);
                    }
                };
            }, destinationAET);
        } catch (Exception e) {
            LOG.error(
                    "Unable to cmove to selected destination {} from {} - Exception {}",
                    remoteAE.getAETitle(), localAE.getAETitle(), e.getMessage());
        }
        finally{
            try {
                scu.close();
            } catch (Exception e) {
                if(!scu.getAs().isReadyForDataTransfer())
                    LOG.error("Unable to close association {}, already closed", scu.getAs());
                else
                    LOG.error(
                            "General exception closing association {} - Exception {}",
                            scu.getAs(), e.getMessage());
            }
        }
        return scu.getReturnState();
    }

    @Override
    public CmoveReturnState moveStudy(ApplicationEntity localAE,
            String studyInstanceUID,
            int instancesInStudy,
            DimseRSPHandler handler, List<ApplicationEntity> possibleLocations,
            String destination) {
        Attributes keys  = new Attributes();
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, "STUDY");
        keys.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUID);
        for(ApplicationEntity remoteAE : possibleLocations) {
            scu = new CMoveSCU(localAE, remoteAE);
            CmoveReturnState state = cmove(localAE, remoteAE, keys, handler, destination, instancesInStudy, false);
            switch (state) {
            case Completed:
                return CmoveReturnState.Completed;

            default:
                break;
            }
        }
        return scu.getReturnState();
    }

}
