package sigma.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
	
	private Instrument inst;
	
    private Connection connect = null;
    //private Statement statement = null;
    //private ResultSet resultSet = null;
    
	protected ArrayList<Instrument> instList;
    
    // Configuration
    String host = "sigma-db.cq2omyeocnub.us-east-1.rds.amazonaws.com";
    String db = "trading";
    String user = "trading";
    String pwd = "simukitkarp";
    
    public DataCapture() {
    	super("Data Capture");
    	
    	inst = new Instrument();
    }

    /**
     * Connects to tick database
     * 
     * @throws Exception
     */
    public void dbConnect() throws Exception {
    
        Class.forName("com.mysql.jdbc.Driver").newInstance();

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
		inst = new Instrument("CL", "NYMEX", "FUT", "201711");
		
		logger.log("Requesting market data");
		for(Instrument i: instList) {
			if(this.getClient().isConnected())
				this.getClient().reqMktData(this.getValidId(), i.getContract(), "", false, false, null);
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
			
		switch(field) {
		case 1: //bid
			this.writeEntry(field, price);
			inst.setBid(price);
			break;
		case 2: // ask
			this.writeEntry(field, price);
			inst.setAsk(price);
			break;
		case 4: //last
			this.writeEntry(field, price);
			inst.setSpot(price);
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
		logger.verbose("Tick Size. Ticker Id:" + tickerId + ", Field: " + field + ", Size: " + size);		
		
		switch(field) {
		case 0: //bid
			this.writeEntry(field, size);
			inst.setBidSize(size);
			break;
		case 3: // ask
			this.writeEntry(field, size);
			inst.setAskSize(size);
			break;
		case 5: // last
			this.writeEntry(field, size);
			inst.setSpotSize(size);
			break;
		default:
			break;
		}
	}
	
	public void writeEntry(int field, double value) {
		PreparedStatement preparedStmt = null;
		
	    String query = " insert into trading.tbl_ticks (symbol, dtg, spotDelta, bidDelta, askDelta, bidSize, askSize, spotSize)"
    	        + " values (?, now(4), ?, ?, ?, ?, ?, ?)";

        try {
        	preparedStmt = this.connect.prepareStatement(query);
			preparedStmt.setString (1, inst.getSymbol());
		} catch (SQLException e) {
			logger.error(e.toString());
		}
	    			   	
    	switch(field) {
    	case 1: // bid
    		try {
				preparedStmt.setDouble(2, value - inst.getBid());
				preparedStmt.setDouble(3, 0.0);
				preparedStmt.setDouble(4, 0.0);
			} catch (SQLException e) {
				logger.error(e.toString());
			}
    		break;
    	case 2: // ask
    		try {
    			preparedStmt.setDouble(2, 0.0);
				preparedStmt.setDouble(3, value - inst.getAsk());
				preparedStmt.setDouble(4, 0.0);
			} catch (SQLException e) {
				logger.error(e.toString());
			}
    		break;
    	case 4: // last
    		try {
				preparedStmt.setDouble(2, 0.0);
				preparedStmt.setDouble(3, 0.0);
				preparedStmt.setDouble(4, value - inst.getSpot());
			} catch (SQLException e) {
				logger.error(e.toString());
			}
    		break;
    	default:
    		try {
				preparedStmt.setDouble(2, 0.0);
				preparedStmt.setDouble(3, 0.0);
				preparedStmt.setDouble(4, 0.0);
			} catch (SQLException e) {
				logger.error(e.toString());
			}
    		break;
    	}

        try {
    		preparedStmt.setInt(5, inst.getBidSize());
    		preparedStmt.setInt(6, inst.getAskSize());
    		preparedStmt.setInt(7, inst.getSpotSize());
    	} catch (SQLException e) {
    		logger.error(e.toString());
    	}
        
    	try {
    		if (preparedStmt != null)
    			preparedStmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.toString());
		}
	}
	
    
    /**
     * Run the capture
     * @throws IOException 
     */
    public void run() throws IOException {
    	logger.log("Main trading loop");
    	while(System.in.available() == 0) {}
    	logger.log("End of main trading loop");
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
		try {
			tws.run();
		} catch (IOException e) {
			tws.logger.error(e.toString());
		}
		
		// Disconnect from tick database
		try {
			tws.dbDisconnect();
		} catch (Exception e) {
			tws.logger.error(e.toString());
		}
		
		tws.twsDisconnect();
	}

}
