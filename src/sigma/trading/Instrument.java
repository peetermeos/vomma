package sigma.trading;

import com.ib.client.Contract;

/**
 * Financial instrument description
 * 
 * @author Peeter Meos
 * @version 0.1
 */
public class Instrument {
	protected Contract c;
	protected String symbol;
	protected String exch;
	protected String expiry;
	protected String type;
	
	/**
	 * Standard constructor
	 */
	public Instrument() {
		c = new Contract();
	}
	
	/**
	 * Detailed constructor for instrument
	 * 
	 * @param symbol Instrument symbol
	 * @param exchange Exchange that trades the instrument
	 * @param type STK/OPT/FUT/FOP
	 * @param expiry Expiry date
	 */
	public Instrument(String symbol, String exchange, String type, String expiry) {
		this();
		
		this.symbol = symbol;
		this.exch = exchange;
		this.type = type;
		this.expiry = expiry;
		
		this.c.symbol(this.symbol);
		this.c.exchange(this.exch);
		this.c.secType(this.type);
		this.c.lastTradeDateOrContractMonth(this.expiry);
	}
	
	/**
	 * Returns TWS contract for the instrument
	 * @return TWS contract
	 */
	public Contract getContract() {
		return(c);
	}
}
