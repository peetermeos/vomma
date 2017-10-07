package sigma.quant;

/**
 * Option implementation with second order Greeks
 * 
 * @author Peeter Meos
 * @version 0.1
 *
 */
public class Option {
	// Option valuation parameters
	Double s;     // Spot
	Double k;     // Strike
	Double t;     // Time to maturity
	Double sigma; // Volatility
	Double r;     // Risk free interest
	Double d;     // Dividends
	
	/**
	 * Default constructor, zero to all parameters
	 */
	public Option() {
		s = 0.0;
		k = 0.0;
		t = 0.0;
		sigma = 0.0;
		r = 0.0;
		d = 0.0;
	}
}
