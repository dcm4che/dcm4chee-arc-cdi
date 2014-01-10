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

package org.dcm4chee.archive.patient.impl;

import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.dcm4che.data.Attributes;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.IDWithIssuer;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.issuer.IssuerService;
import org.dcm4chee.archive.patient.PatientService;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Stateless
public class PatientServiceEJB implements PatientService {

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    @Inject
    private IssuerService issuerService;

    @Override
    public Patient findPatientOnStorage(Attributes attrs) {
        IDWithIssuer pid = IDWithIssuer.fromPatientIDWithIssuer(attrs);
        if (pid == null)
            return null;

        List<Patient> list = findPatients(pid);
        if (list.size() != 1)
            return null;

        return followMergedWith(list.get(0));
    }

    private List<Patient> findPatients(IDWithIssuer pid) {
        TypedQuery<Patient> query = em.createNamedQuery(
                    Patient.FIND_BY_PATIENT_ID, Patient.class)
                .setParameter(1, pid.getID());
        List<Patient> list = query.getResultList();
        Issuer issuer = pid.getIssuer();
        if (issuer != null) {
            for (Iterator<Patient> it = list.iterator(); it.hasNext();) {
                Patient pat = (Patient) it.next();
                Issuer issuer2 = pat.getIssuerOfPatientID();
                if (issuer2 != null) {
                    if (issuer2.matches(issuer)) {
                        list.clear();
                        list.add(pat);
                        break;
                    } else
                        it.remove();
                }
            }
        }
        return list;
    }

    private Patient followMergedWith(Patient patient) {
        Patient mergedWith;
        while ((mergedWith = patient.getMergedWith()) != null) {
            patient = mergedWith;
        }
        return patient;
    }

    @Override
    public Patient createPatientOnStorage(Attributes attrs,
            StoreParam storeParam) {
        IDWithIssuer pid = IDWithIssuer.fromPatientIDWithIssuer(attrs);
        Patient patient = new Patient();
        if (pid != null && pid.getIssuer() != null)
            patient.setIssuerOfPatientID(
                    issuerService.findOrCreate(pid.getIssuer()));
        patient.setAttributes(attrs,
                storeParam.getAttributeFilter(Entity.Patient),
                storeParam.getFuzzyStr());
        em.persist(patient);
        return patient;
    }
}
