package telematics.rest;

import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by Chris Schraepen on 20/01/2017.
 */
public class ProcessRecordedEvents extends RecordedEvents {
    
	String postBody;
    String vehicle, dateFrom, dateTo;
    private static List<String> drivers;
    private static List<String> events;

    public ProcessRecordedEvents() {
    }

    public void parseArguments(String[] arguments) {
    	Boolean startDatePresent = false,
    			endDatePresent = false,
    			vehiclePresent = false;
        for (int i = 2; i < arguments.length; i ++) {
        	String argId = arguments[i].substring(0, arguments[i].indexOf("=") > 0 ? arguments[i].indexOf("=") : 0);
            if (arguments[i].startsWith("--VEHICLE=")) {
                vehicle = arguments[i].substring(10);
                
            }
            if (arguments[i].startsWith("--STARTDATE=")) {
                dateFrom = arguments[i].substring(12);
            }
            if (arguments[i].startsWith("--ENDDATE=")) {
                dateTo = arguments[i].substring(10);
            }
            if (arguments[i].startsWith("--DRIVERS=")) {
                drivers = parseArgument(arguments[i].substring(arguments[i].indexOf("=") + 1));
            }
            if (arguments[i].startsWith("--EVENTS=")) {
                events = parseArgument(arguments[i].substring(arguments[i].indexOf("=") + 1));
            }
        }
    }

    public void setBody() {
        postBody = "";
        if ((vehicle != null) && !vehicle.equals("")) {
            postBody = postBody + "<VehicleID>" + vehicle + "</VehicleID>";
            wsMethod = "GetVehicleEventsInDateRange";
            recordIdentifier = "GetVehicleEventsInDateRangeResult";
        }
        if ((drivers != null) && !drivers.equals("")) {
        	
            postBody = postBody + "<DriverIDs>";
            for (int i = 0; i < drivers.size(); i++){
            	postBody = postBody + "<short>" + drivers.get(i) + "</short>";
            }
            postBody = postBody + "</DriverIDs>";
            wsMethod = "GetEventsInDateRangeForDrivers";
            recordIdentifier = "GetEventsInDateRangeForDriversResult";
        }
        if (!(dateFrom.equals("") || dateTo.equals(""))) {
        	
            postBody = postBody
                    + "<StartDateTime>" + dateFrom + "</StartDateTime>"
                    + "<EndDateTime>" + dateTo + "</EndDateTime>";
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
