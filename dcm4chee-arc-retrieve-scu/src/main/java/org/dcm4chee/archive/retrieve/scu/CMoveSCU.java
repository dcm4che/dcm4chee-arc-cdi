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

package org.dcm4chee.archive.retrieve.scu;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.EnumSet;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
public class CMoveSCU {
    
    public enum CmoveReturnState{
        Completed,
        PartiallyCompleted,
        FailedCompletely
    }
    private AAssociateRQ rq;
    private final int priority = 0;
    private ApplicationEntity ae;
    private ApplicationEntity remoteAE;
    private Association as;
    private CmoveReturnState returnState = CmoveReturnState.FailedCompletely;
    private static String[] DEFAULT_TS = { UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndianRetired };

    public CMoveSCU(ApplicationEntity ae, ApplicationEntity remoteAE) {
        this.rq = new AAssociateRQ();
        this.rq.setCalledAET(remoteAE.getAETitle());
        this.rq.setCallingAET(ae.getAETitle());
        this.ae = ae;
        this.remoteAE = remoteAE;
    }

    public Association open(boolean relational) throws IOException, InterruptedException,
            IncompatibleConnectionException, GeneralSecurityException {
        rq.addPresentationContext(new PresentationContext(1, UID.StudyRootQueryRetrieveInformationModelMOVE, DEFAULT_TS));
        if (relational)
            rq.addExtendedNegotiation(new ExtendedNegotiation(UID.StudyRootQueryRetrieveInformationModelMOVE, 
                    QueryOption.toExtendedNegotiationInformation(EnumSet.of(QueryOption.RELATIONAL))));
        return (as = ae.connect(remoteAE, rq));
    }

    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer()) {
            as.waitForOutstandingRSP();
            as.release();
        }
    }

    public void cmove(Attributes keys, DimseRSPHandler handler, String destination)
            throws IOException, InterruptedException {
        as.cmove(UID.StudyRootQueryRetrieveInformationModelMOVE, priority,
                keys, null, destination, handler);
    }

    public AAssociateRQ getRq() {
        return rq;
    }

    public Association getAs() {
        return as;
    }

    public ApplicationEntity getRemoteAE() {
        return remoteAE;
    }

    public ApplicationEntity getAe() {
        return ae;
    }

    public CmoveReturnState getReturnState() {
        return returnState;
    }

    public void setReturnState(CmoveReturnState returnState) {
        this.returnState = returnState;
    }

}
