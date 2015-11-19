//
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

package org.dcm4chee.archive.qc.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.dcm4che3.data.Attributes;
import org.dcm4chee.archive.dto.QCEventInstance;
import org.dcm4chee.archive.entity.QCUpdateHistory.QCUpdateScope;
import org.dcm4chee.archive.qc.QCEvent;
import org.dcm4chee.archive.qc.QCOperationContext;
import org.dcm4chee.archive.qc.QC_OPERATION;
import org.dcm4chee.archive.sc.STRUCTURAL_CHANGE;
import org.dcm4chee.archive.sc.impl.BasicStructuralChangeContext;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
public class QCContextImpl extends BasicStructuralChangeContext implements QCOperationContext {
    private Attributes updateAttributes;
    
    private Set<org.dcm4chee.archive.entity.Instance> rejectionNotes = Collections.emptySet();
    
    public static QCContextImpl createInstance(Enum<?> structuralChangeType, QCEvent qcEvent) {
        QC_OPERATION qcOperation = QC_OPERATION.valueOf(qcEvent.getOperation().toString());
        
        QCContextImpl qcCtx;
        if(QC_OPERATION.UPDATE.equals(qcOperation)) {
            QCUpdateScope updateScope = QCUpdateScope.valueOf(qcEvent.getUpdateScope());
            qcCtx = new QCContextImpl(structuralChangeType, STRUCTURAL_CHANGE.QC, qcOperation, updateScope);
        } else {
            qcCtx = new QCContextImpl(structuralChangeType, qcOperation);
        }
        
        Collection<QCEventInstance> sourceInstances = qcEvent.getSource();
        if(sourceInstances != null) {
            for (QCEventInstance qcInstance : sourceInstances) {
                InstanceIdentifier instance = new InstanceIdentifierImpl(qcInstance.getStudyInstanceUID(),
                        qcInstance.getSeriesInstanceUID(), qcInstance.getSopInstanceUID());
                qcCtx.addSourceInstance(instance);
            }
        }
        
        Collection<QCEventInstance> targetInstances = qcEvent.getTarget();
        if (targetInstances != null) {
            for (QCEventInstance qcInstance : targetInstances) {
                InstanceIdentifier instance = new InstanceIdentifierImpl(qcInstance.getStudyInstanceUID(),
                        qcInstance.getSeriesInstanceUID(), qcInstance.getSopInstanceUID());
                qcCtx.addTargetInstance(instance);
            }
        }
        
        qcCtx.updateAttributes = qcEvent.getUpdateAttributes();
        
        Collection<org.dcm4chee.archive.entity.Instance> rejectionNotes = qcEvent.getRejectionNotes();
        if(rejectionNotes != null) {
            qcCtx.rejectionNotes = new HashSet<>(rejectionNotes);
        }
        
        return qcCtx;
    }
    
    private QCContextImpl(Enum<?>... qcOperations) {
        super(qcOperations);
    }
    
    @Override
    public Set<org.dcm4chee.archive.entity.Instance> getRejectionNotes() {
        return rejectionNotes;
    }
    
    @Override
    public Attributes getUpdateAttributes() {
        return updateAttributes;
    }
    
    @Override
    public String toString() {
        String str = "QCContext[ operation: "+ Arrays.toString(getChangeTypeHierarchy()) 
                + "\n timestamp: " + new Date(getTimestamp()).toString() 
                + "\n updateAttributes : " + (updateAttributes == null ? "No Info" : updateAttributes.toString())
                + "\n operation successfully applied on the following instances : \n" + toString(getSourceInstances())
                + "\n resulting in the following instances : \n" + toString(getTargetInstances())
                + "]";
                return str;
    }

    private static String toString(Collection<InstanceIdentifier> insts) {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for (InstanceIdentifier inst : insts) {
            buf.append(inst.toString() + "\n");
        }
        buf.append("]");
        return buf.toString();
    }

}
