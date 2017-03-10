package telematics.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;
import java.util.Properties;

/**
 * Created by Pascal De Poorter on 9/03/2017.
 */
public class Positioning {
    static CloseableHttpClient httpClient;
    static HttpPost postRequest;
    String id = null;
    String previousId = null;
    String body = null;
    String recordIdentifier = null;
    InputStream response;
    String wsMethod;
    Boolean continuous = false;
    Boolean withHeader = true;

    public Positioning() {
        if (httpClient == null) {
            httpClient = HTTPClient.createClient();
        }
        if (postRequest == null) {
            postRequest = new HttpPost("HTTP://api.fm-web.co.uk/webservices/AssetDataWebSvc/Positioning.asmx");
            postRequest.addHeader("Content-Type", "application/soap+xml");
        }

        body =    "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <soap12:Header>"
                + "    <TokenHeader xmlns=\"http://www.omnibridge.com/SDKWebServices/Positioning\">"
                + "      <Token>" + Token.getToken() + "</Token>"
                + "    </TokenHeader>"
                + "  </soap12:Header>"
                + "  <soap12:Body>"
                + "    <%method% xmlns=\"http://www.omnibridge.com/SDKWebServices/Positioning\">"
                + "      %body%"
                + "    </%method%>"
                + "  </soap12:Body>"
                + "</soap12:Envelope>";

    }

    public void getResponse() {
        try {
            HttpResponse httpResponse = HTTPClient.getResponse(postRequest);

            if (httpResponse != null) {
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("Failed: HTTP code " + httpResponse.getStatusLine().getReasonPhrase());
                }
                response = httpResponse.getEntity().getContent();
            } else {
                if (!isContinuous()) throw new RuntimeException("Error during getReponse()!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void withHeader() {
        this.withHeader = true;
    }

    public void withoutHeader() {
        this.withHeader = false;
    }

    public void parseToXML() {
        ProcessXMLResponse.parse(response, wsMethod);
    }

    public void parseToCSV() {
        try {
            ProcessXMLResponse.parseToCSV(response, recordIdentifier, withHeader);
            if ( previousId != null && previousId.equals(id)) {
                // Sleep for 5 minutes
                System.err.println("Sleeping for 5 minutes!");
                Thread.sleep(300000);
            } else {
                // Sleep for 30 Seconds.
                previousId = id;
                id = ProcessXMLResponse.getLastID();
                saveId(id);
                System.err.println("Sleeping for 30 seconds!");
                Thread.sleep(30000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isContinuous() { return continuous;}

    private void saveId(String eventID) {
        File rtPropFile = new File("runtime.properties");
        Properties rtProp = new Properties();
        try {
            rtProp.setProperty("eventID", eventID);
            FileOutputStream propOut = new FileOutputStream(rtPropFile);
            rtProp.store(propOut, "eventID");
            propOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getId() {
        File rtPropFile = new File("runtime.properties");
        Properties rtProp = new Properties();
        try {
            if (!rtPropFile.exists()) {
                throw new RuntimeException("No previous ID saved. --ID parameter needed)!");
            }
            else {
                FileInputStream rtFile = new FileInputStream("runtime.properties");
                rtProp.load(rtFile);
                String event = rtProp.getProperty("eventID");
                if (event != null && !event.equals("")) {
                    return event;
                } else
                {
                    throw new RuntimeException("No previous ID saved. --ID parameter needed)!");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
