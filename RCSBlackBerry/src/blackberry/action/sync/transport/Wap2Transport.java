//#preprocess
package blackberry.action.sync.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;

import net.rim.device.api.io.IOCancelledException;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.util.ByteVector;
import blackberry.action.sync.Transport;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.utils.Check;
import blackberry.utils.Utils;

public class Wap2Transport extends Transport {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Wap2Transport", DebugLevel.VERBOSE);
    //#endif

    private String transportId;
    private String cookie;

    boolean stop;
    

    private final String HEADER_CONTENTTYPE = "content-type";
    private final String HEADER_SETCOOKIE = "set-cookie";
    private final String HEADER_CONTENTLEN = "content_length";

    private final String USER_AGENT = "Profile/MIDP-2.0 Configuration/CLDC-1.0";
    private final String CONTENT_TYPE = "application/octet-stream";    
    //private static String CONTENTTYPE_TEXTHTML = "text/html";

    public Wap2Transport(String host, int port) {
        super(host, port);
    }

    public boolean isAvailable() {
        transportId = getWap2TransportUid();
        return transportId != null;
    }

    public boolean initConnection() {
        url = "http://" + host + ":" + port + "/"
                + ";deviceside=true;ConnectionUID=" + transportId;
        cookie = null;
        stop = false;
        return true;
    }

    public void close() {

    }

    public byte[] command(byte[] data) throws TransportException {

        HttpConnection connection = sendHttpPostRequest(data);
        byte[] content = parseHttpConnection(connection);
        
        return content;
    }

    private static String getWap2TransportUid() {
        // Get the service book records for WAP2 transport.
        ServiceRecord[] records = ServiceBook.getSB().findRecordsByCid("WPTCP");
        for (int i = 0; i < records.length; i++) {
            // Determine if the current one is suitable.
            ServiceRecord record = records[i];
            if (record.isValid() && !record.isDisabled()) {
                String recordName = record.getName().toUpperCase();
                if (recordName.indexOf("WIFI") < 0
                        && recordName.indexOf("WI-FI") < 0) {
                    // Looks good so fire it back. 
                    return record.getUid();
                }
            }
        }

        // No WAP2 transport found.
        return null;
    }

    private HttpConnection sendHttpPostRequest(byte[] data)
            throws TransportException {
        //#ifdef DBC
        Check.requires(data != null, "sendHttpPostRequest: null data");
        //#endif
        String content = "";

        boolean httpOK;
        HttpConnection httpConn = null;

        // Open the connection and extract the data.
        try {
            StreamConnection s = null;
            s = (StreamConnection) Connector.open(getUrl());
            httpConn = (HttpConnection) s;
            httpConn.setRequestMethod(HttpConnection.POST);
            httpConn.setRequestProperty("User-Agent", USER_AGENT);
            httpConn.setRequestProperty("Content-Language", "en-US");

            if (cookie != null) {
                httpConn.setRequestProperty("Cookie", cookie);
            }
            
            httpConn.setRequestProperty("Content-Type",
                    CONTENT_TYPE );

            OutputStream os = null;
            os = httpConn.openOutputStream();
            os.write(data);
            os.flush(); // Optional, getResponseCode will flush

            int status = httpConn.getResponseCode();
            httpOK = (status == HttpConnection.HTTP_OK);

            os.close();

            //#ifdef DEBUG
            debug.trace("sendHttpPostRequest response: " + status);
            //#endif

        } catch (Exception ex) {
            throw new TransportException();
        }

        if (!httpOK) {
            throw new TransportException();
        }

        //#ifdef DBC
        Check.ensures(httpConn != null, "sendHttpPostRequest: httpConn null");
        //#endif     
        return httpConn;

    }

    private byte[] parseHttpConnection(HttpConnection httpConn) throws TransportException {

        try {
            // Is this html?
            String contentType = httpConn.getHeaderField(HEADER_CONTENTTYPE);
            boolean htmlContent = (contentType != null && contentType
                    .startsWith(contentType));

            if (!htmlContent) {
                throw new TransportException();
            }

            String setCookie = httpConn.getHeaderField(HEADER_SETCOOKIE);
            
            if(setCookie!=null){
                //#ifdef DEBUG
                debug.trace("parseHttpConnection setCookie: " + setCookie);
                //#endif
                
                cookie = setCookie;
            }
            
            String contentLen = httpConn.getHeaderField(HEADER_CONTENTLEN);

            // expected content size
            int totalLen = Integer.parseInt(contentLen);

            InputStream input = httpConn.openInputStream();

            // buffer data
            byte[] buffer = new byte[256];
            byte[] content = new byte[totalLen];
            int size = 0; // incremental size
            int len = 0; // iterative size

            while (-1 != (len = input.read(buffer))) {
                // Exit condition for the thread. An IOException is 
                // thrown because of the call to  httpConn.close(), 
                // causing the thread to terminate.
                if (stop) {
                    httpConn.close();
                    input.close();
                }
                Utils.copy(content, size, buffer, 0, len);
                size += len;
            }

            //#ifdef DEBUG
            debug.trace("parseHttpConnection received:" + size);
            //#endif

            input.close();

            //#ifdef DBC
            Check.ensures(len != totalLen, "sendHttpPostRequest: received:"
                    + size + " expected: " + totalLen);
            //#endif 
            return content;

        } catch (IOCancelledException e) {
            throw new TransportException();
        } catch (IOException e) {
            throw new TransportException();
        }

    }

}
