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

package org.dcm4chee.archive.query;

import com.mysema.query.Tuple;
import com.mysema.query.types.Expression;
import org.dcm4chee.storage.conf.Availability;

import java.util.Date;

/**
 * Calculates the derived fields for a Series.
 *
 * Created by Umberto Cappellini on 6/12/15.
 */
public interface DerivedSeriesFields {

    /**
     * returns the querydsl fields needed to calculate
     * the derived fields. Those fields will be available
     * in the Tuple object of addInstance
     */
    Expression<?>[] fields();

    /**
     * For each instance, the provided querydsl Tuple
     * provides the sets of queried fields necessary to
     * calculate derived fields. Partially calculated
     * fields are stored in the private properties of the
     * bean (note: bean is RequestScoped) and returned
     * by the relative getters.
     */
    void addInstance(Tuple result);

    /**
     * @return number of instances of this Series
     */
    int getNumberOfInstances();

    /**
     * @return retrieve AETs associated to this Series
     */
    String[] getRetrieveAETs();

    /**
     * @return Series availability (ONLINE,NEARLINE,OFFLINE,UNAVAILABLE)
     */
    Availability getAvailability();

    /**
     * @return number of visible images of this Series (proprietary field)
     */
    int getNumberOfVisibleImages();
}
