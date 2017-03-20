package telematics.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import utils.HTTPClient;
import utils.ResponseToOutputFormat;

import java.io.*;


/**
 * This class is the master class for accessing the following Telematics end-point
 * <a href="HTTP://api.fm-web.co.uk/webservices/AssetDataWebSvc/RecordedEventProcessesWS.asmx">HTTP://api.fm-web.co.uk/webservices/AssetDataWebSvc/RecordedEventProcessesWS.asmx.</a>
 *
 * @author  Chris Schraepen
 * @version 1.0
 * @since   20-01-2017
 */
public class RecordedEvents {
    static HttpPost postRequest;
    String body = null;
    String recordIdentifier = null;
    InputStream response;
    String wsMethod;

    /**
     * This method initializes the body for methods related to the Telematics <a href="http://api.fm-web.co.uk/webservices/AssetDataWebSvc/RecordedEventProcessesWS.asmx">RecordedEventProcessesWS</a> api.
     */
    @SuppressWarnings("unused")
    public void initialize() {
        if (postRequest == null) {
            postRequest = new HttpPost("HTTP://api.fm-web.co.uk/webservices/AssetDataWebSvc/RecordedEventProcessesWS.asmx");
            postRequest.addHeader("Content-Type", "application/soap+xml");
        }

        body =    "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <soap12:Header>"
                + "    <TokenHeader xmlns=\"http://www.omnibridge.com/SDKWebServices/AssetData\">"
                + "      <Token>" + Token.getToken() + "</Token>"
                + "    </TokenHeader>"
                + "  </soap12:Header>"
                + "  <soap12:Body>"
                + "    <%method% xmlns=\"http://www.omnibridge.com/SDKWebServices/AssetData\">"
                + "      %body%"
                + "    </%method%>"
                + "  </soap12:Body>"
                + "</soap12:Envelope>";
    }

    /**
     * Get the response from the Telematics api.
     */
    public void getResponse() {
        try {
            HttpResponse httpResponse = HTTPClient.getResponse(postRequest);

            if (httpResponse != null) {
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("Failed: HTTP code " + httpResponse.getStatusLine().getReasonPhrase());
                }

                ResponseToOutputFormat.setResponse(httpResponse.getEntity().getContent());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
