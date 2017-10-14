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
}
