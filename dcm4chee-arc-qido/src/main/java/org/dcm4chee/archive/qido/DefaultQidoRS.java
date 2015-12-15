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
package org.dcm4chee.archive.qido;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.stream.StreamResult;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.ws.rs.MediaTypes;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.query.Query;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.query.QueryService;
import org.dcm4chee.archive.query.QueryServiceUtils;
import org.dcm4chee.archive.query.util.QueryBuilder;
import org.dcm4chee.archive.rs.HostAECache;
import org.dcm4chee.archive.rs.HttpSource;
import org.dcm4chee.archive.web.QidoRS;
import org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.StringPath;

/**
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Alessio Roselli <alessio.roselli@agfa.com>
 */
public class DefaultQidoRS implements QidoRS {

    private static final int STATUS_OK = 200;
    private static final int STATUS_PARTIAL_CONTENT = 206;

    private static final Logger LOG = LoggerFactory.getLogger(DefaultQidoRS.class);

    private static ElementDictionary DICT =
            ElementDictionary.getStandardElementDictionary();

    private final static int[] STUDY_FIELDS = {
        Tag.StudyDate, Tag.StudyTime, Tag.AccessionNumber,
        Tag.ModalitiesInStudy, Tag.ReferringPhysicianName,
        Tag.PatientName, Tag.PatientID, Tag.PatientBirthDate,
        Tag.PatientSex, Tag.StudyID, Tag.StudyInstanceUID,
        Tag.NumberOfStudyRelatedSeries,
        Tag.NumberOfStudyRelatedInstances
    };

    private final static int[] SERIES_FIELDS = {
        Tag.Modality, Tag.SeriesDescription, Tag.SeriesNumber,
        Tag.SeriesInstanceUID, Tag.NumberOfSeriesRelatedInstances,
        Tag.PerformedProcedureStepStartDate,
        Tag.PerformedProcedureStepStartTime,
        Tag.RequestAttributesSequence
    };

    private final static int[] INSTANCE_FIELDS = {
        Tag.SOPClassUID, Tag.SOPInstanceUID, Tag.InstanceNumber,
        Tag.Rows, Tag.Columns, Tag.BitsAllocated, Tag.NumberOfFrames
    };

    private final static int[] STUDY_SERIES_FIELDS =
            catAndSort(STUDY_FIELDS, SERIES_FIELDS);

    private final static int[] STUDY_SERIES_INSTANCE_FIELDS =
            catAndSort(STUDY_SERIES_FIELDS, INSTANCE_FIELDS);

    private final static int[] SERIES_INSTANCE_FIELDS =
            catAndSort(SERIES_FIELDS, INSTANCE_FIELDS);

    private String aetitle;
    
    private ApplicationEntity ae;
    
    private ArchiveAEExtension arcAE;
    
    private QueryContext queryContext;

    @Inject
    private Device device;

    @Inject
    protected QueryService queryService;

    @Inject
    private HostAECache hostAECache;

    @Context
    private HttpServletRequest request;

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpHeaders headers;

    @javax.ws.rs.QueryParam("fuzzymatching")
    private boolean fuzzymatching;

    @javax.ws.rs.QueryParam("datetimematching")
    private boolean datetimematching;

    @javax.ws.rs.QueryParam("timezoneadjustment")
    private boolean timezoneadjustment;

    @javax.ws.rs.QueryParam("offset")
    private int offset;

    @javax.ws.rs.QueryParam("limit")
    private int limit;

    @javax.ws.rs.QueryParam("includefield")
    private List<String> includefield;

    @javax.ws.rs.QueryParam("orderby")
    private List<String> orderby;

    private OrderSpecifier<?>[] orderSpecifiers;

    private final Attributes keys = new Attributes(64);

    private String method;

    private boolean includeAll;

    private static int[] catAndSort(int[] src1, int[] src2) {
        int[] dest = new int[src1.length + src2.length];
        System.arraycopy(src1, 0, dest, 0, src1.length);
        System.arraycopy(src2, 0, dest, src1.length, src2.length);
        Arrays.sort(dest);
        return dest;
    }
    
    /**
     * Setter for the AETitle property, automatically invoked by the CDI
     * container. Setter initializes ArchiveAEExtension and queryParam too.
     * 
     * @param aet
     *            AE title
     */

    @PathParam("AETitle")
    public void setAETitle(String aet) {
        
        this.aetitle=aet;
        ae = device.getApplicationEntity(aet);
        
        if (ae == null || !ae.isInstalled()
                || (arcAE = ae.getAEExtension(ArchiveAEExtension.class)) == null) {
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public Response searchForStudiesXML() throws Exception {
        return search("searchForStudiesXML", QueryRetrieveLevel.STUDY,
                false, null, null, STUDY_FIELDS, Output.DICOM_XML);
    }

    @Override
    public Response searchForStudiesJSON() throws Exception {
        return search("searchForStudiesJSON", QueryRetrieveLevel.STUDY,
                false, null, null, STUDY_FIELDS, Output.JSON);
    }

    @Override
    public Response searchForSeriesXML() throws Exception {
        return search("searchForSeriesXML",
                QueryRetrieveLevel.SERIES, true, null, null,
                STUDY_SERIES_FIELDS, Output.DICOM_XML);
    }

    @Override
    public Response searchForSeriesJSON() throws Exception {
        return search("searchForSeriesJSON",
                QueryRetrieveLevel.SERIES, true, null, null,
                STUDY_SERIES_FIELDS, Output.JSON);
    }

    @Override
    public Response searchForSeriesOfStudyXML(String studyInstanceUID) throws Exception {
        return search("searchForSeriesOfStudyXML", QueryRetrieveLevel.SERIES,
                false, studyInstanceUID, null, SERIES_FIELDS, Output.DICOM_XML);
    }

    @Override
    public Response searchForSeriesOfStudyJSON(String studyInstanceUID) throws Exception {
        return search("searchForSeriesOfStudyJSON", QueryRetrieveLevel.SERIES,
                false, studyInstanceUID, null, SERIES_FIELDS, Output.JSON);
    }

    @Override
    public Response searchForInstancesXML() throws Exception {
        return search("searchForInstancesXML",
                QueryRetrieveLevel.IMAGE, true, null, null,
                STUDY_SERIES_INSTANCE_FIELDS, Output.DICOM_XML);
    }

    @Override
    public Response searchForInstancesJSON() throws Exception {
        return search("searchForInstancesJSON",
                QueryRetrieveLevel.IMAGE, true, null, null,
                STUDY_SERIES_INSTANCE_FIELDS, Output.JSON);
    }

    @Override
    public Response searchForInstancesOfStudyXML(String studyInstanceUID) throws Exception {
        return search("searchForInstancesOfStudyXML", QueryRetrieveLevel.IMAGE,
                true, studyInstanceUID, null, SERIES_INSTANCE_FIELDS, Output.DICOM_XML);
    }

    @Override
    public Response searchForInstancesOfStudyJSON(String studyInstanceUID) throws Exception {
        return search("searchForInstancesOfStudyJSON", QueryRetrieveLevel.IMAGE,
                true, studyInstanceUID, null, SERIES_INSTANCE_FIELDS, Output.JSON);
    }

    @Override
    public Response searchForInstancesOfSeriesXML(String studyInstanceUID,String seriesInstanceUID) throws Exception {
        return search("searchForInstancesOfSeriesXML", QueryRetrieveLevel.IMAGE,
                false, studyInstanceUID, seriesInstanceUID,
                INSTANCE_FIELDS, Output.DICOM_XML);
    }

    @Override
    public Response searchForInstancesOfSeriesJSON(String studyInstanceUID,String seriesInstanceUID) throws Exception {
        return search("searchForInstancesOfSeriesJSON", QueryRetrieveLevel.IMAGE,
                false, studyInstanceUID, seriesInstanceUID,
                INSTANCE_FIELDS, Output.JSON);
    }

    private Response search(String method, QueryRetrieveLevel qrlevel,
            boolean relational, String studyInstanceUID,
            String seriesInstanceUID, int[] includetags, Output output) throws Exception{
        
        
        init(method, qrlevel, relational, studyInstanceUID, seriesInstanceUID,
                includetags);

        Query query = QueryServiceUtils.createQuery(queryService, qrlevel, queryContext);
        try {
            query.initQuery();
            int status = STATUS_OK;
            int maxResults = arcAE.getQIDOMaxNumberOfResults();
            int offset = Math.max(this.offset, 0);
            int limit = Math.max(this.limit, 0);
            if (maxResults > 0 && (limit == 0 || limit >  maxResults)) {
                int numResults = (int) (query.count() - offset);
                if (numResults == 0)
                    return Response.ok().build();
    
                if (numResults > maxResults) {
                    limit = maxResults;
                    status = STATUS_PARTIAL_CONTENT;
                }
            }
            if (offset > 0)
                query.offset(offset);
            
            if (limit > 0)
                query.limit(limit);
    
            if (orderSpecifiers != null)
                query.orderBy(orderSpecifiers);
    
            query.executeQuery();
            if (!query.hasMoreMatches())
                return Response.ok().build();
    
            return Response.status(status).entity(
                    output.entity(this, query, qrlevel)).build();
        } finally {
            query.close();
        }
    }

    /**
     * Initializes query options and parameters
     * 
     * @throws DicomServiceException
     */
    private void init(String method, QueryRetrieveLevel qrlevel,
            boolean relational, String studyInstanceUID,
            String seriesInstanceUID, int[] defIncludefields) throws DicomServiceException {
        this.method = method;

        ApplicationEntity sourceAE;
        try {
            sourceAE = hostAECache.findAE(new HttpSource(request));
        } catch (ConfigurationException e) {
            throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
        }

        TransferCapability tc = ae.getTransferCapabilityFor(
                    UID.StudyRootQueryRetrieveInformationModelFIND, Role.SCP);
        if (tc == null)
            throw new WebApplicationException(Status.FORBIDDEN);

        EnumSet<QueryOption> queryOpts = EnumSet.noneOf(QueryOption.class);
        if (relational)
            queryOpts.add(QueryOption.RELATIONAL);
        if (datetimematching)
            queryOpts.add(QueryOption.DATETIME);
        if (fuzzymatching)
            queryOpts.add(QueryOption.FUZZY);
        if (timezoneadjustment)
            queryOpts.add(QueryOption.TIMEZONE);

        if (!queryOpts.isEmpty()) {
            EnumSet<QueryOption> supportedQueryOpts = tc.getQueryOptions();
            if (supportedQueryOpts == null
                    || !supportedQueryOpts.containsAll(queryOpts))
                throw new WebApplicationException(Status.FORBIDDEN);
        }

        try {
            includeAll = !includefield.isEmpty() 
                    && includefield.get(0).equalsIgnoreCase("all");
            if (!includeAll) {
                initDefaultIncludefields(defIncludefields);
                parseIncludefield();
            }
            for (Map.Entry<String, List<String>> qParam
                    : uriInfo.getQueryParameters().entrySet()) {
                String name = qParam.getKey();
                if (isDicomAttribute(name))
                    parseDicomAttribute(name, qParam.getValue());
            }
            if (studyInstanceUID != null)
                keys.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUID);
            if (seriesInstanceUID != null)
                keys.setString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUID);

            LOG.debug("{}: Querykeys:\n{}", method, keys);
            
            parseOrderby(qrlevel);
            
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e, Status.BAD_REQUEST);
        }

        queryContext = queryService.createQueryContext(queryService);

        queryContext.setRemoteAET(sourceAE.getAETitle());
        queryContext.setServiceSOPClassUID(UID.StudyRootQueryRetrieveInformationModelFIND);
        queryContext.setArchiveAEExtension(arcAE);
        QueryParam queryParam = queryService.getQueryParam(
                request, queryContext.getRemoteAET(), arcAE, queryOpts,
                accessControlIDs());
        queryContext.setQueryParam(queryParam);
        queryContext.setKeys(keys);
        queryService.coerceRequestAttributes(queryContext);
        queryService.initPatientIDs(queryContext);

    }

    //TODO
    private String[] accessControlIDs() {
        return StringUtils.EMPTY_STRING;
    }

    private void initDefaultIncludefields(int[] defIncludefields) {
        for (int tag : defIncludefields) {
            keys.setNull(tag, DICT.vrOf(tag));
        }
    }

    private static boolean isDicomAttribute(String name) {
        switch (name.charAt(0)) {
        case 'd':
            return !name.equals("datetimematching");
        case 'f':
            return !name.equals("fuzzymatching");
        case 'i':
            return !name.equals("includefield");
        case 'l':
            return !name.equals("limit");
        case 'o':
            return !name.equals("offset")
                && !name.equals("orderby");
        case 't':
            return !name.equals("timezoneadjustment");
        }
        return true;
    }

    private void parseIncludefield() {
        for (String s : includefield) {
            for (String field : StringUtils.split(s, ',')) {
                try {
                    int[] tagPath = parseTagPath(field);
                    int tag = tagPath[tagPath.length-1];
                    nestedKeys(tagPath).setNull(tag, DICT.vrOf(tag));
                } catch (IllegalArgumentException e2) {
                    throw new IllegalArgumentException("includefield=" + s);
                }
            }
        }
    }

    private void parseOrderby(QueryRetrieveLevel qrLevel) {
        if (orderby.isEmpty())
            return;

        ArrayList<OrderSpecifier<?>> list = new ArrayList<OrderSpecifier<?>>();
        for (String s : orderby) {
            try {
                for (String field : StringUtils.split(s, ',')) {
                    boolean desc = field.charAt(0) == '-';
                    int tag = parseTag(desc ? field.substring(1) : field);
                    for (com.mysema.query.types.Path<?> path : QueryBuilder.stringOrDateTimePathOf(tag, qrLevel)) {
                        if (path instanceof DateTimePath) {
                            list.add(desc ? 
                                    ((DateTimePath<java.util.Date>) path).desc() 
                                    : ((DateTimePath<java.util.Date>) path).asc());
                        }
                        else {
                            list.add(desc ? ((StringPath) path).desc() : ((StringPath) path).asc());
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("orderby=" + s);
            }
        }

        orderSpecifiers = list.toArray(new OrderSpecifier<?>[list.size()]);
    }

    private void parseDicomAttribute(String attrPath, List<String> values) {
        try {
            int[] tagPath = parseTagPath(attrPath);
            int tag = tagPath[tagPath.length-1];
            nestedKeys(tagPath).setString(tag, DICT.vrOf(tag),
                    values.toArray(new String[values.size()]));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(attrPath + "=" + values.get(0));
        } 
    }

    private Attributes nestedKeys(int[] tags) {
        Attributes item = keys;
        for (int i = 0; i < tags.length-1; i++) {
            int tag = tags[i];
            Sequence sq = item.getSequence(tag);
            if (sq == null)
                sq = item.newSequence(tag, 1);
            if (sq.isEmpty())
                sq.add(new Attributes());
            item = sq.get(0);
        }
        return item;
    }

    private static int[] parseTagPath(String attrPath) {
        return parseTagPath(StringUtils.split(attrPath, '.'));
    }

    private static int[] parseTagPath(String[] attrPath) {
        int[] tags = new int[attrPath.length];
        for (int i = 0; i < tags.length; i++)
            tags[i] = parseTag(attrPath[i]);
        return tags;
    }

    private static int parseTag(String tagOrKeyword) {
        try {
            return Integer.parseInt(tagOrKeyword, 16);
        } catch (IllegalArgumentException e) {
            int tag = DICT.tagForKeyword(tagOrKeyword);
            if (tag == -1)
                throw new IllegalArgumentException(tagOrKeyword);
            return tag;
        }
    }

    private enum Output {
        DICOM_XML {
            @Override
            Object entity(DefaultQidoRS service, Query query, QueryRetrieveLevel qrlevel) {
                return service.writeXML(query, qrlevel);
            }
        },
        JSON {
            @Override
            Object entity(DefaultQidoRS service, Query query, QueryRetrieveLevel qrlevel) {
                return service.writeJSON(query, qrlevel);
            }
        };
        
        abstract Object entity(DefaultQidoRS service, Query query, QueryRetrieveLevel qrlevel);
    }

    private Object writeXML(Query query, QueryRetrieveLevel qrlevel) {
        MultipartRelatedOutput output = new MultipartRelatedOutput();
        int count = 0;
        while (query.hasMoreMatches()) {
            Attributes tmp = query.nextMatch();
            if (tmp == null)
                continue;
            final Attributes match = adjust(tmp, qrlevel, query);
            LOG.debug("{}: Match #{}:\n{}", new Object[]{method, ++count, match});
            output.addPart(new StreamingOutput() {

                @Override
                public void write(OutputStream out) throws IOException,
                        WebApplicationException {
                    try {
                        SAXTransformer.getSAXWriter(new StreamResult(out)).write(match);
                    } catch (Exception e) {
                        throw new WebApplicationException(e);
                    }
                }},
                MediaTypes.APPLICATION_DICOM_XML_TYPE);
        }
        LOG.info("{}: {} Matches", method, count);
        return output;
    }

    private Object writeJSON(Query query, QueryRetrieveLevel qrlevel) {
        final ArrayList<Attributes> matches = new ArrayList<Attributes>();
        int count = 0;
        while (query.hasMoreMatches()) {
            Attributes tmp = query.nextMatch();
            if (tmp == null)
                continue;
            Attributes match = adjust(tmp, qrlevel, query);
            LOG.debug("{}: Match #{}:\n{}", new Object[]{method, ++count, match});
            matches.add(match);
        }
        LOG.info("{}: {} Matches", method, count);
        StreamingOutput output = new StreamingOutput(){

            @Override
            public void write(OutputStream out) throws IOException {
                try {
                    JsonGenerator gen = Json.createGenerator(out);
                    JSONWriter writer = new JSONWriter(gen);
                    gen.writeStartArray();
                    for (int i = 0, n=matches.size(); i < n; i++) {
                            Attributes match = matches.get(i);
                            writer.write(match);
                    }
                    gen.writeEnd();
                    gen.flush();
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                }
            }
        };
        return output;
    }

    private Attributes adjust(Attributes match, QueryRetrieveLevel qrlevel, Query query) {

        // response adjustment (e.g. timezone)
        try {
            queryService.coerceResponseAttributes(query.getQueryContext(), match);
        } catch (DicomServiceException e) {
            throw new WebApplicationException(e);
        }

        return filter(addRetrieveURL(match, qrlevel));
    }
    
    private Attributes addRetrieveURL(Attributes match, QueryRetrieveLevel qrlevel) {
        match.setString(Tag.RetrieveURL, VR.UR, RetrieveURL(match, qrlevel));
        return match;
    }

    private String RetrieveURL(Attributes match, QueryRetrieveLevel qrlevel) {
        StringBuilder sb = new StringBuilder(256);
        sb.append(uriInfo.getBaseUri())
          .append(aetitle)
          .append("/studies/")
          .append(match.getString(Tag.StudyInstanceUID));

        if (qrlevel == QueryRetrieveLevel.STUDY)
            return sb.toString();

        sb.append("/series/")
          .append(match.getString(Tag.SeriesInstanceUID));

        if (qrlevel == QueryRetrieveLevel.SERIES)
            return sb.toString();

        sb.append("/instances/")
          .append(match.getString(Tag.SOPInstanceUID));
        return sb.toString();
    }
    
    private Attributes filter(Attributes match) {
        if (includeAll)
            return match;

        Attributes filtered = new Attributes(match.size());
        filtered.addSelected(match, Tag.SpecificCharacterSet,
                Tag.RetrieveAETitle, Tag.InstanceAvailability, Tag.RetrieveURL);
        filtered.addSelected(match, keys);
        return filtered;
    }
}
