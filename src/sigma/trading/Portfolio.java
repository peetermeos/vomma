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
	
	/**
	 * Adds instrument to the portfolio
	 * @param i
	 */
	public void addInstrument(Instrument i) {
		if (positions != null) {
			positions.add(new Position(i));
		}
	}
	
	/**
	 * Adds trade to the portfolio
	 * @param i Instrument traded
	 * @param price Trade price
	 * @param q Quantity traded
	 */
	public void addTrade(Instrument i, double price, int q) {
		this.trades.add(new Trade(i, price, q));
		
		// TODO position update for the instrument needs to be done here
	}
}
