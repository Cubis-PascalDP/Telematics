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
public class ProcessRecordedEventsDrivers extends RecordedEvents {
    
	String postBody;
    String dateFrom, dateTo;
    private static List<String> drivers;
    private static List<String> events;

    public ProcessRecordedEventsDrivers() {
    }

    public void parseArguments(String[] arguments) {
        for (int i = 2; i < arguments.length; i ++) {
        	String argId = arguments[i].substring(0, arguments[i].indexOf("=") > 0 ? arguments[i].indexOf("=") : 0);
            if (arguments[i].startsWith("--STARTDATE=")) {
                dateFrom = arguments[i].substring(arguments[i].indexOf("=") +1);
            }
            if (arguments[i].startsWith("--ENDDATE=")) {
                dateTo = arguments[i].substring(arguments[i].indexOf("=") + 1);
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
        wsMethod = "GetEventsInDateRangeForDrivers";
        recordIdentifier = "GetEventsInDateRangeForDriversResult";
        //Optional ArrayOfShort in method GetEventsInDateRangeForDrivers
        if ((drivers != null) && !drivers.equals("")) {
            postBody = postBody + "<DriverIDs>";
            for (int i = 0; i < drivers.size(); i++){
            	postBody = postBody + "<short>" + drivers.get(i) + "</short>";
            }
            postBody = postBody + "</DriverIDs>";
        }
        //Mandatory dateTime in GetEventsInDateRangeForDrivers
        if (((dateFrom != null) && !dateFrom.equals("")) || ((dateTo != null) && !dateTo.equals(""))) {  	
            postBody = postBody
                    + "<StartDateTime>" + dateFrom + "</StartDateTime>"
                    + "<EndDateTime>" + dateTo + "</EndDateTime>";
        }
        //Optional ArrayOfShort in method GetEventsInDateRangeForDrivers
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
