package telematics.rest;

import org.apache.http.entity.StringEntity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * Created by Pascal De Poorter on 9/03/2017.
 */
public class ProcessPositions extends Positioning {

	String postBody;
    private String vehicle, dateFrom, dateTo, header;

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
            if (arguments[i].startsWith("--ID=")) {
                id = arguments[i].substring(arguments[i].indexOf("=") + 1);
            }
            if (arguments[i].startsWith("--HEADER=")) {
                header = arguments[i].substring(arguments[i].indexOf("=") + 1);
            }
            if (arguments[i].startsWith("--LOG=")) {
                File file = new File(arguments[i].substring(arguments[i].indexOf("=") + 1));
                PrintStream ps = null;
                try {
                    ps = new PrintStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                System.setErr(ps);
            }
            if (arguments[i].startsWith("--CONTINUOUS=")) {
                this.continuous = arguments[i].substring(arguments[i].indexOf("=") + 1).equals("Y");
            }
        }
    }

    public void setBody() {
        postBody = "";
        wsMethod = "GetGPSPositionsForVehicleInDateRange";
        recordIdentifier = "GPSPosition";
        //Check if a header needs to be generated when CSV output
        if ((header != null) && !header.equals("Y")) withoutHeader();
        else withHeader();
        //Optional VehicleID
        if ((vehicle != null) && !vehicle.equals("")) {
                postBody = postBody + "<VehicleID>" + vehicle + "</VehicleID>";
        }
        //Mandatory dateTime in GetEventsInDateRangeForVehicles
        if (((dateFrom != null) && !dateFrom.equals("")) || ((dateTo != null) && !dateTo.equals(""))) {
            postBody = postBody
                    + "<StartDate>" + dateFrom + "</StartDate>"
                    + "<EndDate>" + dateTo + "</EndDate>";
        }
        //In case continuous is active and no ID was given see if a lastevent properties file exists
        //and retrieve event id from properties file
        if (isContinuous() && id == null) {
            id = getId();
        }
        //Mandatory int in method GetEventsSinceID
        if ((id != null) && !id.equals("")) {
            postBody = postBody
                    + "<ID>" + id + "</ID>";
            wsMethod = "GetGPSPositionsSinceID";
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
        return  s.hasNext() ? s.next() : "";
    }
}
