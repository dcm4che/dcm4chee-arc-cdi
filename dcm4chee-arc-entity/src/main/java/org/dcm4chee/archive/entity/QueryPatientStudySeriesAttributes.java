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
package org.dcm4chee.archive.entity;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class QueryPatientStudySeriesAttributes extends PatientStudySeriesAttributes {
    
    private final Long studyPk;
    private final int[] numberOfStudyRelatedSeries;
    private final int[] numberOfStudyRelatedInstances;
    private final int[] numberOfSeriesRelatedInstances;
    private final String modalitiesInStudy;
    private final String sopClassesInStudy;

    public QueryPatientStudySeriesAttributes(Long studyPk,
            int numberOfStudyRelatedSeries1,
            int numberOfStudyRelatedSeries2,
            int numberOfStudyRelatedSeries3,
            int numberOfStudyRelatedInstances1,
            int numberOfStudyRelatedInstances2,
            int numberOfStudyRelatedInstances3,
            int numberOfSeriesRelatedInstances1,
            int numberOfSeriesRelatedInstances2,
            int numberOfSeriesRelatedInstances3,
            String modalitiesInStudy,
            String sopClassesInStudy,
            byte[] seriesAttributes,
            byte[] studyAttributes,
            byte[] patientAttributes) {
        super(seriesAttributes, studyAttributes, patientAttributes);
        this.studyPk = studyPk;
        this.numberOfStudyRelatedSeries = new int[] {
                numberOfStudyRelatedSeries1,
                numberOfStudyRelatedSeries2,
                numberOfStudyRelatedSeries3 };
        this.numberOfStudyRelatedInstances = new int[] {
                numberOfStudyRelatedInstances1,
                numberOfStudyRelatedInstances2,
                numberOfStudyRelatedInstances3 };
        this.numberOfSeriesRelatedInstances = new int[] {
                numberOfSeriesRelatedInstances1,
                numberOfSeriesRelatedInstances2,
                numberOfSeriesRelatedInstances3 };
        this.modalitiesInStudy = modalitiesInStudy;
        this.sopClassesInStudy = sopClassesInStudy;
    }

    public final Long getStudyPk() {
        return studyPk;
    }

    public int getNumberOfStudyRelatedSeries(int slot) {
        return slot <= 0 ? -1 : numberOfStudyRelatedSeries[slot-1];
    }

    public int getNumberOfStudyRelatedInstances(int slot) {
        return slot <= 0 ? -1 : numberOfStudyRelatedInstances[slot-1];
    }

    public int getNumberOfSeriesRelatedInstances(int slot) {
        return slot <= 0 ? -1 : numberOfSeriesRelatedInstances[slot-1];
    }

    public String getModalitiesInStudy() {
        return modalitiesInStudy;
    }

    public String getSopClassesInStudy() {
        return sopClassesInStudy;
    }
}
