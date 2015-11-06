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

import java.util.HashSet;
import java.util.Set;

import org.dcm4che3.data.Attributes;
import org.dcm4chee.archive.dto.QCEventInstance;
import org.dcm4chee.archive.entity.QCUpdateHistory.QCUpdateScope;
import org.dcm4chee.archive.qc.QCEvent;
import org.dcm4chee.archive.qc.QCEvent.QCOperation;
import org.dcm4chee.archive.sc.STRUCTURAL_CHANGE;
import org.dcm4chee.archive.sc.impl.BasicStructuralChangeContext;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
public class QCContextImpl extends BasicStructuralChangeContext {
    private final Set<Instance> sourceInstances = new HashSet<>();
    private final Set<Instance> targetInstances = new HashSet<>();

    private Attributes updateAttributes;
    
    private Set<org.dcm4chee.archive.entity.Instance> rejectionNotes;
    
    public static QCContextImpl createInstance(Enum<?> structuralChangeType, QCEvent qcEvent) {
        QCOperation qcOperation = QCOperation.valueOf(qcEvent.getOperation().toString());
        
        QCContextImpl qcCtx;
        if(QCOperation.UPDATE.equals(qcOperation)) {
            QCUpdateScope updateScope = QCUpdateScope.valueOf(qcEvent.getUpdateScope());
            qcCtx = new QCContextImpl(structuralChangeType, STRUCTURAL_CHANGE.QC, qcOperation, updateScope);
        } else {
            qcCtx = new QCContextImpl(structuralChangeType, qcOperation);
        }
        
        for(QCEventInstance qcInstance : qcEvent.getSource()) {
            Instance instance = new InstanceImpl(qcInstance.getStudyInstanceUID(), 
                    qcInstance.getSeriesInstanceUID(), qcInstance.getSopInstanceUID());
            qcCtx.addAffectedInstance(instance);
            qcCtx.sourceInstances.add(instance);
        }
        
        for(QCEventInstance qcInstance : qcEvent.getTarget()) {
            Instance instance = new InstanceImpl(qcInstance.getStudyInstanceUID(), 
                    qcInstance.getSeriesInstanceUID(), qcInstance.getSopInstanceUID());
            qcCtx.addAffectedInstance(instance);
            qcCtx.targetInstances.add(instance);
        }
        
        qcCtx.updateAttributes = qcEvent.getUpdateAttributes();
        
        qcCtx.rejectionNotes = new HashSet<>(qcEvent.getRejectionNotes());
        
        return qcCtx;
    }
    
    private QCContextImpl(Enum<?>... qcOperations) {
        super(qcOperations);
    }
    
    public Set<Instance> getSourceInstances() {
        return sourceInstances;
    }
    
    public Set<Instance> getTargetInstances() {
        return targetInstances;
    }
    
    public Set<org.dcm4chee.archive.entity.Instance> getRejectionNotes() {
        return rejectionNotes;
    }
    
    public Attributes getUpdateAttributes() {
        return updateAttributes;
    }

}
