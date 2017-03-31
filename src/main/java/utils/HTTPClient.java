package utils;


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

import static telematics.GetTelematicsData.ta;

/**
 * This class administrates the connection with the internet in order to allow RESTFUL interogations to the
 * Telematics api
 *
 * @author  Pascal De Poorter
 * @version 1.0
 * @since   01-01-2017
 */
public class HTTPClient {

    private static CloseableHttpClient httpClient;
    private static RequestConfig config;

    /**
     * Initializes the http client.
     */
    public static void createClient() {

        /* Check if running at behind proxy */
        if ( ta.getProxyHost() != null && !ta.getProxyHost().equals("")) {

            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

            if(!ta.getProxyUser().equals("")) {
                credentialsProvider.setCredentials( new AuthScope(ta.getProxyHost(), ta.getProxyPort()),
                        new NTCredentials(ta.getProxyUser(), ta.getProxyPassword(),
                                null, "STIB-MIVB"));
            }

            httpClient = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();

            HttpHost proxy = new HttpHost(ta.getProxyHost(), ta.getProxyPort());
            config = RequestConfig.custom().setProxy(proxy).build();


        } else {
            httpClient = HttpClients.createDefault();
        }
    }

    /**
     * Post the api body through the http Client and captures the response
     *
     * @param post Post message created by the command processing class
     * @return Response message returned by the Http Client
     */
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

    /**
     * Closes the Http Client
     */
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
