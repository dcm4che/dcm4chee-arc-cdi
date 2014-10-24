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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4chee.archive.filemgmt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.dcm4chee.archive.entity.FileRef;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

@Stateless
public class FileMgmtEJB implements FileMgmt{

    private static final Logger LOG = LoggerFactory.getLogger(FileMgmtEJB.class);
    
    @Resource(mappedName="java:/ConnectionFactory")
    private ConnectionFactory connFactory;

    @Resource(mappedName="java:/queue/delete")
    private Queue deleteQueue;

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    @Override
    public void scheduleDelete(Collection<FileRef> refs, int delay) throws Exception {
    for(FileRef ref: refs)
        try {
        Connection conn = connFactory.createConnection();
            try {
                Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session.createProducer(deleteQueue);
                ObjectMessage msg = session.createObjectMessage(ref);
                if (delay > 0)
                    msg.setLongProperty("_HQ_SCHED_DELIVERY",
                            System.currentTimeMillis() + delay);
                producer.send(msg);
            } finally {
                conn.close();
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void failDelete(FileRef ref) {
        ref.setStatus(FileRef.Status.DELETE_FAILED);
        LOG.warn("Failed to delete file {}, setting file reference status to {}",ref.getFilePath(),ref.getStatus() );
    }

    private void removeDeadFileRef(FileRef ref) {

        try {
            em.remove(ref);
        }
        catch (Exception e)
        {
            LOG.error("Failed to remove File Ref {}", ref.toString());
        }

    }

    @Override
    public boolean doDelete(FileRef ref) {
        try{
        File tmp = new File(ref.getFileSystem().getPath().toString(),ref.getFilePath());

            Files.delete(tmp.toPath());
        }
        catch(IOException e)
        {
            return false;
        }
        removeDeadFileRef(ref);
        return true;
    }

    @Override
    public FileRef reattachRef(FileRef ref) {
        long pk = ref.getPk();
        FileRef reattached =  em.find(FileRef.class, pk);
        return reattached;
    }
}
