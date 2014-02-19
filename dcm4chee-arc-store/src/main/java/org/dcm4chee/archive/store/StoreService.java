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

package org.dcm4chee.archive.store;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.persistence.EntityManager;

import org.dcm4che.data.Attributes;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public interface StoreService {

    int DATA_SET_NOT_PARSEABLE = 0xC900;

    StoreSession initStoreSession(String name, StoreService storeService,
            String sourceAET, ArchiveAEExtension arcAE) throws DicomServiceException;

    StoreContext initStoreContext(StoreSession session, Attributes fmi,
            InputStream data) throws DicomServiceException;

    StoreContext initStoreContext(StoreSession session, Attributes fmi,
            Attributes attrs) throws DicomServiceException;

    void onClose(StoreSession session);

    void store(StoreContext context) throws DicomServiceException;

    Path spool(StoreSession session, InputStream in, String suffix)
            throws IOException;

    void coerceAttributes(StoreContext context) throws DicomServiceException;

    void processFile(StoreContext context) throws DicomServiceException;

    Path calcStorePath(StoreContext context);

    void updateDB(StoreContext context) throws DicomServiceException;

    void updateDB(EntityManager em, StoreContext context)
            throws DicomServiceException;

    Instance findOrCreateInstance(EntityManager em, StoreContext context)
            throws DicomServiceException;

    Series findOrCreateSeries(EntityManager em, StoreContext context)
            throws DicomServiceException;

    Study findOrCreateStudy(EntityManager em, StoreContext context)
            throws DicomServiceException;

    Patient findOrCreatePatient(EntityManager em, StoreContext context)
            throws DicomServiceException;

    StoreAction instanceExists(EntityManager em, StoreContext context,
            Instance instance) throws DicomServiceException;

    Instance createInstance(EntityManager em, StoreContext context)
            throws DicomServiceException;

    Series createSeries(EntityManager em, StoreContext context)
            throws DicomServiceException;

    Study createStudy(EntityManager em, StoreContext context)
            throws DicomServiceException;

    Patient createPatient(EntityManager em, StoreContext context)
            throws DicomServiceException;

    void updateInstance(EntityManager em, StoreContext context, Instance inst)
            throws DicomServiceException;

    void updateSeries(EntityManager em, StoreContext context, Series series)
            throws DicomServiceException;

    void updateStudy(EntityManager em, StoreContext context, Study study)
            throws DicomServiceException;

    void updatePatient(EntityManager em, StoreContext context, Patient patient);

    void updateCoercedAttributes(StoreContext context);

    void cleanup(StoreContext context);

    void fireStoreEvent(StoreContext context);

}
