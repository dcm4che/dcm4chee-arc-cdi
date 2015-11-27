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

package org.dcm4chee.archive.sc.impl;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.dcm4chee.archive.sc.StructuralChangeContext;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
public class BasicStructuralChangeContext implements StructuralChangeContext {
    private final Set<String> affectedStudyUIDs = new HashSet<>();
    private final Set<String> affectedSeriesUIDs = new HashSet<>();
    private final Set<InstanceIdentifier> affectedInstances = new HashSet<>();
    
    private final Set<InstanceIdentifier> sourceInstances = new HashSet<>();
    private final Set<InstanceIdentifier> targetInstances = new HashSet<>();
    
    protected final Enum<?>[] changeTypes;
    private final long timestamp;
    
    public BasicStructuralChangeContext(Enum<?>... changeType) {
        this(System.currentTimeMillis(), changeType);
    }
    
    private BasicStructuralChangeContext(long timestamp, Enum<?>... changeTypes) {
        this.changeTypes = changeTypes;
        this.timestamp = timestamp;
    }
    
    @Override
    public boolean hasChangeType(Enum<?> changeType) {
        return indexOfChangeType(changeType) != -1;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Enum<?>> T getChangeTypeValue(Class<T> changeTypeClass) {
        int idx = indexOfChangeTypeClass(changeTypeClass);
        return (idx != -1) ? (T)changeTypes[idx] : null;
    }
    
    @Override
    public Enum<?>[] getSubChangeTypeHierarchy(Enum<?> changeType) {
        int idx = indexOfChangeType(changeType);
        if(idx != -1) {
            int subChangeTypesLength = changeTypes.length - idx;
            Enum<?>[] subChangeTypes = new Enum[subChangeTypesLength];
            System.arraycopy(changeTypes, idx, subChangeTypes, 0, subChangeTypesLength);
            return subChangeTypes;
        } else {
            return null;
        }
    }
    
    private int indexOfChangeType(Enum<?> changeType) {
        for (int i = 0; i < changeTypes.length; i++) {
            if(changeTypes[i].equals(changeType)) {
                return i;
            }
        }
        return -1;
    }
    
    private int indexOfChangeTypeClass(Class<?> changeTypeClass) {
        for (int i = 0; i < changeTypes.length; i++) {
            if(changeTypeClass.isAssignableFrom(changeTypes[i].getClass())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Enum<?>[] getChangeTypeHierarchy() {
        return changeTypes;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public Set<String> getAffectedStudyUIDs() {
        return affectedStudyUIDs;
    }
    
    @Override
    public Set<String> getAffectedSeriesUIDs() {
        return affectedSeriesUIDs;
    }
    
    @Override
    public Set<InstanceIdentifier> getAffectedInstances() {
        return affectedInstances;
    }
    
    @Override
    public Set<InstanceIdentifier> getSourceInstances() {
        return sourceInstances;
    }
    
    @Override
    public Set<InstanceIdentifier> getTargetInstances() {
        return targetInstances;
    }
    
    public void addSourceInstance(InstanceIdentifier srcInstance) {
        this.sourceInstances.add(srcInstance);
        addAffectedInstance(srcInstance);
    }
    
    public void addSourceInstances(Collection<InstanceIdentifier> srcInstances) {
        this.sourceInstances.addAll(srcInstances);
        addAffectedInstances(srcInstances);
    }
    
    public void addTargetInstance(InstanceIdentifier targetInstance) {
        this.targetInstances.add(targetInstance);
        addAffectedInstance(targetInstance);
    }
    
    public void addTargetInstances(Collection<InstanceIdentifier> targetInstances) {
        this.targetInstances.addAll(targetInstances);
        addAffectedInstances(targetInstances);
    }
    
    private void addAffectedInstance(InstanceIdentifier affectedInstance) {
        this.affectedStudyUIDs.add(affectedInstance.getStudyInstanceUID());
        this.affectedSeriesUIDs.add(affectedInstance.getSeriesInstanceUID());
        this.affectedInstances.add(affectedInstance);
    }
    
    private void addAffectedInstances(Collection<InstanceIdentifier> affectedInstances) {
        for(InstanceIdentifier affectedInstance : affectedInstances) {
            addAffectedInstance(affectedInstance);
        }
    }
    
    @Override
    public String toString() {
        String str = "BasicStructuralChangeContext[ operation: "+ Arrays.toString(getChangeTypeHierarchy()) 
                + "\n timestamp: " + new Date(getTimestamp()).toString() 
                + "\n affectedStudies: " + getAffectedStudyUIDs()
                + "\n affectedSeries: " + getAffectedSeriesUIDs()
                + "\n #sourceInstances: " + getSourceInstances().size()
                + "\n #targetInstances: " + getTargetInstances().size()
                + "]";
                return str;
    }
    
    public static class InstanceIdentifierImpl implements InstanceIdentifier {
        private final String studyInstanceUID;
        private final String seriesInstanceUID;
        private final String sopInstanceUID;
        
        public InstanceIdentifierImpl(String studyInstanceUID, String seriesInstanceUID, String sopInstanceUID) {
            this.studyInstanceUID = studyInstanceUID;
            this.seriesInstanceUID = seriesInstanceUID;
            this.sopInstanceUID = sopInstanceUID;
        }

        @Override
        public String getStudyInstanceUID() {
            return studyInstanceUID;
        }

        @Override
        public String getSeriesInstanceUID() {
            return seriesInstanceUID;
        }

        @Override
        public String getSopInstanceUID() {
            return sopInstanceUID;
        }
        
        @Override
        public String toString() {
            return format("(studyInstanceUID: %s, seriesInstanceUID: %s, sopInstanceUID: %s)", 
                    studyInstanceUID, seriesInstanceUID, sopInstanceUID);
        }
        
        @Override
        public boolean equals(Object other) {
            if(this == other) {
                return true;
            }
            
            if(other == null || !(other instanceof InstanceIdentifier)) {
                return false;
            }
            
            InstanceIdentifier that = (InstanceIdentifier)other;
            return studyInstanceUID.equals(that.getStudyInstanceUID()) 
                    && seriesInstanceUID.equals(that.getSeriesInstanceUID())
                    && sopInstanceUID.equals(that.getSopInstanceUID());
        }
        
        @Override
        public int hashCode() {
            return 37 * studyInstanceUID.hashCode() + seriesInstanceUID.hashCode() + sopInstanceUID.hashCode();
        }
        
    }
    
}
