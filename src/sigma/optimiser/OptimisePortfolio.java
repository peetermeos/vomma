package sigma.optimiser;

import java.util.ArrayList;

import com.ib.client.Contract;

import sigma.trading.Connector;
import sigma.trading.Instrument;

/**
 * Option portfolio optimisation class
 * Main sequence
 * - connect to tws
 * - get current portfolio
 * - get market data for the underlyings
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
	protected ArrayList<Instrument> portfolio;
	
	private Boolean done;
		
	/**
	 * Standard constructor
	 */
	public OptimisePortfolio() {
		super();
		portfolio = new ArrayList<Instrument>();
	}
	
	/**
	 * Retrieves portfolio from TWS
	 */
	public void getPortfolio() {
		this.done = false;
		
		// Request positions
		logger.log("Getting positions");
		this.getClient().reqPositions();
		
		// Wait until all is received
		while(! this.done) {}
		
		// And we're done
		logger.log("Done retrieving portfolio");
		this.getClient().cancelPositions();
	}
	
	/**
	 * Position reporting processing
	 */
	@Override
	public void position(String account, Contract contract, double pos, double avgCost) {
		Instrument i;
		
		logger.log("Position. " + account + 
				" - Symbol: " + contract.symbol() + 
				", SecType: " + contract.getSecType() + 
				", Currency: " + contract.currency() +
				", Position: " + pos + 
				", Avg cost: " + avgCost);
		
		i = new Instrument(contract.symbol(), 
				contract.exchange(), 
				contract.secType().toString(), 
				contract.lastTradeDateOrContractMonth());
		
		i.setPos(Math.round(pos));
		portfolio.add(i);
	}

	/**
	 * End of position reporting processing
	 */
	@Override
	public void positionEnd() {
		this.done = true;	
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
		
		o.twsConnect();
		o.getPortfolio();
		o.optimise();
		o.twsDisconnect();
	}

}
