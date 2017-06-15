package telematics.rest;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.http.entity.StringEntity;
import utils.ResponseToOutputFormat;

import java.io.UnsupportedEncodingException;

/**
 * This class retrieves the Event Description information using the Telematics api. The usage details for the
 * api can be found <a href="http://api.fm-web.co.uk/webservices/UnitConfigurationWebSvc/EventDescriptionProcessWS.asmx">here</a>
 * <p>
 * In relation to the command parameters different api methods are invoked:
 * <ul>
 * <li>No Parameters: <a href="http://api.fm-web.co.uk/webservices/UnitConfigurationWebSvc/EventDescriptionProcessWS.asmx?op=GetEventsList">GetEventList</a></li>
 * <li>--vehicle: <a href="http://api.fm-web.co.uk/webservices/UnitConfigurationWebSvc/EventDescriptionProcessWS.asmx?op=GetInUseEventsForVehicleID">GetInUseEventsForVehicleID</a></li>
 * <li>--eventid: <a href="http://api.fm-web.co.uk/webservices/UnitConfigurationWebSvc/EventDescriptionProcessWS.asmx?op=GetEventDescriptionDetails">GetEventDescriptionDetails</a></li>
 * </ul>
 *
 * @author  Chris Schraepen
 * @version 1.0
 * @since   20-01-2017
 */

@Parameters(commandDescription = "Process event description or events in use on vehicles")
public class ProcessEvents extends Events {
    @Parameter(names = "--vehicle", description = "Return list of event details for events defined for the given vehicle." +
                                       " Should not be combined with the --eventid parameter")
    @SuppressWarnings("unused")
    private String vehicle;
    @Parameter(names = "--eventid", description = "Return event details for the given event. Should not be combined with the " +
                                       " --vehicle parameter.")
    @SuppressWarnings("unused")
    private String event;

    String postBody;

    /**
     * Validates if a correct combination of parameters where passed to the command argument
     * @return boolean as result of the validation
     */
    public boolean parseArguments() {
        if ((vehicle != null && !vehicle.equals("")) &&
                (event != null && !event.equals(""))) {
            System.err.println("ProcessEvents: arguments --vehicle and --eventid should not be used together.");
            return false;
        }
        return true;
    }

    /**
     * In relations to the arguments given to the correct api method and result record identifiers are
     * determined.
     */
    public void setBody() {
        String wsMethod ="GetEventsList";
        ResponseToOutputFormat.setRecordID("EventDescription");
        postBody = "";
        if ((event != null) && !event.equals("")) {
            postBody = postBody + "<EventID>" + event + "</EventID>";
            wsMethod = "GetEventDescriptionDetails";
            ResponseToOutputFormat.setRecordID("GetEventDescriptionDetailsResult");
        }
        if ((vehicle != null) && !vehicle.equals("")) {
            postBody = postBody + "<VehicleID>" + vehicle + "</VehicleID>";
            wsMethod = "GetInUseEventsForVehicleID";
            ResponseToOutputFormat.setRecordID("GetInUseEventsForVehicleIDResult");
        }
        try {
            ResponseToOutputFormat.setMethod(wsMethod);
            String replaceBody = body.replaceAll("%method%", wsMethod).replaceAll("%body%", postBody);
            postRequest.setEntity(new StringEntity(replaceBody));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
