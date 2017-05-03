package telematics.rest;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.http.entity.StringEntity;
import utils.DateTimeValidator;
import utils.ResponseToOutputFormat;
import utils.IntegerBiggerThan0Validator;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static telematics.GetTelematicsData.ta;


/**
 * This class retrieves the Recorded Event Description information using the Telematics api. The usage details for the
 * api can be found <a href="http://api.fm-web.co.uk/webservices/AssetDataWebSvc/RecordedEventProcessesWS.asmx">here</a>
 * <p>
 * In relation to the command parameters different api methods are invoked:
 * <ul>
 * <li><i>Default</i>: <a href="http://api.fm-web.co.uk/webservices/AssetDataWebSvc/RecordedEventProcessesWS.asmx?op=GetEventsInDateRangeForVehicles">GetEventsInDateRangeForVehicles</a></li>
 * <li>--vehicles: <a href="http://api.fm-web.co.uk/webservices/AssetDataWebSvc/RecordedEventProcessesWS.asmx?op=GetEventsInDateRangeForVehicles">GetEventsInDateRangeForVehicles</a></li>
 * <li>--drivers: <a href="http://api.fm-web.co.uk/webservices/AssetDataWebSvc/RecordedEventProcessesWS.asmx?op=GetEventsInDateRangeForDrivers">GetEventsInDateRangeForDrivers</a></li>
 * <li>--vehicle: Different vehicle related apis are called depending on the given arguments combinations:
 * <ul>
 *     <li>--startdate/--enddate: <a href="http://api.fm-web.co.uk/webservices/AssetDataWebSvc/RecordedEventProcessesWS.asmx?op=GetVehicleEventsInDateRange">GetVehicleEventsInDateRange</a></li>
 *     <li>--lastXevents: <a href="http://api.fm-web.co.uk/webservices/AssetDataWebSvc/RecordedEventProcessesWS.asmx?op=GetVehicleEventsXMostRecent">GetVehicleEventsXMostRecent</a></li>
 * </ul>
 * <li>--id and/or --continuous: <a href="http://api.fm-web.co.uk/webservices/AssetDataWebSvc/RecordedEventProcessesWS.asmx?GetEventsSinceID">GetEventsSinceID</a></li>
 * </ul>
 *
 * @author  Chris Schraepen
 * @version 1.0
 * @since   20-01-2017
 */
@Parameters(commandDescription = "Process recorded vehicle events.")
public class ProcessRecordedEvents extends RecordedEvents {
    
	private String postBody;
	@Parameter(names = "--lastXevents", description = "get the records from the api for the X most recent events. Should be " +
                                "used in combination with the --vehicle argument!")
    @SuppressWarnings("unused")
	private String x;
	@Parameter(names = "--vehicle", description = "vehicle number for which the api should return the records. Only" +
                                        "in combination with --lastXevents argument!", validateWith = IntegerBiggerThan0Validator.class)
    @SuppressWarnings("unused")
	private Integer vehicle;
	@Parameter(names = "--startdate", description = "Start Date/Time offset (yyyy-mm-dd'T'hh:mm). Also --enddate should be provided as argument", validateWith = DateTimeValidator.class)
    @SuppressWarnings("unused")
	private String dateFrom;
    @Parameter(names = "--enddate", description = "End Date/Time offset (yyyy-mm-dd'T'hh:mm). Needs to be combined with --startdate", validateWith = DateTimeValidator.class)
    @SuppressWarnings("unused")
    private String  dateTo;
    @Parameter(names = "--vehicles", variableArity = true, description = "List of vehicles for which to retrieve information. Needs to be in combination with 'dates' arguments. Cannot be in combination with 'drivers' arguments.", validateWith = IntegerBiggerThan0Validator.class)
    @SuppressWarnings({"unused","MismatchedQueryAndUpdateOfCollection"})
    private List<Integer> vehicles;
    @Parameter(names = "--drivers", description = "List of driver IDs to use as a filter. Needs to be in combination with 'dates' arguments. Cannot be in combination with 'vehicle(s)' arguments.")
    @SuppressWarnings({"unused","MismatchedQueryAndUpdateOfCollection"})
    private List<Integer> drivers;
    @Parameter(names = "--events", description = "List of event IDs to use as a filter!")
    @SuppressWarnings({"unused","MismatchedQueryAndUpdateOfCollection"})
    private List<Integer> events;
    @Parameter(names = "--ID", description = "get the records from the api since the given ID. Can only be used in combination with --events")
    @SuppressWarnings("unused")
    private Integer id;
    @Parameter(names = "--MAXID", description = "get the records from the api till the given ID. Can only be used in combination with --events")
    @SuppressWarnings("unused")
    private Integer maxId;
    @Parameter(names = "--continuous", description = "Continuously process the api using the GetEventsSinceID. " +
                                         "Processing starts from the event passed with argument --ID or with the last saved event id in properties.")
    @SuppressWarnings("unused")
    private boolean continuous = false;

    /**
     * Validates if a correct combination of parameters where passed to the command argument
     * @return boolean as result of the validation
     */
    public boolean parseArguments() {
        String message = "";
        // GetVehicleEventsXMostRecent valid body parameter check.
        if ((x != null) && !x.equals("")) {
            if (vehicle == null) {
                message = message + "--lastXevents should be used in combination with --vehicle\n";
            }
            if (dateFrom != null) {
                message = message + "--lastXevents should not be used in combination with --startdate\n";
            }
            if (dateTo != null) {
                message = message + "--lastXevents should not be used in combination with --enddate\n";
            }
            if (vehicles != null) {
                message = message + "--lastXevents should not be used in combination with --vehicles\n";
            }
            if (id != null) {
                message = message + "--lastXevents should not be used in combination with --ID\n";
            }
            if (!message.equals("")) {
                System.err.println(message);
                return false;
            }
        }
        // GetEventsSinceID valid body parameter check.
        if (id != null || continuous)  {
            if (vehicle != null) {
                message = message + "--ID or --continuous should not be used in combination with --vehicle\n";
            }
            if (dateFrom != null) {
                message = message + "--ID or --continuous should not be used in combination with --startdate\n";
            }
            if (dateTo != null) {
                message = message + "--ID or --continuous should not be used in combination with --enddate\n";
            }
            if (vehicles != null) {
                message = message + "--ID or --continuous should not be used in combination with --vehicles\n";
            }
            if (id == null && ta.getLastEventID() == null && continuous) {
                message = message + "--continuous runs for the first time for command method. Should initialized using --ID.\n";
            }

            if (!message.equals("")) {
                System.err.println(message);
                return false;
            }
        }
        // Validate dates argument usage
        if ((dateFrom != null) && !dateFrom.equals("")) {
            if (dateTo == null) {
                System.err.println("--startdate should be combined with --enddate");
                return false;
            }
        }
        if ((dateTo != null) && !dateTo.equals("")) {
            if (dateFrom == null) {
                System.err.println("--enddate should be combined with --startdate");
                return false;
            }
        }
        // Validate vehicle/driver argument usage
        if ((vehicle != null) && (vehicles != null)) {
            System.err.println("--vehicle and --vehicles should not be used together.");
            return false;
        }
        if ((drivers != null) && ((vehicle != null) || (vehicles != null))) {
            System.err.println("--drivers and --vehicle(s) arguments cannot be combined");
            return false;
        }
        // Validate maxId
        if ((maxId != null) && (id == null)) {
            System.err.println("--MAXID should be used in combination with --ID.");
            return false;
        }

        // Validation of arguments OK
        return true;
    }

    /**
     * In relations to the arguments given to the correct api method and result record identifiers are
     * determined.
     */
    public void setBody() {
        postBody = "";
        String wsMethod ="GetEventsInDateRangeForVehicles";
        ResponseToOutputFormat.setRecordID("RecordedEvent");

        //Mandatory short in method GetVehicleEventsXMostRecent
        if ((x != null) && !x.equals("")) {
            postBody = postBody
                    + "<X>" + x + "</X>";
            wsMethod = "GetVehicleEventsXMostRecent";
        }
        //Mandatory Integer in method GetEventsSinceID
        if (!(id == null || continuous)) {
            postBody = postBody
                    + "<ID>" + id + "</ID>";
            wsMethod = "GetEventsSinceID";
        }
        // continuous uses GetEventsSinceID method
        ta.setContinuous(continuous);
        ta.setMaxID(maxId);

        if (continuous) {
            if (id != null) {
                ta.initLastEventID(id);
                id = null;
            }
            postBody = postBody
                    + "<ID>" + ta.getLastEventID() + "</ID>";
            wsMethod = "GetEventsSinceID";
        }

        //Mandatory short in method GetVehicleEventsXMostRecent, GetVehicleEventsInDateRange
        if (vehicle != null) {
            postBody = postBody + "<VehicleID>" + vehicle + "</VehicleID>";
            if (x == null) wsMethod = "GetVehicleEventsInDateRange";
        }
        //Optional ArrayOfShort in method GetEventsInDateRangeForVehicles
        //Optional ArrayOfShort in method GetVehicleEventsXMostRecent
        if (events != null) {
            postBody = postBody + "<EventDescriptionIDs>";
            events.forEach(event ->  postBody = postBody + "<short>" + event + "</short>");
            postBody = postBody + "</EventDescriptionIDs>";
        }
        //Mandatory dateTime in GetEventsInDateRangeForVehicles
        if (((dateFrom != null) && !dateFrom.equals("")) || ((dateTo != null) && !dateTo.equals(""))) {
            postBody = postBody
                    + "<StartDateTime>" + dateFrom + "</StartDateTime>"
                    + "<EndDateTime>" + dateTo + "</EndDateTime>";
        }
        //Optional ArrayOfVehicles in GetEventsInDateRangeForVehicles
        if (vehicles != null) {
            postBody = postBody + "<VehicleIDs>";
            vehicles.forEach(veh -> postBody = postBody + "<short>" + veh + "</short>");
            postBody = postBody + "</VehicleIDs>";
        }
        //Optional ArrayOfVehicles in GetEventsInDateRangeForVehicles
        if (drivers != null) {
            postBody = postBody + "<DriverIDs>";
            drivers.forEach(driver -> postBody = postBody + "<short>" + driver + "</short>");
            postBody = postBody + "</DriverIDs>";
            wsMethod = "GetEventsInDateRangeForDrivers";
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
