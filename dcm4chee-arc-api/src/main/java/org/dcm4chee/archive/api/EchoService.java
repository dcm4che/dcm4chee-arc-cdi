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

import org.dcm4che3.net.Connection;
import org.dcm4che3.net.service.DicomServiceException;

/**
 * Service to verify the communication between two DICOM Application Entities.
 * 
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public interface EchoService {

    public static final String JNDI_NAME = "java:global/org.dcm4chee.archive.api.EchoService";

    /**
     * Verify the connection from the default local AE to an (already
     * configured) remote AE.
     * 
     * If you need to verify the connection to a new (not yet configured) AE,
     * then use one of the other methods in this interface.
     * 
     * @param remoteAETitle
     *            remote AE title (needs to be already configured)
     * @return time needed for the verification in nanoseconds
     * @throws DicomServiceException
     *             if the verification is unsuccessful. Check
     *             {@link DicomServiceException#getStatus()} and
     *             {@link DicomServiceException#getMessage()} for more details.
     */
    public long cecho(String remoteAETitle) throws DicomServiceException;

    /**
     * Verify the connection from a local AE to an (already configured) remote
     * AE.
     * 
     * If you need to verify the connection to a new (not yet configured) AE,
     * then use one of the other methods in this interface.
     * 
     * @param localAETitle
     *            local AE title
     * @param remoteAETitle
     *            remote AE title (needs to be already configured)
     * @return time needed for the verification in nanoseconds
     * @throws DicomServiceException
     *             if the verification is unsuccessful. Check
     *             {@link DicomServiceException#getStatus()} and
     *             {@link DicomServiceException#getMessage()} for more details.
     */
    public long cecho(String localAETitle, String remoteAETitle) throws DicomServiceException;

    /**
     * Verify the connection from the default local AE to a new (not yet
     * configured) remote AE.
     * 
     * @param remoteAETitle
     *            remote AE title
     * @param remoteConnection
     *            connection to the remote AE
     * @return time needed for the verification in nanoseconds
     * @throws DicomServiceException
     *             if the verification is unsuccessful. Check
     *             {@link DicomServiceException#getStatus()} and
     *             {@link DicomServiceException#getMessage()} for more details.
     */
    public long cecho(String remoteAETitle, Connection remoteConnection) throws DicomServiceException;

    /**
     * Verify the connection from a local AE to a new (not yet configured)
     * remote AE.
     * 
     * @param localAETitle
     *            local AE title
     * @param remoteAETitle
     *            remote AE title
     * @param remoteConnection
     *            connection to the remote AE
     * @return time needed for the verification in nanoseconds
     * @throws DicomServiceException
     *             if the verification is unsuccessful. Check
     *             {@link DicomServiceException#getStatus()} and
     *             {@link DicomServiceException#getMessage()} for more details.
     */
    public long cecho(String localAETitle, String remoteAETitle, Connection remoteConnection) throws DicomServiceException;

    /**
     * Shortcut for {@link #cecho(String, Connection)} which will create a
     * default configured remote connection for you.
     * 
     * @param remoteAETitle
     *            remote AE title
     * @param remoteHostname
     *            remote host name
     * @param remotePort
     *            a DICOM port
     * @return time needed for the verification in nanoseconds
     * @throws DicomServiceException
     *             if the verification is unsuccessful. Check
     *             {@link DicomServiceException#getStatus()} and
     *             {@link DicomServiceException#getMessage()} for more details.
     */
    public long cecho(String remoteAETitle, String remoteHostname, int remotePort) throws DicomServiceException;

}
