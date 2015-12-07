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
package org.dcm4chee.archive.qc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.dcm4che3.data.Attributes;
import org.dcm4chee.archive.dto.QCEventInstance;
import org.dcm4chee.archive.entity.Instance;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */
@Deprecated
public class QCEvent {

    public enum QCOperation{
        UPDATE, DELETE, REJECT, RESTORE, MERGE, SPLIT, SEGMENT, LINK, UNLINK, MERGEPAT, UPDATE_ID
    }

    private QCOperation operation;
    private Collection<QCEventInstance> sourceUIDs;
    private Collection<QCEventInstance> targetUIDs;
    private long eventTime;

    private String updateScope;
    private Attributes updateAttributes;
    
    private Collection<Instance> rejNotes;
    
    public QCEvent(QCOperation operation, String updateScope, Attributes updateAttributes, 
            Collection<QCEventInstance> sourceUIDs, Collection<QCEventInstance> targetUIDs)
    {
        this.operation = operation;
        this.sourceUIDs = sourceUIDs;
        this.targetUIDs = targetUIDs;
        this.eventTime = System.currentTimeMillis();
        this.updateAttributes = updateAttributes;
        this.updateScope = updateScope;
    }

    public QCOperation getOperation() {
        return operation;
    }
    public void setOperation(QCOperation operation) {
        this.operation = operation;
    }

    public long getEventTime() {
        return eventTime;
    }
    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public Collection<QCEventInstance> getSource() {
        return sourceUIDs;
    }

    public void setSource(Collection<QCEventInstance> sourceUIDs) {
        this.sourceUIDs = sourceUIDs;
    }

    public Collection<QCEventInstance> getTarget() {
        return targetUIDs;
    }

    public void setTarget(Collection<QCEventInstance> targetUIDs) {
        this.targetUIDs = targetUIDs;
    }

    public String getUpdateScope() {
        return updateScope;
    }

    public void setUpdateScope(String updateScope) {
        this.updateScope = updateScope;
    }

    public Attributes getUpdateAttributes() {
        return updateAttributes;
    }

    public void setUpdateAttributes(Attributes updateAttributes) {
        this.updateAttributes = updateAttributes;
    }

    public void addRejectionNote(Instance rejNote) {
        if (rejNotes == null)
            rejNotes = new ArrayList<Instance>();
        rejNotes.add(rejNote);
    }
    public Collection<Instance> getRejectionNotes() {
        return rejNotes;
    }
    
    @Override
    public String toString() {
        String str = "QCEvent[ operation: "+ (operation!=null?operation.name():"") 
                + "\n eventTime: " + new Date(eventTime).toString() 
                + "\n update :" + (updateScope==null? "No Info":updateScope)
                + "\n updateAttributes : " + (updateAttributes==null? "No Info":updateAttributes.toString())
                + "\n operation successfully applied on the following instances : \n" + (sourceUIDs!=null?printQCEventInstance(sourceUIDs):"[]")
                + "\n resulting in the following instances : \n" + (targetUIDs!=null?printQCEventInstance(targetUIDs):"[]")
                + "]";
                return str;
    }

    private String printQCEventInstance(Collection<QCEventInstance> uids) {
        String printedString = "["; 
        for(QCEventInstance inst : uids) {
            printedString += inst.toString() + "\n";
        }
        printedString += "]";
        return printedString;
    }
}
