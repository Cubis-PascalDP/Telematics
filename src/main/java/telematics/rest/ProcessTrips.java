package telematics.rest;

import com.beust.jcommander.Parameter;
import org.apache.http.entity.StringEntity;
import utils.DateTimeValidator;
import utils.IntegerBiggerThan0Validator;
import utils.ResponseToOutputFormat;

import java.io.UnsupportedEncodingException;

/**
 * This class retrieves the Trips information using the Telematics api. The usage details for the
 * api can be found <a href="http://api.fm-web.co.uk/webservices/UnitConfigurationWebSvc/EventDescriptionProcessWS.asmx">here</a>
 * <p>
 * In relation to the command parameters different api methods are invoked:
 * <ul>
 * <li>No Parameters: <a href="http://api.fm-web.co.uk/webservices/UnitConfigurationWebSvc/DriverProcessWS.asmx?op=GetDriverList">GetDriverList</a></li>
 * <li>--driver: <a href="http://api.fm-web.co.uk/webservices/UnitConfigurationWebSvc/DriverProcessWS.asmx?op=GetDriver">GetDriver</a></li>
 * </ul>
 *
 * @author  Pascal De Poorter
 * @version 1.0
 * @since   15-06-2017
 */

public class ProcessTrips extends Trips {
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


    public boolean parseArguments() {
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

        return true;
    }

    public void setBody() {
        String postBody = "";
        String wsMethod = "GetTripsWithTotalsForDateRange";
        ResponseToOutputFormat.setRecordID("TripWithTotals");
        if ((vehicle != null) && !vehicle.equals("")) {
            postBody = postBody + "<vehicleId>" + vehicle + "</vehicleId>";
            wsMethod = "GetTripsWithTotalsForVehicleInDateRange";
        }
        if (((dateFrom != null) && !dateFrom.equals("")) || ((dateTo != null) && !dateTo.equals(""))) {
            postBody = postBody
                    + "<StartDateTime>" + dateFrom + "</StartDateTime>"
                    + "<EndDateTime>" + dateTo + "</EndDateTime>";
        }
        try {
            ResponseToOutputFormat.setMethod(wsMethod);
            postRequest.setEntity(new StringEntity(body.replaceAll("%method%", wsMethod).replaceAll("%body%", postBody)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
