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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4chee.archive.datamgmt.ejb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

import javax.persistence.EntityNotFoundException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.datamgmt.ejb.DataMgmtEJB.PatientCommands;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

public interface DataMgmtBean {


    Study deleteStudy(String studyInstanceUID) throws Exception;
    Series deleteSeries(String seriesInstanceUID) throws Exception;
    Instance deleteInstance(String sopInstanceUID) throws FileNotFoundException, NoSuchFileException, IOException, Exception;
    
    boolean deleteSeriesIfEmpty(String seriesInstanceUID, String studyInstanceUID);
    boolean deleteStudyIfEmpty(String studyInstanceUID);

    Study getStudy(String studyInstanceUID);
    void updateStudy(ArchiveDeviceExtension arcDevExt, String studyInstanceUID, Attributes attrs) throws EntityNotFoundException;
    void updateSeries(ArchiveDeviceExtension arcDevExt,
            String studyInstanceUID, String seriesInstanceUID, Attributes attrs) throws EntityNotFoundException;
    void updateInstance(ArchiveDeviceExtension arcDevExt,
            String studyInstanceUID, String seriesInstanceUID,
            String sopInstanceUID, Attributes attrs) throws EntityNotFoundException;
    void updatePatient(ArchiveDeviceExtension arcDevExt, IDWithIssuer id,
            Attributes attrs) throws EntityNotFoundException;
    Issuer findOrCreateIssuer(String local, String universal,
            String universalType);
    Issuer getIssuer(String local, String universal,
            String universalType);
    boolean moveStudy(String studyInstanceUID, IDWithIssuer id);
    boolean splitStudy(String studyInstanceUID, String seriesInstanceUID,
            String targetStudyInstanceUID);
    boolean splitSeries(String studyInstanceUID, String seriesInstanceUID,
            String sopInstanceUID, String targetStudyInstanceUID,
            String targetSeriesInstanceUID);
    boolean segmentStudy(String studyInstanceUID, String seriesInstanceUID,
            String targetStudyInstanceUID, ArchiveDeviceExtension arcDevExt);
    boolean patientOperation(Attributes sourcePatientAttributes, Attributes targetPatientAttributes,ApplicationEntity arcAE, PatientCommands command);
}
