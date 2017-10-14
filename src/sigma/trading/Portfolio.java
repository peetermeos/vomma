package sigma.trading;

import java.util.ArrayList;

/**
 * Portfolio tracking
 * 
 * @author Peeter Meos
 * @version 1.0
 *
 */
public class Portfolio {

	protected ArrayList<Position> positions;
	protected ArrayList<Trade> trades;
	protected double pnl;
	
	
	/**
	 * Default constructor for portfolio 
	 */
	public Portfolio() {
		positions = new ArrayList<>();
		trades = new ArrayList<>();
		
		pnl = 0.0;
	}
}
