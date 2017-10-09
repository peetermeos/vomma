package sigma.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import sigma.trading.Connector;
import sigma.trading.Instrument;

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
    
	protected ArrayList<Instrument> instList;
    
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
	public void createInstruments() {
		instList = new ArrayList<Instrument>();
		
		logger.log("Creating instruments");
		instList.add(new Instrument("CL", "NYMEX", "FUT", "201711"));
		
		
		logger.log("Requesting market data");
		for(Instrument i: instList) {
			if(this.getClient().isConnected())
				this.getClient().reqMktData(this.getValidId(), i.getContract(), "", false, true, null);
		}
	}
    
    /**
     * Run the capture
     */
    public void run() {
    	// TODO Wait until keypress or cancel 
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
		
		tws.createInstruments();
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
