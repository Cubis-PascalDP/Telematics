package telematics.rest;

import com.beust.jcommander.Parameter;
import org.apache.http.entity.StringEntity;
import utils.ResponseToOutputFormat;

import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * This class retrieves the Vehicle information using the Telematics api. The usage details for the
 * api can be found <a href="http://api.fm-web.co.uk/webservices/AssetDataWebSvc/VehicleProcessesWS.asmx">here</a>
 * <p>
 * In relation to the command parameters different api methods are invoked:
 * <ul>
 * <li>No Parameters: <a href="http://api.fm-web.co.uk/webservices/AssetDataWebSvc/VehicleProcessesWS.asmx?op=GetVehiclesList">GetVehicleList</a></li>
 * <li>--vehicle: <a href="http://api.fm-web.co.uk/webservices/AssetDataWebSvc/VehicleProcessesWS.asmx?op=GetVehicle">GetVehicle</a></li>
 * </ul>
 *
 * @author  Pascal De Poorter
 * @version 1.0
 * @since   15-06-2017
 */
public class ProcessVehicle extends Vehicle {

    @Parameter(names = "--vehicle", description = "Return driver information for the given vehicle.")
    @SuppressWarnings("unused")
    private String vehicle;

    public boolean parseArguments() {
        return true;
    }

    public void setBody() {
        String postBody = "";
        String wsMethod = "GetVehiclesList";
        ResponseToOutputFormat.setRecordID("Vehicle");
        if ((vehicle != null) && !vehicle.equals("")) {
            postBody = postBody + "<VehicleID>" + vehicle + "</VehicleID>";
            wsMethod = "GetVehicle";
            ResponseToOutputFormat.setRecordID("GetVehicleResult");
        }
        try {
            String replaceBody = body.replaceAll("%method%", wsMethod).replaceAll("%body%", postBody);
            postRequest.setEntity(new StringEntity(replaceBody));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
