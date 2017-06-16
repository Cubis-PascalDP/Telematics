package telematics.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import utils.HTTPClient;
import utils.ResponseToOutputFormat;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Pascal De Poorter on 20/12/2016.
 */
public class Vehicle {
    static CloseableHttpClient httpClient;
    static HttpPost postRequest;
    String body = null;
    InputStream response;
    String wsMethod;
    String URL = "HTTP://api.fm-web.co.uk/webservices/AssetDataWebSvc/VehicleProcessesWS.asmx";

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

    public void getResponse() {
        try {
            HttpResponse httpResponse = HTTPClient.getResponse(postRequest);

            if(httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed: HTTP code " + httpResponse.getStatusLine().getReasonPhrase());
            }

            ResponseToOutputFormat.setResponse(httpResponse.getEntity().getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
