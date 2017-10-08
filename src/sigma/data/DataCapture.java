package sigma.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import sigma.trading.Connector;

/**
 * This implements tick data retrieval and import into database
 * 
 * @author Peeter Meos
 * @version 0.1
 *
 */
public class DataCapture extends Connector {
    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    
    // Configuration
    String host = "localhost";
    String db = "trading";
    String user = "user";
    String pwd = "pass";

    /**
     * Connects to tick database
     * 
     * @throws Exception
     */
    public void dbConnect() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");

        connect = DriverManager.getConnection("jdbc:mysql://" + 
        		host + "/" + db + "?" +
                "user=" + user + "&password=" + pwd);
    }
    
    /**
     * Closes tick database.
     * 
     * @throws Exception
     */
    public void dbDisconnect() throws Exception {
    	if (connect != null) {
    		connect.close();
    	}
    }
    
    /**
     * Create instruments and request data
     */
    public void requestData() {
    	
    }
    
    /**
     * Run the capture
     */
    public void run() {
    	
    }
    
	/**
	 * Main entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		DataCapture tws;
		
		tws = new DataCapture();

		tws.twsConnect();
		
		// Connect to tick database
		try {
			tws.dbConnect();
		} catch (Exception e) {
			tws.logger.error(e.toString());
		}
		
		tws.requestData();
		tws.run();
		
		// Disconnect from tick database
		try {
			tws.dbDisconnect();
		} catch (Exception e) {
			tws.logger.error(e.toString());
		}
		
		tws.twsDisconnect();
	}

}
