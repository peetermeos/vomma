package sigma.trading;

/**
 * Class for tracking trades
 * 
 * @author Peeter Meos
 * @version 1.0
 *
 */
public class Trade {
	protected Instrument inst;
	protected double price;
	protected int q;
	
	/**
	 * Default constructor
	 */
	public Trade() {
		inst = new Instrument();
	}
	
	/**
	 * Constructor setting the instrument, price and quantity for the trade.
	 * 
	 * @param i Instrument traded
	 * @param price Price traded at
	 * @param q Quantity traded
	 */
	public Trade(Instrument i, double price, int q) {
		this.inst = i;
		this.price = price;
		this.q = q;
	}
}
