package telematics.rest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Created by Pascal De Poorter on 20/12/2016.
 */
public class TripProcesses {
    private static HttpClient httpClient;
    private static HttpPost httpPost;
    String body = null;

    public TripProcesses() {
        if (httpClient == null) {
            httpClient = HttpClientBuilder.create().build();
        }
        if (httpPost == null) {
            HttpPost postRequest = new HttpPost("HTTP://api.fm-web.co.uk/webservices/AssetDataWebSvc/TripProcessesWS.asmx");
        }

        body =    "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <soap12:Header>"
                + "    <TokenHeader xmlns=\"http://www.omnibridge.com/SDKWebServices/AssetData\">"
                + "      <Token>%token%</Token>"
                + "    </TokenHeader>"
                + "  </soap12:Header>"
                + "  <soap12:Body>"
                + "    <GetTripsWithTotalsForVehicleInDateRange xmlns=\"http://www.omnibridge.com/SDKWebServices/AssetData\">"
                + "      %body%"
                + "    </GetTripsWithTotalsForVehicleInDateRange>"
                + "  </soap12:Body>"
                + "</soap12:Envelope>";

    }
}
