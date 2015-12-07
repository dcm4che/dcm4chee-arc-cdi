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

package org.dcm4chee.archive.store.scu;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.fetch.forward.FetchForwardService;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
public interface CStoreSCUService {

    void cstore(String messageID, CStoreSCUContext context
            , List<ArchiveInstanceLocator> insts
            , int priority) throws DicomServiceException;

    void scheduleStoreSCU(String messageID, CStoreSCUContext context,
            List<ArchiveInstanceLocator> insts, int retries,
            int priority, long delay);

    /**
     * Coerce each Object to be sent. CStoreSCUContext is used to share state
     * among different coercions
     */
    void coerceAttributes(Attributes attrs, CStoreSCUContext context)
            throws DicomServiceException;

    /**
     * This method is only used by the time zone support decorator The purpose
     * is to be able to apply time zone conversion from the source time zone
     * from the database to the archive time zone before applying the time zone
     * conversion from the archive time zone to the destination time zone
     */
    void coerceFileBeforeMerge(ArchiveInstanceLocator inst, Attributes attrs,
            CStoreSCUContext context) throws DicomServiceException;

    /**
     * Applies template filters on the retrieved instance to remove if undesired
     * according to style sheet
     */
    boolean isInstanceSuppressed(ArchiveInstanceLocator ref,
                                                Attributes attrs, String supressionCriteriaTemplateURI,
                                                CStoreSCUContext context);

    /**
     * Used to eliminate unsupported SOP classes
     */
    boolean isSOPClassSuppressed(ArchiveInstanceLocator ref, CStoreSCUContext context);

    Path getFile(ArchiveInstanceLocator inst) throws IOException;

    FetchForwardService getFetchForwardService();
}
