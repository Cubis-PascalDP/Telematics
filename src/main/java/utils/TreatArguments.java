package utils;

import com.beust.jcommander.Parameter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;

import static telematics.GetTelematicsData.jc;

/**
 * This class will be responsible for the general arguments treatment and is dependant on <a href="http://jcommander.org/#_overview">JCommander</a>
 *
 * @author  Pascal De Poorter
 * @version 1.0
 * @since   14-03-2017
 */
public class TreatArguments {

    @Parameter(names = {"-o", "--outputFormat"}, required = true, description = "Provide the output format.",
            converter = OutputFormatConvertor.class)
    private OutputFormatEnum outputFormat = null;
    @Parameter(names = {"-tu", "--user"}, description = "User for Telematics api identification")
    private String pw;
    @Parameter(names = {"-tp", "--password"}, description = "Password for Telematics api identification")
    private String user;
    @Parameter(names = {"-h","--proxyHost"}, description = "Proxy host to be used")
    private String proxyHost;
    @Parameter(names = {"-p","--proxyPort"}, description = "Proxy port to be used")
    private int proxyPort;
    @Parameter(names = {"-U","--proxyUser"}, description = "Proxy user to be used")
    private String proxyUser;
    @Parameter(names = {"-P","--proxyPassword"}, description = "Proxy password to be used (input required)", password = true)
    private String proxyPassword;
    @Parameter(names = "--header", description = "Determines if a column header will be generated before parsing the records", arity = 1)
    private Boolean header = true;
    @SuppressWarnings("unused")
    @Parameter(names = "--topic", description = "Kafka topic to send the data to.")
    private String kafkaTopic;
    @Parameter(names = {"-b", "--bootstrap_server"}, description = "Kafka bootstrap server.")
    private String kafkaBootstrapServer;

    private Integer eventId;

    private FileBasedConfigurationBuilder<FileBasedConfiguration> builder = null;
    private Configuration prop = null;
    private Boolean continuous = false;
    private Integer minDelayBetweenAPICalls = null;
    private Integer contDelayNoData = null;
    private Integer previousID = null;


    /**
     * During construction parameters will be looked up in the telematics.properties file. They will be overwritten
     * by environment variables that are possibly set. These will on their turn be overwritten by arguments given to
     * the program.
     */
    public TreatArguments() {
        try {
            Parameters params = new Parameters();
            String PROPERTIES_FILE = "telematics.properties";
            builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                            .configure(params.fileBased().setFile(new File(PROPERTIES_FILE)));
            prop = builder.getConfiguration();
            // Telematics User
            user = System.getenv("TM_USER") == null ? prop.getString("user") : System.getenv("TM_USER");
            // Telematics Password
            pw = System.getenv("TM_PASSWORD") == null ? prop.getString("password") : System.getenv("TM_PASSWORD");
            // Proxy host
            proxyHost = System.getenv("TM_PROXYHOST") == null ? prop.getString("proxyHost") : System.getenv("TM_PROXYHOST");
            // Arguments given to VM get highest priority
            proxyHost = System.getProperty("http.proxyHost") != null ? System.getProperty("http.proxyHost") : proxyHost;
            // Proxy port
            String port = System.getenv("TM_PROXYPORT") == null ? prop.getString("proxyPort") : System.getenv("TM_PROXYPORT");
            // Arguments given to VM get highest priority
            port = System.getProperty("http.proxyPort") == null ? port : System.getProperty("http.proxyPort");
            proxyPort = port != null ? Integer.parseInt(port) : 0;
            // Proxy user
            proxyUser = System.getenv("TM_PROXYUSER") == null ? prop.getString("proxyUser") : System.getenv("TM_PROXYUSER");
            // Arguments given to VM get highest priority
            proxyUser = System.getProperty("http.proxyUser") == null ? proxyUser : System.getProperty("http.proxyUser");
            // Proxy password
            proxyPassword = System.getenv("TM_PROXYPASSWORD") == null ? prop.getString("proxyPassword") : System.getenv("TM_PROXYPASSWORD");
            // Arguments given to VM get highest priority
            proxyPassword = System.getProperty("http.proxyPassword") == null ? proxyPassword : System.getProperty("http.proxyPassword");
            // Provide header line to output
            String sHeader = prop.getString("header");
            header = !(sHeader != null && sHeader.equalsIgnoreCase("FALSE"));
            // Get minimum delay between api calls
            minDelayBetweenAPICalls = prop.getInt("minApiCallDelay");
            // Get delay during continuous processing when no data was retrieved with previous call.
            contDelayNoData = prop.getInt("contDelayNoData");
            // Get Kafka bootstrap server
            kafkaBootstrapServer = prop.getString("kafkaBootstrapServer");

        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialized eventID. Dependant on arguments method decision during jc.pars(). Therefor separate
     * call from the initialize() method
     */
    public void initializeEventID() {
        // Last processed Event ID from properties file.
        String event = prop.getString("eventId" + jc.getParsedCommand());
        eventId = (event != null) ? Integer.parseInt(event) : null;
    }

    /**
     * Stores the eventID in the telematics.properties file
     * @param eventID Event to set as latest event processed
     */
    public void setLastEventID(Integer eventID) {
        // Only set events if continuous flag is set
        if (continuous) {
            eventId = eventID;
            prop.setProperty("eventId" + jc.getParsedCommand(), eventID);
            try {
                builder.save();
            } catch (ConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Continuous processing delay
     */
    public void delay() {
        if (previousID != null && eventId != null) {
            try {
                if (previousID.equals(eventId)) Thread.sleep( contDelayNoData * 1000);
                else Thread.sleep(minDelayBetweenAPICalls * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        previousID = eventId;
    }

    /**
     * Getter method for last used event ID
     * @return eventId
     */
    public Integer getLastEventID(){
        return eventId;
    }

    /**
     * Getter for output format
     * @return outputFormat
     */
    public OutputFormatEnum getOutputFormat() {
        return outputFormat;
    }

    /**
     * Getter for telematics password
     * @return pw
     */
    public String getPw() {
        return pw;
    }

    /**
     * Getter for telematics user
     * @return user
     */
    public String getUser() {
        return user;
    }

    /**
     * Getter for proxy host
     * @return proxyHost
     */
    String getProxyHost() {
        return proxyHost;
    }

    /**
     * Getter for proxy port
     * @return proxyPort
     */
    int getProxyPort() {
        return proxyPort;
    }

    /**
     * Getter for proxy User
     * @return proxyUser
     */
    String getProxyUser() {
        return proxyUser;
    }

    /**
     * Getter for proxy password
     * @return proxyPassword
     */
    String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * Getter for header
     * @return header
     */
    public Boolean getHeader() {
        return header;
    }

    /**
     * Setter for header
     * @param header boolean flag header
     */
    public void setHeader(boolean header){
        this.header = header;
    }

    /**
     * Getter for continuous flag
     * @return continuous
     */
    public Boolean getContinuous() {
        return continuous;
    }

    /**
     * Setter for continuous flag
     * @param continuous boolean flag continuous
     */
    public void setContinuous(Boolean continuous) {
        this.continuous = continuous;
    }

    /**
     * Getter for Kafka Topic
     * @return kafakTopic
     */
    public String getKafkaTopic() {
        return kafkaTopic;
    }

    /**
     * Getter for Kafka bootstrap server
     * @return kafkaBootstrapServer
     */
    public String getKafkaBootstrapServer() {
        return kafkaBootstrapServer;
    }
}
