package telematics.rest;


import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

/**
 * Created by depoorterp on 22/12/2016.
 */
public class HTTPClient {
    static CloseableHttpClient httpClient;
    static RequestConfig config;

    /* Create http Client and decide if proxy needs to be used */
    public static CloseableHttpClient createClient() {

        /* Check if running at STIB-MIVB */
        String userDomain = System.getenv("USERDOMAIN");
        if ( userDomain != null && userDomain.equals("STIB-MIVB")) {
            System.setProperty("https.proxyHost" , "proxy.stib-mivb.be");
            System.setProperty("https.proxyPort" , "3128");

            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

            if(System.getProperty("http.proxyUser") != null) {
                credentialsProvider.setCredentials( new AuthScope("proxy.stib-mivb.be", 3128),
                        new NTCredentials(System.getProperty("http.proxyUser"), System.getProperty("http.proxyPassword"),
                                null, userDomain));
            }

            httpClient = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();

            HttpHost proxy = new HttpHost("proxy.stib-mivb.be", 3128);
            config = RequestConfig.custom().setProxy(proxy).build();


        } else {
            httpClient = HttpClients.createDefault();
        }

        return httpClient;
    }

    public static HttpResponse getResponse(HttpPost post){
        if (config != null) {
            post.setConfig(config);
        }
        HttpResponse resp = null;
        try {
            resp = httpClient.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp;
    }

    public static void closeClient() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
