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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.iocm.RejectionServiceDeleteBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */

@Stateless
public class RejectionServiceDeleteEJB implements RejectionServiceDeleteBean {
    static Logger LOG = LoggerFactory.getLogger(RejectionServiceDeleteEJB.class);

    @PersistenceContext(unitName = "dcm4chee-arc")
    public EntityManager em;
    
    @Override
    public Collection<Location> deleteRejected(Object source, Collection<Instance> instances) {
        try {
        Collection<Location> toBeDeleted = new HashSet<Location>();
        for(Instance inst: instances) {
            inst = em.merge(inst);
              if(isRejected(inst)){
                Collection<Location> tmpRefs = clone(inst.getLocations());
                tmpRefs.addAll(inst.getOtherLocations());
                inst.getSeries().clearQueryAttributes();
                inst.getSeries().getStudy().clearQueryAttributes();
                toBeDeleted.addAll(detachReferences(inst, tmpRefs));
                em.remove(inst);
                LOG.info("Removing {} and Scheduling delete for associated file references" , inst);
            }
        }

           return toBeDeleted;
        }
        catch(Exception e) {
            LOG.error("{}: Error deleting rejected objects, Transaction rolled back", e);
            throw new EJBException(e.getMessage());
        }

    }

    private boolean isRejected(Instance inst) {
        return inst.getRejectionNoteCode()!=null;
    }

    private Collection<Location> clone(Collection<Location> refs)
    {
        ArrayList<Location> clone = new ArrayList<Location>();
        Iterator<Location> iter = refs.iterator();
        while(iter.hasNext())
        {
            Location ref = iter.next();
            clone.add(ref);
        }
        return clone;
    }

    private Collection<Location> detachReferences(Instance inst, Collection<Location> refs) {
        
        for(Iterator<Location> iterator = refs.iterator(); iterator.hasNext();) {
            Location ref = iterator.next();
            
            if(ref.getInstances().isEmpty()){
                //no file alias only file ref - normal case - nullify and delete
                inst.getLocations().remove(ref);
                ref.setInstance(null);
                continue;
            }
            else {
                if(ref.getInstances().size() == 1) {
                    if(ref.getInstances().contains(inst))
                    {
                        
                        if(ref.getInstance()==null) {
                       //instance referenced only in file alias relation and ref has no reference through file ref relation to another instance - cloned case - remove alias and  delete
                            ref.getInstances().clear();
                            inst.getOtherLocations().remove(ref);
                        continue;
                        }
                        else if(ref.getInstance().getSopInstanceUID()
                            .equalsIgnoreCase(inst.getSopInstanceUID())) {
                        //instance referenced in both relations and ref has no more instances - remove both and delete
                            inst.getLocations().remove(ref);
                            ref.setInstance(null);
                        ref.getInstances().clear();
                        inst.getOtherLocations().remove(ref);
                        continue;
                        }
                        else
                      //instance referenced only in file alias relation and ref has a reference through file ref relation to another instance - cloned case - remove alias and do not  delete
                            ref.getInstances().clear();
                            inst.getOtherLocations().remove(ref);
                    }
                    else
                    {
                        //instance referenced only in file ref relation - file alias relation references another instance - remove file ref relation and do not delete
                        inst.getLocations().remove(ref);
                        ref.setInstance(null);
                    }
                }
                else {
                    //size > 1
                    if(ref.getInstances().contains(inst) 
                            && ref.getInstance()!=null 
                            && ref.getInstance().getSopInstanceUID()
                            .equalsIgnoreCase(inst.getSopInstanceUID())) {
                    //ref in both - remove both relations and do not delete
                        ref.getInstances().remove(inst);
                        inst.getOtherLocations().remove(ref);
                        inst.getLocations().remove(ref);
                        ref.setInstance(null);
                    }
                    else if(ref.getInstances().contains(inst))
                    {
                    //only ref in file alias relation - remove file alias relation and do not delete
                        ref.getInstances().remove(inst);
                        inst.getOtherLocations().remove(ref);
                    }
                    else {
                        //only ref in file ref relation - nullify relation and do not delete
                        inst.getLocations().remove(ref);
                        ref.setInstance(null);
                    }
                }
            }
            iterator.remove();
        }

        return refs;
    }

    @Override
    public Collection<Instance> findRejectedObjects(Code rejectionNote, Timestamp deadline, int maxDeletes) {
        String queryStr = "SELECT i FROM Instance i WHERE i.rejectionNoteCode = ?1 and i.updatedTime < :deadline";
        try{
            TypedQuery<Instance> query = em.createQuery(queryStr, Instance.class);
            if(maxDeletes > 0)
                query.setMaxResults(maxDeletes);

            query.setParameter(1, rejectionNote);
            query.setParameter("deadline", deadline);
            return query.getResultList();
        }
        catch(Exception e)
        {
            LOG.error("{}: Error - finding rejected objects",e);
            return Collections.emptyList();
        }
    }
}