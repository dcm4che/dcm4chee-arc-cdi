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

package org.dcm4che.arc.api.impl;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.dcm4che.arc.api.FileAccess;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.conf.QueryRetrieveView;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;

/**
 * Implementation of {@link FileAccess}.
 * 
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 */
@EJB(name = FileAccess.JNDI_NAME, beanInterface = FileAccess.class)
@Stateless
@Local(FileAccess.class)
public class DefaultFileAccess implements FileAccess {

    @Inject
    private  org.dcm4chee.archive.retrieve.RetrieveService archiveRetrieveService;
    
    @Inject
    private org.dcm4chee.storage.service.RetrieveService storageRetrieveService;

    @Override
    public List<Path> getStudy (String uid) throws IOException {

        List<ArchiveInstanceLocator> ails = 
                archiveRetrieveService.calculateMatches(uid, null, null, param(), false);
        
        if (ails == null || ails.size() == 0)
            return null;
        
        List<Path> res = new ArrayList<Path>();
        
        for (ArchiveInstanceLocator ail : ails)
            res.add(getFile(ail));
        
        return res;
    }

    @Override
    public List<Path> getSeries (String uid) throws IOException {

        List<ArchiveInstanceLocator> ails = 
                archiveRetrieveService.calculateMatches(null, uid, null, param(), false);
        
        if (ails == null || ails.size() == 0)
            return null;
        
        List<Path> res = new ArrayList<Path>();
        
        for (ArchiveInstanceLocator ail : ails)
            res.add(getFile(ail));
        
        return res;
    }

    @Override
    public Path getInstance (String uid) throws IOException {

        List<ArchiveInstanceLocator> ails = 
                archiveRetrieveService.calculateMatches(null, null, uid, param(), false);
        
        if (ails == null || ails.size() == 0)
            return null;
        
        return getFile(ails.get(0));
    }
    
    private QueryParam param() {
        QueryParam param = new QueryParam();
        param.setMatchLinkedPatientIDs(false);
        param.setQueryRetrieveView(new QueryRetrieveView());
        return param;
    }
    
    private Path getFile(ArchiveInstanceLocator inst) throws IOException {

        ArchiveInstanceLocator archInst = (ArchiveInstanceLocator) inst;

        org.dcm4chee.storage.RetrieveContext ctx = storageRetrieveService
                .createRetrieveContext(archInst.getStorageSystem());
        try {
            return archInst.getEntryName() == null ? storageRetrieveService
                    .getFile(ctx, archInst.getFilePath())
                    : storageRetrieveService.getFile(ctx,
                            archInst.getFilePath(), archInst.getEntryName());
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
    }
}
