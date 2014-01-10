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

import java.nio.file.Path;

import javax.persistence.EntityManager;

import org.dcm4che.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.entity.FileSystem;
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

    FileSystem selectFileSystem(StoreSource source, ArchiveAEExtension arcAE)
            throws DicomServiceException;

    StoreContext createStoreContext(StoreSource source,
            ArchiveAEExtension arcAE, FileSystem fs, Path file,
            byte[] digest);

    void parseAttributes(StoreContext storeContext)
            throws DicomServiceException;

    void coerceAttributes(StoreContext storeContext)
            throws DicomServiceException;

    void moveFile(StoreContext storeContext) throws DicomServiceException;

    void updateDB(StoreContext storeContext) throws DicomServiceException;

    Instance findInstance(EntityManager em, StoreContext storeContext)
            throws DicomServiceException;

    Series findSeries(EntityManager em, StoreContext storeContext)
            throws DicomServiceException;

    Study findStudy(EntityManager em, StoreContext storeContext)
            throws DicomServiceException;

    Patient findPatient(EntityManager em, StoreContext storeContext)
            throws DicomServiceException;

    Patient createPatient(EntityManager em, StoreContext storeContext)
            throws DicomServiceException;

    Study createStudy(EntityManager em, StoreContext storeContext,
            Patient patient) throws DicomServiceException;

    Series createSeries(EntityManager em, StoreContext storeContext,
            Study study) throws DicomServiceException;

    Instance createInstance(EntityManager em, StoreContext storeContext,
            Series series) throws DicomServiceException;

    void createFileRef(EntityManager em, StoreContext storeContext,
            Instance instance) throws DicomServiceException;

    StoreDuplicate storeDuplicate(StoreContext storeContext, Instance instance);

    void coerceAttributes(StoreContext storeContext, Patient patient);

    void coerceAttributes(StoreContext storeContext, Study study);

    void coerceAttributes(StoreContext storeContext, Series series);

    void coerceAttributes(StoreContext storeContext, Instance instance);

}
