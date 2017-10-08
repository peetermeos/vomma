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
	OptSide side; // Side
	
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
		side = OptSide.CALL;
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
	 * @param side Option side
	 */
	public Option(Double s, Double k, Double t, Double sigma, Double r, Double d, OptSide side) {
		this.s = s;
		this.k = k;
		this.t= t;
		this.sigma = sigma;
		this.r = r;
		this.d = d;
		this.side = side;
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
	 * Helper function, returns total volatility over period.
	 * Ie sigma * sqrt(t)
	 * 
	 * @return total volatility
	 */
	private Double sigmatau() {
		return(sigma * Math.sqrt(t));
	}
	
	/**
	 * Standard vanilla BS d1 calculation
	 * 
	 * @return Double d1 value
	 */
	private Double d1() {
		return((Math.log(s / k) + t * (r + 0.5 * sigma * sigma)) / (sigmatau()));
	}
	
	/**
	 * Standard vanilla BS d2 calculation
	 * 
	 * @return Double d2 value
	 */
	private Double d2() {
		return(d1() - sigmatau());
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
	
	/**
	 * Calculates vanilla Black-Scholes delta
	 * 
	 * @return delta of the option
	 */
	public Double delta() {
		if(side == OptSide.CALL) {
			 return(Math.exp(-d * t) * cdf(d1()));
		} else {
			return(-Math.exp(-d * t) * cdf(-d1()));
		}
	}
	
	/**
	 * Calculates vanilla Black-Scholes gamma
	 * 
	 * @return gamma of the option
	 */
	public Double gamma() {
		return(Math.exp(-d * t) * phi(d1()) / (s * sigmatau()));
	}
	
	/**
	 * Calculates vanilla Black-Scholes vega
	 * 
	 * @return vega of the option
	 */
	public Double vega() {
		return(s * Math.exp(-d * t) * phi(d1()) * Math.sqrt(t));
	}
	
	/**
	 * Calculates vanilla Black-Scholes theta
	 * 
	 * @return theta of the option
	 */
	public Double theta() {
		Double x;
		Double y;
		Double z;
		
		x = -Math.exp(-d * t) * (s * phi(d1()) * sigma) / (2 * Math.sqrt(t));
		y = r * k * Math.exp(-r * t);
		z = d * s * Math.exp(-d * t);
		
		if(side == OptSide.CALL) {
			return(x - y * cdf(d2()) + z * cdf(d1()));
		} else {
			return(x + y * cdf(-d2()) - z * cdf(-d1()));
		}
	}

	/**
	 * Calculates charm of the option (d delta / d time)
	 * 
	 * @return charm of the option
	 */
	public Double charm() {
		Double x;
		Double y;
		
		x = d * Math.exp(-d * t);
		y = Math.exp(-d * t) * phi(d1()) * (2 * (r - d) * t - d2() * sigmatau()) / (2 * t * sigmatau());
		
		if(side == OptSide.CALL) {
			return(x * cdf(d1()) - y);
		} else {
			return(-x * cdf(-d1()) - y);
		}
	}
	
	/**
	 * Calculates thega of the option (d theta / d time)
	 * 
	 * @return thega of the option
	 */
	public Double thega() {
		Double x;
		Double y;
		Double z;
		Double u;
		
		x = -(s * sigma * phi(d1())) / (4 * t * Math.sqrt(t)) * (1 + (2 * (r - d) * t - d2() * sigmatau()) / sigmatau() * d1());
		y = r * r * k * Math.exp(-r * t) * cdf(d2());
		z = d * d * s * Math.exp(-d * t) * cdf(d1());
		u = s * Math.exp(-d * t) * phi(d1()) * (2 * (r - d) * (r - d) * t - d2() * sigmatau()) / (2 * t * sigmatau());
		
		if(side == OptSide.CALL) {
			return(x - y + z + u);
		} else {
			return(x + y - z + u);
		}
	}

	/**
	 * Calculates color (d gamma /d time)
	 * 
	 * @return color of the option
	 */
	public Double color() {
		return(-Math.exp(-d * t) * phi(d1()) / (2 * s * t * sigmatau()) * (2 * d * t + 1 + (2 * (r - d) * t - d2() * sigmatau()) / (sigmatau()) * d1()));
	}
	
	/**
	 * Calculates speed (d gamma / d spot)
	 * 
	 * @return speed of the option
	 */
	public Double speed() {
		return(-gamma() / s * (d1() / sigmatau() + 1));
	}

}
