package telematics.rest;

import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * Created by depoorterp on 21/12/2016.
 */
public class TripProcessForVehicle extends TripProcesses {
    String postBody;
    String wsMethod, vehicle, dateFrom, dateTo;

    public TripProcessForVehicle() {
        wsMethod = "GetTripsWithTotalsForVehicleInDateRange";
        recordIdentifier = "TripWithTotals";
    }

    public void parseArguments(String[] arguments) {
        for (int i = 2; i < arguments.length; i ++) {
            if (arguments[i].startsWith("--VEHICLE=")) {
                vehicle = arguments[i].substring(10);
            }
            if (arguments[i].startsWith("--STARTDATE=")) {
                dateFrom = arguments[i].substring(12);
            }
            if (arguments[i].startsWith("--ENDDATE=")) {
                dateTo = arguments[i].substring(10);
            }
        }
    }

    public void setBody() {
        postBody = "";
        if (!vehicle.equals("")) {
            postBody = postBody + "<vehicleId>" + vehicle + "</vehicleId>";
        }
        if (!(dateFrom.equals("") || dateTo.equals(""))) {
            postBody = postBody
                    + "<StartDateTime>" + dateFrom + "</StartDateTime>"
                    + "<EndDateTime>" + dateTo + "</EndDateTime>";
        }
        try {
            postRequest.setEntity(new StringEntity(body.replaceAll("%method%", wsMethod).replaceAll("%body%", postBody)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setBody(Integer veh, String startDate, String toDate ) {
        postBody = postBody + "<vehicleId>" + Integer.toString(veh) + "</vehicleId>";
        postBody = postBody
                + "<StartDateTime>" + startDate + "</StartDateTime>"
                + "<EndDateTime>" + toDate + "</EndDateTime>";
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
