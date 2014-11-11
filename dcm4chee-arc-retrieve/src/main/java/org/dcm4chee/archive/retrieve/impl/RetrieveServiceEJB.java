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

package org.dcm4chee.archive.retrieve.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.entity.FileRef;
import org.dcm4chee.archive.entity.PatientStudySeriesAttributes;
import org.dcm4chee.archive.entity.QFileRef;
import org.dcm4chee.archive.entity.QFileSystem;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.QPatient;
import org.dcm4chee.archive.entity.QSeries;
import org.dcm4chee.archive.entity.QStudy;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.query.util.QueryBuilder;
import org.hibernate.Session;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.hibernate.HibernateQuery;
import com.mysema.query.types.Expression;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Stateless
public class RetrieveServiceEJB {

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    static final QFileRef filealiastableref = new QFileRef("filealiastableref");
    static final QFileSystem filealiastablereffilesystem = new QFileSystem("filealiastablereffilesystem");

    public List<Tuple> query(Expression<?>[] select,
            IDWithIssuer[] pids, String[] studyIUIDs, String[] seriesIUIDs,
            String[] objectIUIDs, QueryParam queryParam) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QueryBuilder.pids(pids, 
                queryParam.isMatchLinkedPatientIDs(), false));
        builder.and(QueryBuilder.uids(QStudy.study.studyInstanceUID,
                studyIUIDs, false));
        builder.and(QueryBuilder.uids(QSeries.series.seriesInstanceUID,
                seriesIUIDs, false));
        builder.and(QueryBuilder.uids(QInstance.instance.sopInstanceUID,
                objectIUIDs, false));

        builder.and(QFileRef.fileRef.status.ne(FileRef.Status.REPLACED));
        builder.and(QueryBuilder.hideRejectedInstance(queryParam));
        builder.and(QueryBuilder.hideRejectionNote(queryParam));

        List<Tuple> query = new HibernateQuery(em.unwrap(Session.class))
                .from(QInstance.instance)
                .leftJoin(QInstance.instance.fileRefs, QFileRef.fileRef)
                .leftJoin(QFileRef.fileRef.fileSystem, QFileSystem.fileSystem)
                .leftJoin(QInstance.instance.fileAliasTableRefs, filealiastableref)
                .leftJoin(filealiastableref.fileSystem,filealiastablereffilesystem )
                .innerJoin(QInstance.instance.attributesBlob, QueryBuilder.instanceAttributesBlob)
                .innerJoin(QInstance.instance.series, QSeries.series)
                .innerJoin(QSeries.series.study, QStudy.study)
                .innerJoin(QStudy.study.patient, QPatient.patient)
                .orderBy(QInstance.instance.pk.asc())
                .where(builder)
                .list(select);

        return query;
    }

    public Attributes getSeriesAttributes(Long seriesPk) {
        PatientStudySeriesAttributes result = (PatientStudySeriesAttributes) em
                .createNamedQuery(Series.PATIENT_STUDY_SERIES_ATTRIBUTES)
                .setParameter(1, seriesPk).getSingleResult();
        return result.getAttributes();
    }

}
