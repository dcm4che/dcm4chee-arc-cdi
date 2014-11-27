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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Attributes.Visitor;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.dto.ReferenceUpdateOnRetrieveScope;
import org.dcm4chee.archive.entity.QCInstanceHistory;
import org.dcm4chee.archive.qc.QCBean;
import org.dcm4chee.archive.retrieve.RetrieveContext;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class RetrieveServiceQCDecorator.
 * Applies UID changes from QC history to outbound files at retrieve time.
 * 
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@Decorator
public abstract class RetrieveServiceQCDecorator implements RetrieveService{

    private static final Logger LOG = LoggerFactory.getLogger(
            RetrieveServiceQCDecorator.class);

    @Inject
    @Delegate
    RetrieveService retrieveService;
    
    ArrayList<Attributes> modifications = new ArrayList<Attributes>();
    
    @Inject
    private QCBean qcManager;

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.retrieve.RetrieveService#coerceRetrievedObject(org.dcm4chee.archive.retrieve.RetrieveContext, java.lang.String, org.dcm4che3.data.Attributes)
     */
    @Override
    public void coerceRetrievedObject(RetrieveContext retrieveContext,
            String remoteAET, Attributes attrs) throws DicomServiceException {
        retrieveService.coerceRetrievedObject(retrieveContext, remoteAET, attrs);
        ReferenceUpdateOnRetrieveScope qcUpdateReferencesOnRetrieve =
                retrieveContext.getArchiveAEExtension().getQcUpdateReferencesOnRetrieve();
        boolean requiresUpdate = false;
        switch(qcUpdateReferencesOnRetrieve) {
        case DEACTIVATE: break;
        case PATIENT :
            requiresUpdate = qcManager.requiresReferenceUpdate(null,attrs);
            LOG.debug("Instance Retrieved Requires Update : {}", requiresUpdate);
            break;
        case STUDY:
            requiresUpdate = qcManager.requiresReferenceUpdate(attrs.getString(
                    Tag.StudyInstanceUID), null);
            LOG.debug("Instance Retrieved Requires Update : {}", requiresUpdate);
            break;
        }
        if(requiresUpdate) {
            LOG.debug("Performing reference update on {} scope", qcUpdateReferencesOnRetrieve.name());
            Collection<String> referencedStudyInstanceUIDs = new ArrayList<String>(); 
            qcManager.scanForReferencedStudyUIDs(attrs, referencedStudyInstanceUIDs);
            
            final Collection<QCInstanceHistory> referencesHistory = qcManager
                    .getReferencedHistory(referencedStudyInstanceUIDs);
            final HashMap<String, String> mapping = createUIDMapFromHistory(referencesHistory);
            final ElementDictionary dict = ElementDictionary.getStandardElementDictionary();
            try {
                 attrs.accept(new Visitor() {
                     private HashMap<String, HashMap<String,ArrayList<String>>> structure = null;
                     Stack<String> sqStack = new Stack<String>();
                    @Override
                    public boolean visit(Attributes attrs, int tag, VR vr, Object value) {
                        if(vr.equals(VR.SQ))
                        sqStack.push(dict.keywordOf(tag));
                        if(attrs.contains(Tag.ReferencedSOPInstanceUID) 
                                && !attrs.getParent().isRoot()) {
                            if(tag == Tag.ReferencedSOPInstanceUID) {
                            //hierarchical
                            if(attrs.getParent().contains(Tag.SeriesInstanceUID)) {
                                //build a new one
                                Stack<String> tmp = new Stack<String>();
                                //fully hierarchical (with study)
                                if(!attrs.getParent().getParent().isRoot()) {
                                    String oldStudyUID = attrs.getParent().getParent().getString(Tag.StudyInstanceUID);
                                    String oldSeriesUID = attrs.getParent().getString(Tag.SeriesInstanceUID);
                                    String oldSopUID = attrs.getString(Tag.ReferencedSOPInstanceUID);
                                    if(mapping.get(oldSopUID) != null) {
                                    extractUpdatedSequencesFullHierarchy(mapping, dict, attrs, tmp,
                                            oldStudyUID, oldSeriesUID,
                                            oldSopUID);
                                    }
                                    else if(tag == Tag.ReferencedSOPInstanceUID){
                                        extractUpdatedSequencesFullHierarchy(null, dict, attrs, tmp,
                                                oldStudyUID, oldSeriesUID,
                                                oldSopUID);
                                    }
                                }
                                else {
                                    //series reference without study (presentation state)
                                    String oldSeriesUID = attrs.getParent().getString(Tag.SeriesInstanceUID);
                                    String oldSopUID = attrs.getString(Tag.ReferencedSOPInstanceUID);
                                    if(mapping.get(oldSopUID) != null) {
                                    extractUpdatedSequenceSeriesHierarchy(
                                            mapping, dict, attrs, tmp,
                                            oldSeriesUID, oldSopUID);
                                    }
                                    else if(tag == Tag.ReferencedSOPInstanceUID){
                                        extractUpdatedSequenceSeriesHierarchy(
                                                null, dict, attrs, tmp,
                                                oldSeriesUID, oldSopUID);
                                    }
                                }
                            }
                            else {
                                //non hierarchical
                                //change in place
                                if(mapping.get(attrs.getString(Tag.ReferencedSOPInstanceUID)) != null )
                                attrs.setString(Tag.ReferencedSOPInstanceUID, VR.UI, mapping.get(
                                        attrs.getString(Tag.ReferencedSOPInstanceUID)));
                            }
                        }
                        }
                        
                        return true;
                    }
                    private void extractUpdatedSequenceSeriesHierarchy(
                            final HashMap<String, String> mapping,
                            final ElementDictionary dict, Attributes attrs,
                            Stack<String> tmp, String oldSeriesUID,
                            String oldSopUID) {
                        Attributes newSeriesAttrs = new Attributes();
                        newSeriesAttrs.setString(Tag.SeriesInstanceUID, VR.UI,mapping == null ? oldSeriesUID : mapping.get(oldSeriesUID));
                        Sequence sopSeq = newSeriesAttrs.newSequence(dict.tagForKeyword(sqStack.peek()), 1);
                        Attributes newSopAttrs = new Attributes();
                        newSopAttrs.setString(Tag.ReferencedSOPClassUID, VR.UI, attrs.getString(Tag.ReferencedSOPClassUID));
                        newSopAttrs.setString(Tag.ReferencedSOPInstanceUID, VR.UI, mapping == null ? oldSopUID : mapping.get(oldSopUID));
                        sopSeq.add(newSopAttrs);
                        tmp.push(sqStack.pop());
                        Attributes tmpRootAttrs = new Attributes();
                        Sequence seq = tmpRootAttrs.newSequence(dict.tagForKeyword(sqStack.peek()), 1);
                        seq.add(newSeriesAttrs);
                        modifications.add(tmpRootAttrs);
                        sqStack.push(tmp.pop());
                    }
                    private void extractUpdatedSequencesFullHierarchy(
                            final HashMap<String, String> mapping,
                            final ElementDictionary dict, Attributes attrs,
                            Stack<String> tmp, String oldStudyUID,
                            String oldSeriesUID, String oldSopUID) {
                        Attributes newStudyAttrs = new Attributes();
                        newStudyAttrs.setString(Tag.StudyInstanceUID, VR.UI, mapping == null ? oldStudyUID : mapping.get(oldStudyUID));
                        Attributes newSeriesAttrs = new Attributes();
                        newSeriesAttrs.setString(Tag.SeriesInstanceUID, VR.UI, mapping == null ? oldSeriesUID : mapping.get(oldSeriesUID));
                        Sequence sopSeq = newSeriesAttrs.newSequence(dict.tagForKeyword(sqStack.peek()), 1);
                        Attributes newSopAttrs = new Attributes();
                        newSopAttrs.setString(Tag.ReferencedSOPClassUID, VR.UI, attrs.getString(Tag.ReferencedSOPClassUID));
                        newSopAttrs.setString(Tag.ReferencedSOPInstanceUID, VR.UI, mapping == null ? oldSopUID : mapping.get(oldSopUID));
                        sopSeq.add(newSopAttrs);
                        tmp.push(sqStack.pop());
                        tmp.push(sqStack.pop());
                        Sequence referencedSeriesSeq = newStudyAttrs.newSequence(dict.tagForKeyword(tmp.peek()), 1);
                        referencedSeriesSeq.add(newSeriesAttrs);
                        Attributes tmpRootAttrs = new Attributes();
                        Sequence seq = tmpRootAttrs.newSequence(dict.tagForKeyword(sqStack.peek()), 1);
                        seq.add(newStudyAttrs);
                        modifications.add(tmpRootAttrs);
                        sqStack.push(tmp.pop());
                        sqStack.push(tmp.pop());
                    }
                }, true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Sequence attrSequence = null;
            boolean loaded = false;
            for(Attributes modification : modifications) {
                for(int tag : modification.tags()) {
                    if(!loaded) {
                        attrSequence = attrs.getSequence(tag);
                        attrSequence.clear();
                        loaded = true;
                    }
                    if(dict.vrOf(tag) == VR.SQ) {
                        Sequence rootLevelSeq = modification.getSequence(tag);
                        for(int i=0; i < rootLevelSeq.size(); i++) {
                           Attributes tmp =  rootLevelSeq.remove(i);
                            attrSequence.add(tmp);
                        }
                    }
                }
            }
            modifications.clear();
        }

    }

    private HashMap<String, String> createUIDMapFromHistory(
            Collection<QCInstanceHistory> referencesHistory) {
        HashMap<String, String> mapping = new HashMap<String, String>();
        for(QCInstanceHistory inst : referencesHistory) {
            mapping.put(inst.getOldUID(), inst.getCurrentUID());
            mapping.put(inst.getSeries().getOldSeriesUID(), inst.getCurrentSeriestUID());
            mapping.put(inst.getSeries().getStudy().getOldStudyUID(), inst.getCurrentStudyUID());
        }
        return mapping;
    }

}
