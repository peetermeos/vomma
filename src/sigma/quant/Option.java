package sigma.quant;

import cern.jet.random.tdouble.Normal;
import cern.jet.random.tdouble.engine.MersenneTwister64;

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
	
	/**
	 * Constructor with assignment of all parameters.
	 * It is assumed that all rates and maturities are normalised to
	 * annual figures.
	 * 
	 * @param s Spot price
	 * @param k Strike price
	 * @param t Time to maturity
	 * @param sigma Volatility
	 * @param r Risk free interest rate
	 * @param d Dividend rate
	 */
	public Option(Double s, Double k, Double t, Double sigma, Double r, Double d) {
		this.s = s;
		this.k = k;
		this.t= t;
		this.sigma = sigma;
		this.r = r;
		this.d = d;
	}
	
	/**
	 * Returns Normal Distribution CDF (x)
	 * 
	 * @param x Value of x
	 * @return Gauss CDF(x)
	 */
	private Double cdf(Double x) {
		
		Normal g;
		
		g = new Normal(0.0, 1.0, new MersenneTwister64());
		
		return(g.cdf(x));
	}
	
	/**
	 * Helper function for greek calculations
	 * 
	 * @param x
	 * @return phi(x)
	 */
	private Double phi(Double x) {
		return((Math.exp(- x * x / 2)) / (Math.sqrt(2 * Math.PI)));
	}
	
	/**
	 * Standard vanilla BS d1 calculation
	 * 
	 * @return Double d1 value
	 */
	private Double d1() {
		return((Math.log(s / k) + t * (r + 0.5 * sigma * sigma)) / (sigma * Math.sqrt(t)));
	}
	
	/**
	 * Standard vanilla BS d2 calculation
	 * 
	 * @return Double d2 value
	 */
	private Double d2() {
		return(d1() - sigma * Math.sqrt(t));
	}
	
	/**
	 * Call price for the option
	 * 
	 * @return Double Call price
	 */
	public Double call() {
		return(s * cdf(d1()) - Math.exp(-r * t) * k * cdf(d2()));
	}
	
	/**
	 * Put price for the option
	 * 
	 * @return Double Put price
	 */
	public Double put() {
		return(call() - s + k * Math.exp(-r * t));
	}

}
