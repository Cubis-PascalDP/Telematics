package telematics.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import utils.HTTPClient;
import utils.ResponseToOutputFormat;

import java.io.IOException;
import java.io.InputStream;


/**
 * This class is the master class for accessing the following Telematics end-point
 * <a href="http://api.fm-web.co.uk/webservices/CommunicationsWebSvc/MessageProcessesWS.asmx">http://api.fm-web.co.uk/webservices/CommunicationsWebSvc/MessageProcessesWS.asmx.</a>
 *
 * @author  Pascal De Poorter
 * @version 1.0
 * @since   03-07-2017
 */
public class Messages {
    static HttpPost postRequest;
    String body = null;
    InputStream response;
    String wsMethod;
    String URL = "http://api.fm-web.co.uk/webservices/CommunicationsWebSvc/MessageProcessesWS.asmx";

    /**
     * This method initializes the body for methods related to the Telematics <a href="http://api.fm-web.co.uk/webservices/CommunicationsWebSvc/MessageProcessesWS.asmx">MessageProcessesWS</a> api.
     */
    @SuppressWarnings("unused")
    public void initialize() {
        if (postRequest == null) {
            postRequest = new HttpPost(URL);
            postRequest.addHeader("Content-Type", "application/soap+xml");
        }

        ResponseToOutputFormat.setURIWSDL(URL);

        body =    "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <soap12:Header>"
                + "    <TokenHeader xmlns=\"http://www.omnibridge.com/SDKWebServices/Communications\">"
                + "      <Token>" + Token.getToken() + "</Token>"
                + "    </TokenHeader>"
                + "  </soap12:Header>"
                + "  <soap12:Body>"
                + "    <%method% xmlns=\"http://www.omnibridge.com/SDKWebServices/Communications\">"
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
