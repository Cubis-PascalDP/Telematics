package telematics.db;

import com.beust.jcommander.Parameters;
import utils.ResponseToOutputFormat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static telematics.GetTelematicsData.ta;

/**
 * This class will read the history records from SQL server database for Recorded events
 *
 * @author  Pascal De Poorter
 * @version 1.0
 * @since   14/04/2017
 */
@Parameters(commandDescription = "Process recorded vehicle events.")
public class TableRecordedEvents {

    private ResultSet rs = null;
    private ResultSetMetaData meta = null;

    public boolean parseArguments() {
        return true;
    }

@SuppressWarnings("unused")
    public void initialize() {

        Connection conn;
        Statement stmt;

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection("jdbc:sqlserver://10.104.1.44:1433;user=hadoop;password=hadoop");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM RecordedEvents");
            meta = rs.getMetaData();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        ta.setContinuous(true);
        ta.dbProcessingOn();
    }

    public void setBody() {

    }

    public void getResponse() {
        List<String> response = new ArrayList<>();

        int k = 0;
        try {
            while (rs.next() & (k < 1000) ) {
                String result = "";
                String seperator = "";
                for(int i = 1; i < meta.getColumnCount(); i++) {
                    result = result + seperator + rs.getString(i);
                    seperator = ";";
                }
                response.add(result);
                k++;
            }
            if (k < 1000) ta.setContinuous(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ResponseToOutputFormat.setResponse(response);
    }
}
