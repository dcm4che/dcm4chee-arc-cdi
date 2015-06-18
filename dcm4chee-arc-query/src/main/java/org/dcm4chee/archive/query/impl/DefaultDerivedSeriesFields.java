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

package org.dcm4chee.archive.query.impl;

import com.mysema.query.Tuple;
import com.mysema.query.types.Expression;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.QSeries;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.query.DerivedSeriesFields;
import org.dcm4chee.storage.conf.Availability;

import javax.enterprise.context.RequestScoped;

/**
 * Created by Umberto Cappellini on 6/12/15.
 */
@RequestScoped
public class DefaultDerivedSeriesFields implements DerivedSeriesFields {

    protected int numberOfInstances;
    protected String[] retrieveAETs;
    protected Availability availability;
    private int numberOfVisibleImages;

    @Override
    public Expression<?>[] fields() {
        return new Expression<?>[]{
                QInstance.instance.retrieveAETs,
                QInstance.instance.availability
        };
    }

    @Override
    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    @Override
    public String[] getRetrieveAETs() {
        return retrieveAETs;
    }

    @Override
    public Availability getAvailability() {
        return availability;
    }

    @Override
    public int getNumberOfVisibleImages() {
        return numberOfVisibleImages;
    }

    @Override
    public void addInstance(Tuple result) {
        String[] retrieveAETs1 = StringUtils.split(
                result.get(QInstance.instance.retrieveAETs),
                '\\');
        Availability availability1 =
                result.get(QInstance.instance.availability);
        if (numberOfInstances++ == 0) {
            retrieveAETs = retrieveAETs1;
            availability = availability1;
        } else {
            retrieveAETs = Utils.intersection(
                    retrieveAETs, retrieveAETs1);
            if (availability.compareTo(availability1) < 0)
                availability = availability1;
        }
    }

}
