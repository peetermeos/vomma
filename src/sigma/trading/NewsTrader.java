package sigma.trading;

/**
 * Simple news trading implementation
 * 
 * @author Peeter Meos
 * @version 0.1
 *
 */
public class NewsTrader extends Connector {

	public void run() {
		this.logger.log("Running trading loop");
		
	}
	
	public static void main(String[] args) {
		NewsTrader trader;
		
		trader = new NewsTrader();
		
		trader.run();
	}

}
