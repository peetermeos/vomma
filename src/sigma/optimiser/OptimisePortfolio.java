package sigma.optimiser;

import java.io.IOException;
import java.util.ArrayList;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EWrapperMsgGenerator;
import com.ib.client.TickAttr;

import sigma.quant.OptSide;
import sigma.quant.Option;
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
	protected ArrayList<Option> portfolio;
	
	private Boolean done;
		
	/**
	 * Standard constructor
	 */
	public OptimisePortfolio() {
		super();
		
		Double[] strikeArray = {45.0 ,50.0, 55.0};
		String[] expiryArray = {"201801", "201802"};
		
		portfolio = new ArrayList<>();
		
		// Add placeholders for options
		for(Double k: strikeArray)
			for(String exp: expiryArray) {
				portfolio.add(new Option("CL", "NYMEX", OptSide.CALL, k, exp));
				portfolio.add(new Option("CL", "NYMEX", OptSide.PUT, k, exp));
			}
	}
	
	/**
	 * Default contract details handling must be overriden 
	 */
	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
        logger.verbose(EWrapperMsgGenerator.contractDetails(reqId, contractDetails));
        // Find matching entry in option surface
        // and populate local symbol and last trading date
        if(reqId < 1000) {
        	portfolio.get(reqId).setExpiry(contractDetails.contract().lastTradeDateOrContractMonth());
        }
	}
	
	/**
	 * Request and receive option surface
	 */
	public void getSurface() {
		Boolean allDone = false;
		
		if(this.getClient().isConnected() == false) {
			this.logger.error("Not connected");
			return;
		}
		
		// Loop through portfolio entries and request contract info
		logger.log("Requesting option surface");
		for(int i=0; i < portfolio.size(); i++) {
			portfolio.get(i).setId(i);
			this.getClient().reqContractDetails(i, portfolio.get(i).getContract());
		}
		
		// Wait until we have the data for the entire surface
		/*
		while(!allDone) {
			allDone = true;
			for(Option o: portfolio) {
				if(o.getExpiry() == "") {
					allDone = false;
				}
			}
		}
		*/
		
		// Request market data for options
		logger.log("Request market data for options");
		for(int i = 0; i < portfolio.size(); i++) {
			this.getClient().reqMktData(1000 + i,  portfolio.get(i).getContract(), "", true, false, null);
		}
		
		// Done with options, now underlyings
		logger.log("Done with options, now underlyings");
		ArrayList<String> used = new ArrayList<>();
		Contract c;
		int ulId = 2000;
		
		for(int i=0; i < portfolio.size(); i++) {
			allDone = false;
			c = portfolio.get(i).getUnderlying();
			
			// Find if we have already requested data for that instrument
			for(int s=0; s<used.size(); s++) {
				if(used.get(s).compareTo(c.lastTradeDateOrContractMonth()) == 0) {
					allDone = true;
					portfolio.get(i).getUl().setId(2000 + s);
				}
			}
			
			// If we have not requested data for that one
			if (!allDone) {
				used.add(portfolio.get(i).getUnderlying().lastTradeDateOrContractMonth());
				portfolio.get(i).getUl().setId(ulId);
				this.getClient().reqMktData(ulId, c, "", true, false, null);
				ulId++;
			}
		}
	}
	
	/**
	 * Prints out the option surface
	 */
	public void printSurface() {
		for (Option o: portfolio) {
			logger.log(o.getSymbol() + " " + 
		               o.getExpiry() + " " + 
					   o.getSide().toString() + 
					   " price: " + o.getPrice() +
					   " ul: " + o.getUl().getPrice()
		               );
		}
	}
	
	/**
	 * Retrieves portfolio from TWS
	 */
	public void getCurrentPortfolio() {
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
		//portfolio.add(i);
	}
	
	/**
	 * Tick price processing override
	 */
	@Override
	public void tickPrice(int tickerId, int field, double price, TickAttr attribs) {
		logger.log("Tick price. Ticker Id:" + tickerId + ", Field: " + field + 
				", Price: " + price + ", CanAutoExecute: " +  attribs.canAutoExecute() +
                ", pastLimit: " + attribs.pastLimit() + ", pre-open: " + attribs.preOpen());
		
		// Process option prices
		if((tickerId >= 1000) && (tickerId < 2000)) {
			switch(field) {
			case 1: // bid
				portfolio.get(tickerId - 1000).setBid(price);
				break;
			case 2: // ask
				portfolio.get(tickerId - 1000).setAsk(price);
				break;
			case 4: // last
			case 9: // close	
				portfolio.get(tickerId - 1000).setPrice(price);
				break;
			default:
				break;
			}
		}
		
		// Process underlying prices
		if(tickerId >= 2000) {
			switch(field) {
			case 1: // bid
				for(Option o: portfolio)
					if(o.getUl().getId() == tickerId)
						o.getUl().setBid(price);
				break;
			case 2: // ask
				for(Option o: portfolio)
					if(o.getUl().getId() == tickerId)
						o.getUl().setAsk(price);
				break;
			case 4: // last
			case 9: // close
				for(Option o: portfolio)
					if(o.getUl().getId() == tickerId)
						o.getUl().setPrice(price);
				break;
			default:
				break;
			}			
		}
	}

	/**
	 * End of position reporting processing
	 */
	@Override
	public void positionEnd() {
		this.done = true;	
	}
	
	/**
	 * Calculates option chain volatilities
	 */
	public void calcVol() {
		for (Option o: portfolio) {
			o.calcVol();
		}
	}
	
	/** 
	 * Main optimisation routine
	 */
	public void optimise() {
		MinimiseBIP min;
		
		min = new MinimiseBIP();
		
		min.optimise();
		
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
		o.getSurface();
		try {
			while(System.in.available() == 0) {}
		} catch (IOException e) {
			o.logger.error(e.toString());
		}
		
		// Calculate volatilities
		o.calcVol();
		
		// For debugging print out the surface
		o.printSurface();
		
		// And we are done
		o.twsDisconnect();
	}

}
