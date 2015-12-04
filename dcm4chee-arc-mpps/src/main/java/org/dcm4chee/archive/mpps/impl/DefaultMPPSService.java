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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4chee.archive.mpps.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.*;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.entity.*;
import org.dcm4chee.archive.hooks.AttributeCoercionHook;
import org.dcm4chee.archive.mpps.MPPSContext;
import org.dcm4chee.archive.mpps.MPPSForwardService;
import org.dcm4chee.archive.mpps.MPPSHook;
import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.archive.mpps.event.MPPSCreate;
import org.dcm4chee.archive.mpps.event.MPPSEvent;
import org.dcm4chee.archive.mpps.event.MPPSFinal;
import org.dcm4chee.archive.mpps.event.MPPSUpdate;
import org.dcm4chee.util.TransactionSynchronization;
import org.dcm4chee.hooks.Hooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Default implementation of {@link MPPSService}.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Roman K
 */
@Stateless
public class DefaultMPPSService implements MPPSService {

    private static Logger LOG = LoggerFactory.getLogger(DefaultMPPSService.class);

    @Inject
    @MPPSCreate
    private Event<MPPSEvent> createMPPSEvent;

    @Inject
    @MPPSUpdate
    private Event<MPPSEvent> updateMPPSEvent;

    @Inject
    @MPPSFinal
    private Event<MPPSEvent> finalMPPSEvent;

    @Inject
    private Device device;

    @Inject
    private TransactionSynchronization transaction;

    /**
     * We need to move coercion out of the interface to remove this 'self' injection
     */
    @EJB
    private MPPSService mppsService;

    @Inject
    private Hooks<MPPSHook> mppsHooks;

    @Inject
    private MPPSForwardService mppsForwardService;

    @Override
    public void createPerformedProcedureStep(final Attributes attrs, final MPPSContext mppsContext) throws DicomServiceException {
        coerceAttributes(mppsContext, attrs);

        for (MPPSHook mppsHook : mppsHooks) {
            mppsHook.onMPPSCreate(mppsContext, attrs);
        }

        final MPPSEvent mppsEvent = new MPPSEvent(attrs, mppsContext);

        mppsForwardService.scheduleForwardMPPS(mppsEvent);

        transaction.afterSuccessfulCommit(new Runnable() {
            @Override
            public void run() {
                createMPPSEvent.fire(mppsEvent);
            }
        });
    }

    @Override
    public void updatePerformedProcedureStep(final Attributes attrs, final MPPSContext mppsContext) throws DicomServiceException {

        coerceAttributes(mppsContext, attrs);

        for (MPPSHook mppsHook : mppsHooks) {
            mppsHook.onMPPSUpdate(mppsContext, attrs);
        }

        final MPPSEvent mppsEvent = new MPPSEvent(attrs, mppsContext);

        mppsForwardService.scheduleForwardMPPS(mppsEvent);

        final MPPS.Status mppsStatus = MPPS.getMPPSStatus(attrs);
        transaction.afterSuccessfulCommit(new Runnable() {
            @Override
            public void run() {
                if (mppsStatus == MPPS.Status.IN_PROGRESS)
                    updateMPPSEvent.fire(mppsEvent);
                else
                    finalMPPSEvent.fire(mppsEvent);
            }
        });
    }

    private void coerceAttributes(MPPSContext context, Attributes attrs) throws DicomServiceException {
        // call coercers
        for (AttributeCoercionHook<MPPSContext> attributeCoercionHook : mppsHooks) {
            attributeCoercionHook.coerceAttributes(context, attrs);
        }
    }

    @Deprecated
    @Override
    public void createPerformedProcedureStep(final String mppsSopInstanceUID, final Attributes attrs, final MPPSContext mppsContext) throws DicomServiceException {
        mppsService.createPerformedProcedureStep(attrs, mppsContext);
    }

    @Deprecated
    @Override
    public void updatePerformedProcedureStep(final String mppsSopInstanceUID, final Attributes attrs, final MPPSContext mppsContext) throws DicomServiceException {
        mppsService.updatePerformedProcedureStep(attrs, mppsContext);
    }

    @Deprecated
    public void coerceAttributes(MPPSContext context, Dimse dimse, Attributes attrs) throws DicomServiceException {
        //noop - this is not called anymore
    }


}
