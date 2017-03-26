package telematics.rest;

import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * Created by depoorterp on 21/12/2016.
 */
public class ProcessDriver extends Driver {
    String postBody;
    String driver;

    public ProcessDriver() {
        wsMethod = "GetDriverList";
        recordIdentifier = "Driver";
    }

    public void parseArguments(String[] arguments) {
        for (int i = 2; i < arguments.length; i ++) {
            if (arguments[i].startsWith("--DRIVER=")) {
                driver = arguments[i].substring(9);
            }
        }
    }

    public void setBody() {
        postBody = "";
        if ((driver != null) && !driver.equals("")) {
            postBody = postBody + "<DriverID>" + driver + "</DriverID>";
            wsMethod = "GetDriver";
            recordIdentifier = "GetDriverResult";
        }
        try {
            String replaceBody = body.replaceAll("%method%", wsMethod).replaceAll("%body%", postBody);
            postRequest.setEntity(new StringEntity(replaceBody));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setBody(Integer veh ) {
        postBody = postBody + "<DriverID>" + driver + "</DriverID>";
        try {
            postRequest.setEntity(new StringEntity(body.replaceAll("%method%",wsMethod).replaceAll("%body%", postBody)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();

        }
    }
    public String toString() {
        Scanner s = new Scanner(response).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        return result;
    }

    public boolean isContinuous() { return false;}
}
