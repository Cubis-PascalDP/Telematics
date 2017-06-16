package telematics.rest;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.http.entity.StringEntity;
import utils.ResponseToOutputFormat;

import java.io.UnsupportedEncodingException;

import static telematics.GetTelematicsData.ta;


/**
 * This class retrieves Messaging information using the Telematics api. The usage details for the
 * api can be found <a href="http://api.fm-web.co.uk/webservices/CommunicationsWebSvc/MessageProcessesWS.asmx">here</a>
 * <p>
 * In relation to the command parameters different api methods are invoked:
 * <ul>
 * <li>--id and/or --continuous: <a href="http://api.fm-web.co.uk/webservices/CommunicationsWebSvc/MessageProcessesWS.asmx?op=GetMessagesSinceID">GetMessageSinceID</a></li>
 * </ul>
 *
 * @author  Pascal De Poorter
 * @version 1.0
 * @since   03-07-2017
 */
@Parameters(commandDescription = "Process recorded vehicle events.")
public class GetMessagesSinceID extends Messages {
    
	private String postBody;

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
        String wsMethod ="GetMessagesSinceID";
        ResponseToOutputFormat.setRecordID("Message");

        //Mandatory Integer in method GetEventsSinceID
        if (!(id == null || continuous)) {
            postBody = postBody
                    + "<ID>" + id + "</ID>";
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
