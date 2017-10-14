package sigma.trading;

/**
 * Portfolio position implementation
 * 
 * @author Peeter Meos
 * @version 1.0
 *
 */
public class Position {
	protected Instrument inst;
	
	protected int q;
	protected double avgCost;
	
	/**
	 * Default constructor
	 */
	public Position() {
		inst = new Instrument();
		this.q = 0;
		this.avgCost = 0.0;
	}
	
	/**
	 * Constructor that sets the instrument.
	 * 
	 * @param i instrument for the position
	 */
	public Position(Instrument i) {
		this();
		this.inst = i;
	}
}
