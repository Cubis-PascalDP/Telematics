package telematics.rest;

import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * Created by depoorterp on 21/12/2016.
 */
public class ProcessVehicle extends Vehicle {
    String postBody;
    String vehicle;

    public ProcessVehicle() {
        wsMethod = "GetVehiclesList";
        recordIdentifier = "Vehicle";
    }

    public void parseArguments(String[] arguments) {
        for (int i = 2; i < arguments.length; i ++) {
            if (arguments[i].startsWith("--VEHICLE=")) {
                vehicle = arguments[i].substring(10);
            }
        }
    }

    public void setBody() {
        postBody = "";
        if ((vehicle != null) && !vehicle.equals("")) {
            postBody = postBody + "<VehicleID>" + vehicle + "</VehicleID>";
            wsMethod = "GetVehicle";
            recordIdentifier = "GetVehicleResult";
        }
        try {
            String replaceBody = body.replaceAll("%method%", wsMethod).replaceAll("%body%", postBody);
            postRequest.setEntity(new StringEntity(replaceBody));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setBody(Integer veh ) {
        postBody = postBody + "<VehicleID>" + vehicle + "</VehicleID>";
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
}
