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
 * Portions created by the Initial Developer are Copyright (C) 2011-2013
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

import java.util.EnumSet;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public interface QueryService {

    QueryContext createQueryContext(QueryRetrieveLevel qrlevel,
            QueryService queryService);

    QueryContext createPatientQueryContext(QueryService queryService);

    QueryContext createStudyQueryContext(QueryService queryService);

    QueryContext createSeriesQueryContext(QueryService queryService);

    QueryContext createInstanceQueryContext(QueryService queryService);

    String[] queryPatientNames(IDWithIssuer[] pids);
    
    Attributes getSeriesAttributes(Long seriesPk, QueryParam queryParam);

    QueryParam getQueryParam(Object source, String sourceAET,
            ArchiveAEExtension aeExt, EnumSet<QueryOption> queryOpts);

    IDWithIssuer[] queryPatientIDs(ArchiveAEExtension aeExt,
            Attributes keys, QueryParam queryParam);

    void adjustMatch(QueryContext query, Attributes match);

    int calculateNumberOfSeriesRelatedInstance(Long seriesPk,
            QueryParam queryParam);

    int calculateNumberOfStudyRelatedSeries(Long studyPk,
            QueryParam queryParam);

    int calculateNumberOfStudyRelatedInstance(Long studyPk,
            QueryParam queryParam);

}
