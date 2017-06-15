package telematics.rest;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.http.entity.StringEntity;
import utils.ResponseToOutputFormat;

import java.io.UnsupportedEncodingException;

/**
 * This class retrieves the Driver information using the Telematics api. The usage details for the
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

@Parameters(commandDescription = "Process event description or events in use on vehicles")
public class ProcessDriver extends Driver {
    @Parameter(names = "--driver", description = "Return driver information for the given dirver.")
    @SuppressWarnings("unused")
    private String driver;

    /**
     * Validates if a correct combination of parameters where passed to the command argument
     * @return boolean as result of the validation
     */
    public boolean parseArguments() {
        return true;
    }

    public void setBody() {
        String postBody = "";
        String wsMethod = "GetDriverList";
        ResponseToOutputFormat.setRecordID("Driver");
        if ((driver != null) && !driver.equals("")) {
            postBody = postBody + "<DriverID>" + driver + "</DriverID>";
            wsMethod = "GetDriver";
            ResponseToOutputFormat.setRecordID("GetDriverResult");
        }
        try {
            String replaceBody = body.replaceAll("%method%", wsMethod).replaceAll("%body%", postBody);
            postRequest.setEntity(new StringEntity(replaceBody));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
