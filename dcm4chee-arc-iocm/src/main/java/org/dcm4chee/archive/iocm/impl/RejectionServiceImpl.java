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

package org.dcm4chee.archive.iocm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import javax.enterprise.context.ApplicationScoped;

import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.FileRef;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.filemgmt.FileMgmt;
import org.dcm4chee.archive.iocm.InstanceAlreadyRejectedException;
import org.dcm4chee.archive.iocm.RejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@ApplicationScoped
public class RejectionServiceImpl implements RejectionService {

    static Logger LOG = LoggerFactory.getLogger(RejectionServiceImpl.class);

    @Inject
    FileMgmt fileManager;
    
    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;
    
    @Override
    public int reject(Object source, Collection<Instance> instances,
            Code rejectionCode, org.dcm4che3.data.Code[] prevRejectionCodes) {
        int count = 0;
        for (Instance inst : instances) {
            Code prevRejectionCode = inst.getRejectionNoteCode();
            if (rejectionCode.equals(prevRejectionCode))
                continue;

            if (!canOverwritePrevRejectionCode(prevRejectionCode, prevRejectionCodes))
                throw new InstanceAlreadyRejectedException(inst);

            LOG.debug("{}: Apply rejection {} to {}", source, rejectionCode, inst);
            updateInstance(inst, rejectionCode);
            count++;
        }
        return count;
    }

    @Override
    public int restore(Object source, Collection<Instance> instances,
            org.dcm4che3.data.Code[] prevRejectionCodes) {
        int count = 0;
        for (Instance inst : instances) {
            Code prevRejectionCode = inst.getRejectionNoteCode();
            if (prevRejectionCode == null)
                continue;

            if (!canOverwritePrevRejectionCode(prevRejectionCode, prevRejectionCodes))
                throw new InstanceAlreadyRejectedException(inst);

            LOG.debug("{}: Revoke rejection {} of {}", source, prevRejectionCode, inst);
            updateInstance(inst, null);
            count++;
        }
        return count;
    }

    

    private boolean canOverwritePrevRejectionCode(Code prevRejectionCode,
            org.dcm4che3.data.Code[] prevRejectionCodes) {
        if (prevRejectionCode == null || prevRejectionCodes == null)
            return true;

        for (org.dcm4che3.data.Code code : prevRejectionCodes) {
            if (prevRejectionCode.equalsIgnoreMeaning(code))
                return true;
        }
        return false;
    }

    private void updateInstance(Instance inst, Code rejectionCode) {
        inst.setRejectionNoteCode(rejectionCode);
        Series series = inst.getSeries();
        Study study = series.getStudy();
        series.clearQueryAttributes();
        study.clearQueryAttributes();
    }

    @Override
    public void deleteRejected(Object source, Collection<Instance> instances) {
        for(Instance inst: instances) {
            if(isRejected(inst)){
                Collection<FileRef> tmpRefs = clone(inst.getFileRefs());
                detachReferences(inst);
                try {
                    fileManager.scheduleDelete(tmpRefs, 0);
                    LOG.debug("{}: Scheduled delete for rejected {}", source, inst);
                    em.remove(inst);
                    LOG.info("Removed {}" + inst);
                    inst.getSeries().clearQueryAttributes();
                    inst.getSeries().getStudy().clearQueryAttributes();
                }
                catch(Exception e) {
                    LOG.error("{}: Error deleting rejected object {}, {}", source, inst, e);
                }
            }
        }
    }

    private boolean isRejected(Instance inst) {
        return inst.getRejectionNoteCode()!=null;
    }

    private Collection<FileRef> clone(Collection<FileRef> refs)
    {
        ArrayList<FileRef> clone = new ArrayList<FileRef>();
        Iterator<FileRef> iter = refs.iterator();
        while(iter.hasNext())
        {
            FileRef ref = iter.next();
            ref.getFileSystem();
            clone.add(ref);
        }
        return clone;
    }

    private void detachReferences(Instance inst) {
        for(FileRef ref: inst.getFileRefs())
            ref.setInstance(null);
        inst.getFileRefs().clear();
    }
}
