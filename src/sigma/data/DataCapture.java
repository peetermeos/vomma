package sigma.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.ib.client.TickAttr;

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
	 * Tick price processing needs to be overriden
	 */
	@Override
	public void tickPrice(int tickerId, int field, double price, TickAttr attribs) {
		logger.log("Tick Price. Ticker Id:" + tickerId + ", Field: " + field + 
				", Price: " + price + ", CanAutoExecute: " +  attribs.canAutoExecute() +
                ", pastLimit: " + attribs.pastLimit() + ", pre-open: " + attribs.preOpen());
		
		// Write update to database
		this.writeEntry(field, price);
		
		switch(field) {
		case 1: //bid
			break;
		case 2: // ask
			break;
		case 4: //last
			break;
		default:
			break;
		}
		
	}
	
	/**
	 * Tick size processing needs to be overriden
	 */
	@Override
	public void tickSize(int tickerId, int field, int size) {
		logger.log("Tick Size. Ticker Id:" + tickerId + ", Field: " + field + ", Size: " + size);		
		
		// Size is cast from int to double
		this.writeEntry(field, size);
		
		switch(field) {
		case 0: //bid
			break;
		case 3: // ask
			break;
		case 5: // last
			break;
		default:
			break;
		}
	}
	
	public void writeEntry(int field, double value) {
		PreparedStatement preparedStmt = null;
		
	    String query = " insert into ticks (symbol, spotDelta, bidDelta, askDelta, bidSize, askSize, spotSize)"
    	        + " values (?, ?, ?, ?, ?, ?, ?)";

    	try {
			preparedStmt = this.connect.prepareStatement(query);
    	} catch (SQLException e) {
    		logger.error(e.toString());
		}
    	      
        /*
        preparedStmt.setString (1, "Barney");
        preparedStmt.setString (2, "Rubble");
        preparedStmt.setDate   (3, startDate);
        preparedStmt.setBoolean(4, false);
        preparedStmt.setInt    (5, this.getBidSize());
        preparedStmt.setInt    (6, this.getAskSize());
        preparedStmt.setInt    (7, this.getSpotSize());
        */
    	    	
    	try {
    		if (preparedStmt != null)
    			preparedStmt.execute();
		} catch (SQLException e) {
			logger.error(e.toString());
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
