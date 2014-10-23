package org.dcm4chee.archive.datamgmt.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;
import javax.json.Json;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONReader.Callback;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.util.TagUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.datamgmt.PatientCommands;
import org.dcm4chee.archive.datamgmt.QCBean;
import org.dcm4chee.archive.datamgmt.QCObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/qc/{AETitle}")
public class QCRestful {

    private static final Logger LOG = LoggerFactory.getLogger(QCRestful.class);

    private static String RSP;

    @Inject
    private Device device;

    @Inject
    private QCBean qcManager;
    
    private String aeTitle;

    private ArchiveAEExtension arcAEExt;

    @PathParam("AETitle")
    public void setAETitle(String aet) {
        this.aeTitle=aet;
        ApplicationEntity ae = device.getApplicationEntity(aet);
        
        if (ae == null || !ae.isInstalled()
                || (arcAEExt = ae.getAEExtension(ArchiveAEExtension.class)) == null) {
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
}

    @POST
    @Consumes("application/json")
    public Response performQC(QCObject object) {
        Attributes mergedAttrs = initializeAttrs(object);
        try{
        switch (object.getOperation().toLowerCase()) {
        case "update":
            ArchiveDeviceExtension arcDevExt = device.getDeviceExtension(ArchiveDeviceExtension.class);

            if(object.getUpdateScope().isEmpty()) {
                LOG.error("Unable to decide update Scope for QC update");
                throw new WebApplicationException("Malformed update scope");
            }
            else {
                qcManager.updateDicomObject(arcDevExt, object.getUpdateScope(), object.getUpdateData());
            }

            break;

        case "merge":
            qcManager.mergeStudies(
                    object.getMoveSOPUIDS(), object.getTargetStudyUID(), object.getQcRejectionCode());
            break;

        case "split":

                qcManager.split(
                        qcManager.locateInstances(object.getMoveSOPUIDS()), mergedAttrs, object.getQcRejectionCode());
            break;

            //in segment the target study must be there
        case "segment":

            qcManager.segment(
                    qcManager.locateInstances(object.getMoveSOPUIDS()),
                    qcManager.locateInstances(object.getCloneSOPUIDs()), mergedAttrs, object.getQcRejectionCode());
            break;

        case "reject":

            qcManager.reject(
                    object.getRestoreOrRejectUIDs(), object.getQcRejectionCode());
            break;

        case "restore":

            qcManager.restore( 
                    object.getRestoreOrRejectUIDs());
            break;
        default:
            return Response.status(Response.Status.CONFLICT).entity("Unable to decide operation").build();
        }
        }
        catch(Exception e) {
            LOG.error("{} : Error in performing QC - Restful interface", e);
            throw new WebApplicationException(e.getMessage());
        }
        return Response.ok("Successfully performed operation "+object.getOperation()).build();
    }

    private Attributes initializeAttrs(QCObject object) {
        object.getTargetStudyData().merge(object.getOrderData());
        Attributes mergedAttrs = object.getTargetStudyData();
        if(!mergedAttrs.contains(Tag.StudyInstanceUID))
            mergedAttrs.setString(Tag.StudyInstanceUID, VR.UI, object.getTargetStudyUID());
        return mergedAttrs;
    }

    @POST
    @Path("patients/{PatientOperation}")
    @Consumes("application/json")
    public Response patientOperation(@Context UriInfo uriInfo, InputStream in,
            @PathParam("PatientOperation") String patientOperation) {
        PatientCommands command = patientOperation.equalsIgnoreCase("merge")? PatientCommands.PATIENT_MERGE
                : patientOperation.equalsIgnoreCase("link")? PatientCommands.PATIENT_LINK
                        : patientOperation.equalsIgnoreCase("unlink")? PatientCommands.PATIENT_UNLINK
                                : patientOperation.equalsIgnoreCase("updateids")? PatientCommands.PATIENT_UPDATE_ID
                                : null;
        
        if (command == null)
            throw new WebApplicationException(
                    "Unable to decide patient command - supported commands {merge, link, unlink}");
        ArrayList<HashMap<String, String>> query = new ArrayList<HashMap<String, String>>();
        ArrayList<Attributes> attrs = null;
        try {
            attrs = parseJSONAttributesToList(in, query);

            if (query.size()%2!=0)
                throw new WebApplicationException(new Exception(
                        "Unable to decide request data"), Response.Status.BAD_REQUEST);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received Attributes for patient operation - "+patientOperation);
                for(int i=0; i< query.size();i++){
                    LOG.debug(i%2==0 ? "Source data[{}]: ":"Target data[{}]: ",(i/2)+1);
                for (String key : query.get(i).keySet()) {
                    LOG.debug(key + "=" + query.get(i).get(key));
                }
                }
            }
        }
        catch(Exception e)
        {
            throw new WebApplicationException(
                    "Unable to process patient operation request data");
        }

        return aggregatePatientOpResponse(attrs,device.getApplicationEntity(aeTitle),command, patientOperation);
    }

    @DELETE
    @Path("delete/studies/{StudyInstanceUID}")
    public Response deleteStudy(
            @PathParam("StudyInstanceUID") String studyInstanceUID) {
        try {
        RSP = "Deleted Study with UID = "
                + qcManager.deleteStudy(studyInstanceUID)
                        .getStudyInstanceUID();
        }
        catch (Exception e)
        {
            RSP = "Failed to delete study with UID = "+studyInstanceUID;
        }
        return Response.ok(RSP).build();

    }

    @DELETE
    @Path("delete/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
    public Response deleteSeries(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID) {
        try {
        RSP = "Deleted Series with UID = "
                + qcManager.deleteSeries(seriesInstanceUID)
                        .getSeriesInstanceUID();
        }
        catch(Exception e)
        {
            RSP = "Failed to delete series with UID = "+seriesInstanceUID;
        }
        return Response.ok(RSP).build();
    }

    @DELETE
    @Path("delete/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/{SOPInstanceUID}")
    public Response deleteInstance(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID,
            @PathParam("SOPInstanceUID") String sopInstanceUID) {
        try{
        RSP = "Deleted Instance with UID = "
                + qcManager.deleteInstance(sopInstanceUID)
                        .getSopInstanceUID();
        }
        catch(Exception e)
        {
            RSP = "Failed to delete Instance with UID = "+sopInstanceUID;
        }
        
        return Response.ok(RSP).build();
    }

    @DELETE
    @Path("purge/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
    public Response deleteSeriesIfEmpty(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID) {
        RSP = "Series with UID = "
                + seriesInstanceUID
                + " was empty and Deleted = "
                + qcManager.deleteSeriesIfEmpty(seriesInstanceUID,
                        studyInstanceUID);

        return Response.ok(RSP).build();
    }

    @DELETE
    @Path("purge/studies/{StudyInstanceUID}")
    public Response deleteStudyIfEmpty(
            @PathParam("StudyInstanceUID") String studyInstanceUID) {
        RSP = "Study with UID = " + studyInstanceUID
                + " was empty and Deleted = "
                + qcManager.deleteStudyIfEmpty(studyInstanceUID);

        return Response.ok(RSP).build();
    }

    private Response aggregatePatientOpResponse(ArrayList<Attributes> attrs,
            ApplicationEntity applicationEntity, PatientCommands command, String patientOperation) {
        ArrayList<Boolean> listRSP = new ArrayList<Boolean>();
        for(int i=0;i<attrs.size();i++)
        listRSP.add(qcManager.patientOperation(attrs.get(i), attrs.get(++i),
                arcAEExt, command));
        int trueCount=0;
        
        for(boolean rsp: listRSP)
        if(rsp)
        trueCount++;
        return trueCount==listRSP.size()?
                Response.status(Status.OK).entity
                ("Patient operation successful - "+patientOperation).build():
                trueCount==0?Response.status(Status.CONFLICT).entity
                ("Error - Unable to perform patient operation - "+patientOperation).build():
                Response.status(Status.CONFLICT).entity
                ("Warning - Unable to perform some operations - "+patientOperation).build();
    }

    private ArrayList<Attributes> parseJSONAttributesToList(InputStream in,
            ArrayList<HashMap<String, String>> query) throws IOException {

        JSONReader reader = new JSONReader(
                Json.createParser(new InputStreamReader(in, "UTF-8")));
        final ArrayList<Attributes> attributesList = new ArrayList<Attributes>();
        
        reader.readDatasets(new Callback() {
            
            @Override
            public void onDataset(Attributes fmi, Attributes dataset) {
                attributesList.add(dataset);
            }
        });
        ElementDictionary dict = ElementDictionary
                .getStandardElementDictionary();

        for (int i = 0; i < attributesList.size(); i++){
            HashMap<String, String> tmpQMap = new HashMap<String, String>(); 
        for (int j = 0; j < attributesList.get(i).tags().length; j++) {
            Attributes ds = attributesList.get(i);
            if (TagUtils.isPrivateTag(ds.tags()[j])) {
                dict = ElementDictionary.getElementDictionary(ds
                        .getPrivateCreator(ds.tags()[j]));
                query.get(i).put(dict.keywordOf(ds.tags()[j]),
                        ds.getString(ds.tags()[j]));
            } else {
                dict = ElementDictionary.getStandardElementDictionary();
                tmpQMap.put(dict.keywordOf(ds.tags()[j]),
                        ds.getString(ds.tags()[j]));
            }
        }
        query.add(tmpQMap);
        }

        return attributesList;
    }
}
