package telematics.rest;

import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * Created by Chris Schraepen on 20/01/2017.
 */
public class ProcessEvents extends Events {
    String postBody;
    String event, vehicle;

    public ProcessEvents() {
        wsMethod = "GetEventsList";
        recordIdentifier = "EventDescription";
    }

    public void parseArguments(String[] arguments) {
        for (int i = 2; i < arguments.length; i ++) {
            if (arguments[i].startsWith("--EVENT=")) {
                event = arguments[i].substring(8);
            }
            if (arguments[i].startsWith("--VEHICLE=")) {
                vehicle = arguments[i].substring(10);
            }
        }
    }

    public void setBody() {
        postBody = "";
        if ((event != null) && !event.equals("")) {
            postBody = postBody + "<EventID>" + event + "</EventID>";
            wsMethod = "GetEventDescriptionDetails";
            recordIdentifier = "GetEventDescriptionDetailsResult";
        }
        if ((vehicle != null) && !vehicle.equals("")) {
            postBody = postBody + "<VehicleID>" + vehicle + "</VehicleID>";
            wsMethod = "GetInUseEventsForVehicleID";
            recordIdentifier = "GetInUseEventsForVehicleIDResult";
        }
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
