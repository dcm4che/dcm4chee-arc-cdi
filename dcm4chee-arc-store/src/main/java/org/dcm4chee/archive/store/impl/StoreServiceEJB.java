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

package org.dcm4chee.archive.store.impl;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4che.data.Attributes;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
@Stateless
public class StoreServiceEJB {

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    public void updateDB(StoreContext storeContext)
            throws DicomServiceException {

        StoreService storeService = storeContext.getService();

        Instance instance = storeService.findInstance(em, storeContext);
        if (instance != null
                && !storeService.replaceInstance(em, storeContext, instance))
            return;

        Patient patient = createOrUpdatePatient(storeContext);
        Study study = createOrUpdateStudy(storeContext, patient);
        Series series = createOrUpdateSeries(storeContext, study);

        instance = storeService.createInstance(em, storeContext, series);
        storeService.createFileRef(em, storeContext, instance);
    }

    private Patient createOrUpdatePatient(StoreContext storeContext)
            throws DicomServiceException {
        
        Patient patient = storeContext.getService().findPatient(em,
                storeContext);

        if (patient == null) {
            patient = storeContext.getService().createPatient(em, storeContext);
        } else {
            storeContext.getService().updatePatient(storeContext, patient);
            storeContext.getAttributes().update(patient.getAttributes(),
                    storeContext.getCoercedAttributes());
        }

        return patient;
    }

    private Study createOrUpdateStudy(StoreContext storeContext, Patient patient)
            throws DicomServiceException {
        
        Study study = storeContext.getService().findStudy(em, storeContext);

        if (study == null) {
            study = storeContext.getService().createStudy(em, storeContext,patient);
        } else {
            storeContext.getService().updateStudy(storeContext, study);
            storeContext.getAttributes().update(study.getAttributes(),
                    storeContext.getCoercedAttributes());
        }

        return study;
    }

    private Series createOrUpdateSeries(StoreContext storeContext, Study study)
            throws DicomServiceException {
        
        Series series = storeContext.getService().findSeries(em, storeContext);
        
        if (series == null) {
            series = storeContext.getService().createSeries(em, storeContext,study);
        } else {
            storeContext.getService().updateSeries(storeContext, series);
            storeContext.getAttributes().update(series.getAttributes(),
                    storeContext.getCoercedAttributes());
        }
        return series;
    }
}
