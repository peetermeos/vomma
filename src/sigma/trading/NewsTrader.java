package sigma.trading;

/**
 * Simple news trading implementation
 * 
 * @author Peeter Meos
 * @version 0.1
 *
 */
public class NewsTrader extends Connector {

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
		trader.run();
		trader.twsDisconnect();
	}

}
