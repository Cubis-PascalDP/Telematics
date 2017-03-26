package telematics.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import telematics.GetTelematicsData;
import utils.HTTPClient;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Properties;

/**
 * Created by Pascal De Poorter on 20/12/2016.
 */
public class Token {
    static String token;
    private static String user, pw;

    public static void createToken() {

        String Body;

        readTelematicsCredentials();

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

            HttpResponse response = HTTPClient.getResponse(postRequest);

            if(response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed: HTTP code " + response.getStatusLine().getReasonPhrase());
            }

            is = response.getEntity().getContent();

        } catch (IOException e) {
            e.printStackTrace();
        }

        token = getTokenfromXML( is );

//        HTTPClient.closeClient();
    }

    public static String getToken() {
        // Check if a token already exists
        File tokenPropFile = new File("runtime.properties");
        Properties tokenProp = new Properties();
        try {
            if (!tokenPropFile.exists()) {

                createToken();
                tokenProp.setProperty("token", token);
                FileOutputStream propOut = new FileOutputStream(tokenPropFile);
                tokenProp.store(propOut, "Token");
                propOut.close();
            }
            else {
                FileInputStream profFile = new FileInputStream("runtime.properties");
                tokenProp.load(profFile);
                token = tokenProp.getProperty("token");
                if (!validateToken()) {
                    createToken();
                    tokenProp.setProperty("token", token);
                    FileOutputStream propOut = new FileOutputStream(tokenPropFile);
                    tokenProp.store(propOut, "Token");
                    propOut.close();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return token;
    }

    private static boolean validateToken() {

        Boolean validToken = true;
        String Body;

        Body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        Body = Body + "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">";
        Body = Body + "  <soap12:Body>";
        Body = Body + "    <GetUserContext xmlns=\"http://www.omnibridge.com/SDKWebServices/Core\">";
        Body = Body + "      <Token>" + token + "</Token>";
        Body = Body + "    </GetUserContext>";
        Body = Body + "  </soap12:Body>";
        Body = Body + "</soap12:Envelope>";

        /* Get Authentication token from Web service */
        HttpPost postRequest = new HttpPost("HTTP://api.fm-web.co.uk/webservices/CoreWebSvc/CoreWS.asmx");
        InputStream is = null;

        try {
            postRequest.addHeader("Content-Type", "application/soap+xml");
            postRequest.setEntity(new StringEntity(Body));

            HttpResponse response = HTTPClient.getResponse(postRequest);

            if(response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed: HTTP code " + response.getStatusLine().getReasonPhrase());
            } else {
                is = response.getEntity().getContent();
                String id = getIDfromXML(is);
                if (id.equals("")) validToken = false;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        HTTPClient.closeClient();

        return validToken;
    }

    private static String getIDfromXML(InputStream is) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = null;
        Node node = null;
        try {
            docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse(is);
            node = doc.getElementsByTagName("ID").item(0);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (node == null) return "";
            else return node.getChildNodes().item(0).getNodeValue();
    }

    private static String getTokenfromXML(InputStream is) {
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

    private static void readTelematicsCredentials() {

        try {
            InputStream propFile = GetTelematicsData.class.getResourceAsStream("/telematics.properties");
            Properties prop = new Properties();
            prop.load(propFile);
            user = prop.getProperty("user");
            pw = prop.getProperty("password");
            propFile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
