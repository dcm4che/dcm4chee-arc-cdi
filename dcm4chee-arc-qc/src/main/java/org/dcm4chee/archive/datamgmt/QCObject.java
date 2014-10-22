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

    public String[] getDeleteUIDs() {
        return restoreOrRejectUIDs;
    }

    public void setDeleteUIDs(String[] deleteUIDs) {
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
