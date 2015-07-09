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

package org.dcm4chee.archive.conf;

import org.dcm4che3.data.Issuer;
import org.dcm4che3.soundex.FuzzyStr;

import java.util.Map;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class QueryParam {

    private FuzzyStr fuzzyStr;
    private Map<Entity, AttributeFilter> attributeFilters;
    private boolean combinedDatetimeMatching;
    private boolean fuzzySemanticMatching;
    private boolean personNameComponentOrderInsensitiveMatching;
    private boolean matchUnknown;
    private boolean matchLinkedPatientIDs;
    private String[] accessControlIDs;
    private Issuer defaultIssuerOfPatientID;
    private Issuer defaultIssuerOfAccessionNumber;
    private boolean deIdentifyLogs = false;
    private String[] visibleImageSRClasses;
    private QueryRetrieveView queryRetrieveView;

    public final boolean isCombinedDatetimeMatching() {
        return combinedDatetimeMatching;
    }

    public final void setCombinedDatetimeMatching(boolean combinedDatetimeMatching) {
        this.combinedDatetimeMatching = combinedDatetimeMatching;
    }

    public final boolean isFuzzySemanticMatching() {
        return fuzzySemanticMatching;
    }

    public final void setFuzzySemanticMatching(boolean fuzzySemanticMatching) {
        this.fuzzySemanticMatching = fuzzySemanticMatching;
    }

    public final boolean isPersonNameComponentOrderInsensitiveMatching() {
        return personNameComponentOrderInsensitiveMatching;
    }

    public final void setPersonNameComponentOrderInsensitiveMatching(
            boolean orderInsensitiveMatching) {
        this.personNameComponentOrderInsensitiveMatching = orderInsensitiveMatching;
    }

    public final boolean isMatchUnknown() {
        return matchUnknown;
    }

    public final void setMatchUnknown(boolean matchUnknown) {
        this.matchUnknown = matchUnknown;
    }

    public final boolean isMatchLinkedPatientIDs() {
        return this.matchLinkedPatientIDs;
    }

    public final void setMatchLinkedPatientIDs(boolean matchLinkedPatientIDs) {
        this.matchLinkedPatientIDs = matchLinkedPatientIDs;
    }

    public final String[] getAccessControlIDs() {
        return accessControlIDs;
    }

    public final void setAccessControlIDs(String[] accessControlIDs) {
        this.accessControlIDs = accessControlIDs;
    }

    public final void setFuzzyStr(FuzzyStr fuzzyStr) {
        this.fuzzyStr = fuzzyStr;
    }

    public final FuzzyStr getFuzzyStr() {
        return fuzzyStr;
    }

    public void setAttributeFilters(Map<Entity, AttributeFilter> attributeFilters) {
        this.attributeFilters = attributeFilters;
    }

    public final AttributeFilter getAttributeFilter(Entity entity) {
        return attributeFilters.get(entity);
    }

    public Issuer getDefaultIssuerOfPatientID() {
        return defaultIssuerOfPatientID;
    }

    public void setDefaultIssuerOfPatientID(Issuer issuer) {
        this.defaultIssuerOfPatientID = issuer;
    }

    public Issuer getDefaultIssuerOfAccessionNumber() {
        return defaultIssuerOfAccessionNumber;
    }

    public void setDefaultIssuerOfAccessionNumber(Issuer issuer) {
        this.defaultIssuerOfAccessionNumber = issuer;
    }
    
    public boolean isDeIdentifyLogs() {
        return deIdentifyLogs;
    }

    public void setDeIdentifyLogs(boolean deIdentifyLogs) {
        this.deIdentifyLogs = deIdentifyLogs;
    }

    public String[] getVisibleImageSRClasses() {
        return visibleImageSRClasses;
    }

    public void setVisibleImageSRClasses(String[] visibleImageSRClasses) {
        this.visibleImageSRClasses = visibleImageSRClasses;
    }


    public QueryRetrieveView getQueryRetrieveView() {
        return queryRetrieveView;
    }

    public void setQueryRetrieveView(QueryRetrieveView queryRetrieveView) {
        this.queryRetrieveView = queryRetrieveView;
    }

}
