package telematics.rest;

import org.apache.http.entity.StringEntity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by Chris Schraepen on 20/01/2017.
 */
public class ProcessRecordedEventsVehicles extends RecordedEvents {
    
	String postBody;
    String dateFrom, dateTo, x, vehicle, header;
    private static List<String> vehicles;
    private static List<String> events;

    public ProcessRecordedEventsVehicles() {
    }

    public void parseArguments(String[] arguments) {
        System.err.println("Starting Processing: ProcessRecordedEventsVehicle");
        for (int i = 2; i < arguments.length; i ++) {
        	String argId = arguments[i].substring(0, arguments[i].indexOf("=") > 0 ? arguments[i].indexOf("=") : 0);
            if (arguments[i].startsWith("--VEHICLES=")) {
            	 vehicles = parseArgument(arguments[i].substring(arguments[i].indexOf("=") + 1));       
            }
            if (arguments[i].startsWith("--STARTDATE=")) {
            	dateFrom = arguments[i].substring(arguments[i].indexOf("=") +1);
            }
            if (arguments[i].startsWith("--ENDDATE=")) {
            	dateTo = arguments[i].substring(arguments[i].indexOf("=") + 1);
            }
            if (arguments[i].startsWith("--EVENTS=")) {
            	events = parseArgument(arguments[i].substring(arguments[i].indexOf("=") + 1));
            }
            if (arguments[i].startsWith("--VEHICLE=")) {
            	vehicle = arguments[i].substring(arguments[i].indexOf("=") + 1);
            }
            //This argument reads the last X (integer) events of a given vehicle
            if (arguments[i].startsWith("--LASTXEVENTS=")) {
            	x = arguments[i].substring(arguments[i].indexOf("=") + 1);
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
                if (arguments[i].substring(arguments[i].indexOf("=") + 1).equals("Y"))
                this.continuous = true;
                else this.continuous = false;
            }
        }
    }

    public void setBody() {
        postBody = "";
        wsMethod = "GetEventsInDateRangeForVehicles";
        recordIdentifier = "RecordedEvent";
        //Check if a header needs to be generated when CSV output
        if ((header != null) && !header.equals("Y")) withoutHeader();
        else withHeader();
        //Optional ArrayOfShort in method GetEventsInDateRangeForVehicles
        if ((vehicles != null) && !vehicles.equals("")) {
            postBody = postBody + "<VehicleIDs>";
            for (int i = 0; i < vehicles.size(); i++){
            	postBody = postBody + "<short>" + vehicles.get(i) + "</short>";
            }
            postBody = postBody + "</VehicleIDs>";
        }
        //Mandatory dateTime in GetEventsInDateRangeForVehicles
        if (((dateFrom != null) && !dateFrom.equals("")) || ((dateTo != null) && !dateTo.equals(""))) {
        	
            postBody = postBody
                    + "<StartDateTime>" + dateFrom + "</StartDateTime>"
                    + "<EndDateTime>" + dateTo + "</EndDateTime>";
        }
        //Mandatory short in method GetVehicleEventsXMostRecent
        if ((vehicle != null) && !vehicle.equals("")) {
            postBody = postBody + "<VehicleID>" + vehicle + "</VehicleID>";     
            wsMethod = "GetVehicleEventsXMostRecent";
        }
        //Mandatory int in method GetVehicleEventsXMostRecent
        if ((x != null) && !x.equals("")) {
            postBody = postBody
                    + "<X>" + x + "</X>";
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
            wsMethod = "GetEventsSinceID";
        }
        //Optional ArrayOfShort in method GetEventsInDateRangeForVehicles
        //Optional ArrayOfShort in method GetVehicleEventsXMostRecent
        if ((events != null) && !events.equals("")) {
            postBody = postBody + "<EventDescriptionIDs>";
            for (int i = 0; i < events.size(); i++){
            	postBody = postBody + "<short>" + events.get(i) + "</short>";
            }
            postBody = postBody + "</EventDescriptionIDs>";
        }
        try {
            String replaceBody = body.replaceAll("%method%", wsMethod).replaceAll("%body%", postBody);
            postRequest.setEntity(new StringEntity(replaceBody));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    
    private static List<String> parseArgument(String argumentList) {
        List<String> elements = new ArrayList<>();
        if ( (  argumentList.length() > 0 ) &&
                argumentList.substring(0,1).equals("(") &&
                argumentList.substring(argumentList.length() - 1).equals(")"))
            elements = Arrays.asList(argumentList.substring(1,argumentList.length() - 1).split("\\s*,\\s*"));
        return elements;
    }
    

    public String toString() {
        Scanner s = new Scanner(response).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        return result;
    }
}
