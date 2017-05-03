package telematics.rest;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.http.entity.StringEntity;
import utils.DateTimeValidator;
import utils.IntegerBiggerThan0Validator;
import utils.ResponseToOutputFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static telematics.GetTelematicsData.ta;

/**
 * This class retrieves the Positioning information using the Telematics api. The usage details for the
 * api can be found <a href="http://api.fm-web.co.uk/webservices/PositioningWebSvc/PositioningWS.asmx">here</a>
 * <p>
 * In relation to the command parameters different api methods are invoked:
 * <ul>
 * <li><i>Default</i>: <a href="http://api.fm-web.co.uk/webservices/PositioningWebSvc/PositioningWS.asmx?op=GetLatest1GPSPositionForVehicle">GetLatestPositionPerVehicle</a></li>
 * <li>--vehicles Different vehicle related apis are called depending on the given arguments combinations:
 * <ul>
 *     <li><i>Default</i>: <a href="http://api.fm-web.co.uk/webservices/PositioningWebSvc/PositioningWS.asmx?op=GetLatest1GPSPositionForVehicle">GetLatestPositionPerVehicle</a></li>
 *     <li>--id and/or --continuous: <a href="http://api.fm-web.co.uk/webservices/PositioningWebSvc/PositioningWS.asmx?op=GetGPSPositionsForSpecifiedVehiclesSinceID">GetGPSPositionsForSpecifiedVehiclesSinceID</a></li>
 * </ul>
 * </ul>
 * <li>--vehicle: Different vehicle related apis are called depending on the given arguments combinations:
 * <ul>
 *     <li>--startdate/--enddate: <a href="http://api.fm-web.co.uk/webservices/PositioningWebSvc/PositioningWS.asmx?op=GetGPSPositionsForVehicleInDateRange">GetGPSPositionsForVehicleInDateRange</a></li>
 *     <li>--id and/or --continuous: <a href="http://api.fm-web.co.uk/webservices/PositioningWebSvc/PositioningWS.asmx?op=GetGPSPositionsForSpecifiedVehiclesSinceID">GetGPSPositionsForSpecifiedVehiclesSinceID</a></li>
 * </ul>
 * </ul>
 *
 * @author  Pascal De Poorter
 * @version 1.0
 * @since   20-03-2017
 */
@Parameters(commandDescription = "Process recorded vehicle events.")
public class ProcessPositions extends Positioning {

	private String postBody;
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
        // Check for valid parameter combinations in case --ID or --continuous is passed as argument
        if (id != null || continuous)  {
            if (dateFrom != null) {
                message = message + "--ID or --continuous should not be used in combination with --startdate\n";
            }
            if (dateTo != null) {
                message = message + "--ID or --continuous should not be used in combination with --enddate\n";
            }
            if (id == null && ta.getLastEventID() == null && continuous) {
                message = message + "--continuous runs for the first time for command method. Should initialized using --ID.\n";
            }
            if (!message.equals("")) {
                System.err.println(message);
                return false;
            }
        }
        // Validate maxId
        if ((maxId != null) && (id == null)) {
            System.err.println("--MAXID should be used in combination with --ID.");
            return false;
        }
        // Validate vehicle/dr argument usage
        if ((vehicle != null) && (vehicles != null)) {
            System.err.println("--vehicle and --vehicles should not be used together.");
            return false;
        }

        // Validation of arguments OK
        return true;
    }

    public void setBody() {
        postBody = "";
        String wsMethod = "GetLatestPositionPerVehicle";
        ResponseToOutputFormat.setRecordID("GPSPosition");
        // process vehicle/vehicles only argument
        if ( vehicle != null && dateFrom == null && id == null ) {
            postBody = postBody + "<SpecificVehicleIDs><short>" + vehicle + "</short></SpecificVehicleIDs>";
        }
        if ( vehicles != null && dateFrom == null && id == null ) {
            postBody = postBody + "<SpecificVehicleIDs>";
            vehicles.forEach(v ->  postBody = postBody + "<short>" + v + "</short>");
            postBody = postBody + "</SpecificVehicleIDs>";
        }
        if ( id != null && vehicle == null & vehicles == null) {
            wsMethod = "GetPositionsV2SinceID";
            ResponseToOutputFormat.setRecordID("GPSPositionV2");
            postBody = postBody + "<fromID>" + id + "</fromID>";
        }
        ta.setContinuous(continuous);
        ta.setMaxID(maxId);

        if (continuous) {
            if (id != null) {
                ta.initLastEventID(id);
                id = null;
            }
            postBody = postBody
                    + "<fromID>" + ta.getLastEventID() + "</fromID>";
            wsMethod = "GetPositionsV2SinceID";
            ResponseToOutputFormat.setRecordID("GPSPositionV2");
        }

        try {
            String replaceBody = body.replaceAll("%method%", wsMethod).replaceAll("%body%", postBody);
            postRequest.setEntity(new StringEntity(replaceBody));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
