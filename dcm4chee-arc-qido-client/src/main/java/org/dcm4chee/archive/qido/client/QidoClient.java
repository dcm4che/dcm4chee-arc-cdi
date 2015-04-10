package org.dcm4chee.archive.qido.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.json.Json;
import javax.ws.rs.core.MediaType;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONReader.Callback;
import org.dcm4che3.util.SafeClose;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QidoClient {

    private static final Logger LOG = LoggerFactory.getLogger(QidoClient.class);

    private QidoContext context;

    public QidoClient(QidoContext context) {
        super();
        this.context = context;
    }

    public Collection<String> verifyStorage(Collection<String> sopInstanceUIDs) {
        ArchiveAEExtension aeExt = context.getArchiveAEExtension();
        String aeTitle = context.getRemoteAE().getAETitle();
        String url = adjustToQidoURL(aeTitle, context.getRemoteBaseURL());
        if(aeExt == null) {
            throw new IllegalArgumentException("ArchiveAEExtension "
                    + "not initialized in context");
        }
        ArrayList<String> verifiedSopUIDs = new ArrayList<String>();
        MediaType type = MediaType.valueOf(aeExt.getQidoClientAcceptType() 
                !=null ? aeExt.getQidoClientAcceptType(): "application/json");
        
        for (String sopiuid : sopInstanceUIDs) {
            if (queryOverWebService(aeTitle, url, sopiuid, context.isFuzzyMatching()
                    ,context.isTimeZoneAdjustment(), type))
                verifiedSopUIDs.add(sopiuid);
        }
        return verifiedSopUIDs;
    }

    private boolean queryOverWebService(String aeTitle, String url, String sopUID,
            boolean fuzzyMatching, boolean timeZoneAdjustment
            , MediaType type) {
        HttpURLConnection connection = null;;
        boolean verified = false;
        
        try {
            url+="?SOPInstanceUID=" + sopUID;
            
            if(timeZoneAdjustment)
                url+="&timezoneadjustment=true";
            if(fuzzyMatching)
                url+="&fuzzymatching=true";
            
            URL qidoURL = new URL(url);
            
            connection = setUpQidoConnection(qidoURL, type);
            
            InputStream in = connection.getInputStream();
            
                if(type.isCompatible(MediaType.APPLICATION_JSON_TYPE))
                    verified = readJSON(in);
                else
                    verified = readXML(in);
            
        } catch (IOException e) {
            LOG.error("Error writing to http data output stream"
                    + e.getStackTrace().toString());
            return false;
        }
        finally {
            connection.disconnect();
        }
        
        return verified;
    }

    private boolean readXML(InputStream in) {
        String full="";
        String str;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String boundary = null;
        try {
            boundary = reader.readLine();
            
        while((str = reader.readLine())!=null) {
            full+=str;
        }
        } catch (IOException e) {
            LOG.error("Failed to read Input Stream {}", e);
        }
        finally{
            try {
                reader.close();
            } catch (IOException e) {
                LOG.error("Failed to close Stream {}", e);
            }
        }
        String[] parts = full.split(boundary);
        Attributes attrs = new Attributes();
        
        for(int i=0;i<parts.length-1;i++) {
                try {
                    attrs = SAXReader.parse(new ByteArrayInputStream(removeXMLHeader(parts[i]).getBytes()));
                } catch (Exception e) {
                    LOG.error("Error while parsing XML stream", e);
                }
                if(attrs.getString(Tag.InstanceAvailability) != null
                        && attrs.getString(Tag.InstanceAvailability)
                        .equalsIgnoreCase("ONLINE"))
                    return true;
        }
        return false;
    }

    private boolean readJSON(InputStream in) {
        try {
            JSONReader reader = null;
            
            try {
                reader = new JSONReader(
                        Json.createParser(new InputStreamReader(in, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                LOG.error("Unsupported encoding exception"
                        + " while parsing json stream", e);
            }
            
            final ArrayList<Attributes> attrs = new ArrayList<Attributes>();
            
            reader.readDatasets(new Callback() {
                
                @Override
                public void onDataset(Attributes fmi, Attributes dataset) {
                    attrs.add(dataset);
                }
            });
            if(attrs.get(0).getString(Tag.InstanceAvailability) != null
                    && attrs.get(0).getString(Tag.InstanceAvailability)
                    .equalsIgnoreCase("ONLINE"))
                return true;
        } finally {
                SafeClose.close(in);
        }
        return false;
    }



    private HttpURLConnection setUpQidoConnection(URL url , MediaType type) 
            throws IOException{
            HttpURLConnection connection = (HttpURLConnection) url
            .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", type.toString());
            connection.setRequestProperty("charset", "utf-8");
            connection.setUseCaches(false);
            return connection;
    }


    private String adjustToQidoURL(String aeTitle, String remoteBaseURL) {
     String qidoPath = "qido/"+aeTitle+"/studies";
     return remoteBaseURL.endsWith("/") 
             ? remoteBaseURL + qidoPath
                     : remoteBaseURL + "/" + qidoPath;
    }

    private String removeXMLHeader(String str) {
        String buff="";
        
        for(int i=0;i<str.length();i++)
        if(str.charAt(i) == '<') {
            if(str.charAt(i+1)=='?') {
                buff+=str.substring(i,str.length());
                break;
            }
        }
        return buff;
    }

}
