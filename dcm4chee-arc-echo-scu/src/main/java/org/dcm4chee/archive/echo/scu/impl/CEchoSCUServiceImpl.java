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

package org.dcm4chee.archive.echo.scu.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.CompatibleConnection;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSP;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.echo.scu.CEchoSCUService;

/**
 * Implementation of {@link CEchoSCUService}.
 * 
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
@ApplicationScoped
public class CEchoSCUServiceImpl implements CEchoSCUService {

    @Inject
    private Device device;

    @Inject
    private IApplicationEntityCache aeCache;

    @Override
    public long cecho(String remoteAETitle) throws DicomServiceException {
        return cecho(getDefaultLocalAETitle(), remoteAETitle);
    }


    @Override
    public long cecho(String remoteAETitle, Connection remoteConnection) throws DicomServiceException {
        return cecho(getDefaultLocalAETitle(), remoteAETitle, remoteConnection);
    }

    private String getDefaultLocalAETitle() {
        ArchiveDeviceExtension archiveDevice = device.getDeviceExtension(ArchiveDeviceExtension.class);
        return archiveDevice.getDefaultAETitle();
    }

    @Override
    public long cecho(String localAETitle, String remoteAETitle) throws DicomServiceException {
        ApplicationEntity localAE = lookupLocalAE(localAETitle);
        ApplicationEntity remoteAE = lookupRemoteAE(remoteAETitle);

        return cecho(localAE, remoteAE);
    }

    private long cecho(ApplicationEntity localAE, ApplicationEntity remoteAE) throws DicomServiceException {
        CompatibleConnection compatibleConnection;
        try {
            compatibleConnection = localAE.findCompatibelConnection(remoteAE);
        } catch (IncompatibleConnectionException e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }

        return cecho(localAE, remoteAE.getAETitle(), compatibleConnection);
    }

    @Override
    public long cecho(String localAETitle, String remoteAETitle, Connection remoteConnection) throws DicomServiceException {
        ApplicationEntity localAE = lookupLocalAE(localAETitle);

        return cecho(localAE, remoteAETitle, remoteConnection);
    }

    private long cecho(ApplicationEntity localAE, String remoteAETitle, Connection remoteConnection) throws DicomServiceException {
        Connection localConnection;
        try {
            localConnection = localAE.findCompatibelConnection(remoteConnection);
        } catch (IncompatibleConnectionException e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
        CompatibleConnection compatibleConnection = new CompatibleConnection(localConnection, remoteConnection);

        return cecho(localAE, remoteAETitle, compatibleConnection);
    }

    private ApplicationEntity lookupLocalAE(String localAETitle) throws DicomServiceException {
        ApplicationEntity localAE = device.getApplicationEntity(localAETitle);
        if (localAE == null)
            throw new DicomServiceException(Status.ProcessingFailure, "Local AE " + localAETitle + " unknown.");
        return localAE;
    }

    private ApplicationEntity lookupRemoteAE(String remoteAETitle) throws DicomServiceException {
        try {
            return aeCache.findApplicationEntity(remoteAETitle);
        } catch (ConfigurationException e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }

    private long cecho(ApplicationEntity localAE, String remoteAETitle, CompatibleConnection compatibleConnection) throws DicomServiceException {
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.setCallingAET(localAE.getAETitle());
        aarq.setCalledAET(remoteAETitle);

        aarq.addPresentationContextFor(UID.VerificationSOPClass, UID.ImplicitVRLittleEndian);
        aarq.addPresentationContextFor(UID.VerificationSOPClass, UID.ExplicitVRLittleEndian);
        aarq.addPresentationContextFor(UID.VerificationSOPClass, UID.ExplicitVRBigEndianRetired);

        long time = System.nanoTime();

        int status;
        try {
            Association association = localAE.connect(compatibleConnection.getLocalConnection(), compatibleConnection.getRemoteConnection(), aarq);
            try {
                DimseRSP response = association.cecho();
                response.next();
                Attributes cmd = response.getCommand();
                status = cmd.getInt(Tag.Status, -1);
            } finally {
                association.release();
            }
        } catch (IOException | InterruptedException | IncompatibleConnectionException | GeneralSecurityException e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }

        time = System.nanoTime() - time;

        if (status == Status.Success)
            return time;

        throw new DicomServiceException(status);
    }


}
