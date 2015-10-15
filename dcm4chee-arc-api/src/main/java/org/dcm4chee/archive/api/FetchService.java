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

package org.dcm4chee.archive.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Future;

/**
 * Service to either retrieve or trigger a retrieve of instances from storage (regardless of storage location) 
 * at Study, Series or Instance level.
 *
 */
public interface FetchService {
    
    public static final String JNDI_NAME = "java:global/org.dcm4chee.archive.api.FetchService";

    /**
     * Asynchronously retrieve a study, regardless of where it is located.
     * @param Study instance UID
     * @return A future that will be fulfilled when the retrieve is complete.
     * The string value of the future will be the requested studyUID.
     */
    public Future<String> fetchStudyAsync(String studyUID) throws IOException;

    /**
     * Asynchronously retrieve a series, regardless of where it is located.
     * @param Series instance UID
     * @return A future that will be fulfilled when the retrieve is complete.
     * The string value of the future will be the requested seriesUID.
     */
    public Future<String> fetchSeriesAsync(String seriesUID) throws IOException;

    /**
     * Asynchronously retrieve an instance, regardless of where it is located.
     * @param SOP instance UID
     * @return A future that will be fulfilled when the retrieve is complete. The future will then contain
     * the Path value for where the instance can be accessed.
     */
    public Future<Path> fetchInstanceAsync(String sopIUID) throws IOException;
    
}