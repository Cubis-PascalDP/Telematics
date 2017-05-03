package telematics;

import com.beust.jcommander.JCommander;
import telematics.db.TableRecordedEvents;
import telematics.rest.ProcessEventNotifications;
import telematics.rest.ProcessEvents;
import telematics.rest.ProcessPositions;
import telematics.rest.ProcessRecordedEvents;
import utils.HTTPClient;
import utils.KafkaClientProducer;
import utils.ResponseToOutputFormat;
import utils.TreatArguments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static utils.OutputFormatEnum.KAFKA;

/**
 * The GetTelematicsData program is a java wrapper program around the <a href="http://api.fm-web.co.uk/webservices/">Telematics API</a>
 * It will interrogate the api in relation to the arguments given to the program.
 *
 * @author  Pascal De Poorter
 * @version 0.1
 * @since   01-01-2017
 */

public class GetTelematicsData {

    public static TreatArguments ta = null;
    public static JCommander jc = null;
    public static KafkaClientProducer kp = null;

    private static Class<?> commandClass = null;
    private static Object commandObject = null;

    /**
     * Get the telematics data using the arguments passed to the program
     *
     * @param args  List of arguments
     */
    public static void main(String[] args) {

        // Register classes as command arguments for JCommander processing
        ta = new TreatArguments();
        jc = new JCommander(ta);

        // Initialize the Telematics classes to be used as commands
        Map<String, Object> classes = new HashMap<>();

        classes.put("ProcessEvents", new ProcessEvents());
        classes.put("ProcessRecordedEvents", new ProcessRecordedEvents());
        classes.put("ProcessPositions", new ProcessPositions());
        classes.put("ProcessEventNotifications", new ProcessEventNotifications());
        classes.put("TableRecordedEvents", new TableRecordedEvents());

        classes.forEach((k, v) -> jc.addCommand(k, v));

        try {
            jc.parse(args);
            // Arguments have been parsed. command method is now known and EventID can be initialized
            ta.initializeEventID();
        } catch (Exception e ) {
            System.err.println(e.getMessage());
            jc.usage();
            System.exit(1);
        }
        if (jc != null) {
            if (jc.getParsedCommand() == null) {
              System.err.println("Command expected!");
              jc.usage();
              System.exit(1);
            } else {
                if (classes.containsKey(jc.getParsedCommand())) {
                    try {
                        commandClass = classes.get(jc.getParsedCommand()).getClass();
                        commandObject = classes.get(jc.getParsedCommand());
                        Method parseArguments = commandClass.getMethod("parseArguments");
                        if ((Boolean) parseArguments.invoke(commandObject)) {
                            run();
                        } else {
                            jc.usage();
                            System.exit(1);
                        }
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // Close http Client
        HTTPClient.closeClient();
    }

    /**
     * Perform Telematics api processing for the command class passed as argument to the program
     */
    private static void run() {

        if (commandClass != null) {
            if (ta.getOutputFormat() == KAFKA) {
                kp = new KafkaClientProducer();
            }
            try {
                // Prepare the command class for usage
                Method initialize = commandClass.getMethod("initialize");
                Method setBody = commandClass.getMethod("setBody");
                Method getResponse = commandClass.getMethod("getResponse");

                // Create http Client
                HTTPClient.createClient();

                // Process the response from the api linked to the command class
                initialize.invoke(commandObject);
                do {
                    ta.delay();
                    setBody.invoke(commandObject);
                    getResponse.invoke(commandObject);
                    ResponseToOutputFormat.parse();
                    ta.setHeader(false);
                } while (ta.getContinuous());
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                if (ta.getOutputFormat() == KAFKA) {
                    kp.kafkaClose();
                }
            }
        }
    }
}
