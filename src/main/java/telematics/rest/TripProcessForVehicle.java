package telematics.rest;

import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * Created by depoorterp on 21/12/2016.
 */
public class TripProcessForVehicle extends TripProcesses {
    String postBody;
    int vehicle;
    String wsMethod, dateFrom, dateTo;

    public TripProcessForVehicle() {
        wsMethod = "GetTripsWithTotalsForVehicleInDateRange";
        recordIdentifier = "TripWithTotals";
    }

    public void setBody(int veh, String startDate, String toDate ) {
        this.vehicle = veh;
        this.dateFrom = startDate;
        this.dateTo = toDate;
        postBody = "<vehicleId>" + Integer.toString(veh) + "</vehicleId>"
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
