package telematics.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by Pascal De Poorter on 20/12/2016.
 */
public class Token {
    static String token;

    public static String getToken(String user, String pw) {

        HttpClient httpClient = HttpClientBuilder.create().build();

        String Body;

        Body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        Body = Body + "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">";
        Body = Body + "  <soap12:Body>";
        Body = Body + "    <Login xmlns=\"http://www.omnibridge.com/SDKWebServices/Core\">";
        Body = Body + "      <UserName>" + user + "</UserName>";
        Body = Body + "      <Password>" + pw + "</Password>";
        Body = Body + "    </Login>";
        Body = Body + "  </soap12:Body>";
        Body = Body + "</soap12:Envelope>";

        /* Get Authentication token from Web service */
        HttpPost postRequest = new HttpPost("HTTP://api.fm-web.co.uk/webservices/CoreWebSvc/CoreWS.asmx");
        InputStream is = null;

        try {
            postRequest.addHeader("Content-Type", "application/soap+xml");
            postRequest.setEntity(new StringEntity(Body));

            HttpResponse response = httpClient.execute(postRequest);

            if(response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed: HTTP code " + response.getStatusLine().getReasonPhrase());
            }

            is = response.getEntity().getContent();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return getTokenfromXML( is );
    }

    static String getTokenfromXML(InputStream is) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = null;
        Node tokenNode = null;
        try {
            docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse(is);
            tokenNode = doc.getElementsByTagName("Token").item(0);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return tokenNode.getChildNodes().item(0).getNodeValue();
    }

}
