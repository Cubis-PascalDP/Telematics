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
public class ProcessRecordedEventsVehicles extends RecordedEvents {
    
	String postBody;
    String dateFrom, dateTo;
    private static List<String> vehicles;
    private static List<String> events;

    public ProcessRecordedEventsVehicles() {
    }

    public void parseArguments(String[] arguments) {
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
        }
    }

    public void setBody() {
        postBody = "";
        if ((vehicles != null) && !vehicles.equals("")) {
            postBody = postBody + "<VehicleIDs>";
            for (int i = 0; i < vehicles.size(); i++){
            	postBody = postBody + "<short>" + vehicles.get(i) + "</short>";
            }
            postBody = postBody + "</VehicleIDs>";
            wsMethod = "GetEventsInDateRangeForVehicles";
            recordIdentifier = "GetEventsInDateRangeForVehiclesResult";
        }
        if (!(dateFrom.equals("") || dateTo.equals(""))) {
        	
            postBody = postBody
                    + "<StartDateTime>" + dateFrom + "</StartDateTime>"
                    + "<EndDateTime>" + dateTo + "</EndDateTime>";
        }
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
