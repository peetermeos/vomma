package sigma.trading;

import java.util.ArrayList;

/**
 * Simple news trading implementation
 * 
 * @author Peeter Meos
 * @version 0.1
 *
 */
public class NewsTrader extends Connector {

	protected ArrayList<Instrument> instList;
	
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
	 * Main trading loop
	 */
	public void run() {
		this.logger.log("Running trading loop");		
	}
	
	/**
	 * Main entry point
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		NewsTrader trader;
		
		trader = new NewsTrader();
		
		trader.twsConnect();
		trader.createInstruments();
		trader.run();
		trader.twsDisconnect();
	}

}
