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
package org.dcm4chee.archive.datamgmt;

import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.json.JSONReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

public class QCObject {

    private String operation;
    private String updateScope;
    private String[] moveSOPUIDS;
    private String[] cloneSOPUIDs;
    private String[] restoreOrRejectUIDs;
    private String targetStudyUID;
    private @JsonDeserialize(using = AttributesDeserializer.class)
    Attributes targetStudyData;
    private @JsonDeserialize(using = AttributesDeserializer.class)
    Attributes orderData;
    private @JsonDeserialize(using = AttributesDeserializer.class)
    Attributes updateData;
    private boolean samePatient;
    private Code qcRejectionCode;

    public QCObject() {
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String[] getMoveSOPUIDS() {
        return moveSOPUIDS;
    }

    public void setMoveSOPUIDS(String[] moveSOPUIDS) {
        this.moveSOPUIDS = moveSOPUIDS;
    }

    public String[] getCloneSOPUIDs() {
        return cloneSOPUIDs;
    }

    public void setCloneSOPUIDs(String[] cloneSOPUIDs) {
        this.cloneSOPUIDs = cloneSOPUIDs;
    }

    public String[] getRestoreOrRejectUIDs() {
        return restoreOrRejectUIDs;
    }

    public void setRestoreOrRejectUIDs(String[] deleteUIDs) {
        this.restoreOrRejectUIDs = deleteUIDs;
    }

    public boolean isSamePatient() {
        return samePatient;
    }

    public void setSamePatient(boolean samePatient) {
        this.samePatient = samePatient;
    }

    public String getTargetStudyUID() {
        return targetStudyUID;
    }

    public void setTargetStudyUID(String targetStudyUID) {
        this.targetStudyUID = targetStudyUID;
    }

    public Attributes getOrderData() {
        return orderData;
    }

    public void setOrderData(Attributes orderData) {
        this.orderData = orderData;
    }

    public String getUpdateScope() {
        return updateScope;
    }

    public void setUpdateScope(String updateScope) {
        this.updateScope = updateScope;
    }

    public Attributes getTargetStudyData() {
        return targetStudyData;
    }

    public void setTargetStudyData(Attributes targetStudyData) {
        this.targetStudyData = targetStudyData;
    }

    public Attributes getUpdateData() {
        return updateData;
    }

    public void setUpdateData(Attributes updateData) {
        this.updateData = updateData;
    }

    public Code getQcRejectionCode() {
        return qcRejectionCode;
    }

    public void setQcRejectionCode(Code qcRejectionCode) {
        this.qcRejectionCode = qcRejectionCode;
    }
}

class AttributesDeserializer extends JsonDeserializer<Attributes> {

    private static final Logger LOG = LoggerFactory.getLogger(AttributesDeserializer.class);
    @Override
    public Attributes deserialize(org.codehaus.jackson.JsonParser parser,
            DeserializationContext ctx) throws IOException,
            JsonProcessingException {
        Attributes ds = new Attributes();
        try {
            ObjectCodec oc = parser.getCodec();
            JsonNode node = oc.readTree(parser);
            JSONReader reader = new JSONReader(
                    Json.createParser(new StringReader(node.toString())));
            ds = reader.readDataset(ds);
        } catch (Exception e) {
            LOG.error("{} : Error deserializing DICOM attributes for targetAttributes or orderData", e);
        }
        return ds;
    }

//class CodeDeserializer extends JsonDeserializer<Code> {
//
//    @Override
//    public Code deserialize(JsonParser parser, DeserializationContext ctx)
//            throws IOException, JsonProcessingException {
//        ObjectCodec oc = parser.getCodec();
//        JsonNode node = oc.readTree(parser);
//        
//    }
//    
//}
}
