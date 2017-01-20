package telematics.rest;

import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * Created by depoorterp on 21/12/2016.
 */
public class ProcessSite extends Site {
    String postBody;

    public ProcessSite() {
        wsMethod = "GetSiteList";
        recordIdentifier = "Site";
    }

    public void parseArguments(String[] arguments) {

    }

    public void setBody() {
        postBody = "";
        try {
            String replaceBody = body.replaceAll("%method%", wsMethod).replaceAll("%body%", postBody);
            postRequest.setEntity(new StringEntity(replaceBody));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        Scanner s = new Scanner(response).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        return result;
    }
}
