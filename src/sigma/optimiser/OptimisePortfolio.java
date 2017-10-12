package sigma.optimiser;

import sigma.trading.Connector;
import sigma.trading.Instrument;

/**
 * Option portfolio optimisation class
 * Main sequence
 * - connect to tws
 * - get portfolio
 * - get market data for the 
 * - get market data for the option chain
 * - calculate greeks
 * - populate optimisation model
 * - optimise
 * - output results
 * - optionally create orders to balance portfolio
 * - disconnect from the tws 
 *  
 * @author Peeter Meos
 * @version 0.1
 *
 */
public class OptimisePortfolio extends Connector {
	protected Instrument inst;
		
	/**
	 * Standard constructor
	 */
	public OptimisePortfolio() {
		super();
	}
	
	/** 
	 * Main optimisation routine
	 */
	public void optimise() {
		
	}

	/**
	 * Main entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		OptimisePortfolio o;
		
		o = new OptimisePortfolio();
		
		o.optimise();
	}

}
