package sigma.trading;

import com.ib.client.Contract;

/**
 * Financial instrument description
 * 
 * @author Peeter Meos
 * @version 0.1
 */
public class Instrument {
	protected int id;
	
	// Price fields
	protected Double spot;
	protected Double bid;
	protected Double ask;
	protected int spotSize;
	protected int bidSize;
	protected int askSize;
	
	// Quantity and price fields
	protected long pos;
	
	// Symbol fields
	protected String symbol;
	protected String exch;
	protected String expiry;
	protected String type;
	
	/**
	 * Standard constructor
	 */
	public Instrument() {
		
		this.spot = 0.0;
		this.bid = 0.0;
		this.ask = 0.0;
		
		this.bidSize = 0;
		this.askSize = 0;
		this.spotSize = 0;
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
	}
	
	/**
	 * Returns TWS contract for the instrument
	 * 
	 * @return TWS contract
	 */
	public Contract getContract() {
		Contract c = new Contract();
		
		c.symbol(this.symbol);
		c.exchange(this.exch);
		c.secType(this.type);
		c.lastTradeDateOrContractMonth(this.expiry);
		
		return(c);
	}
	
	/**
	 * Sets instrument id
	 * 
	 * @param id ID of the instrument
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Gets instrument id
	 * 
	 * @return ID of the instrument
	 */
	public int getId() {
		return(this.id);
	}
	
	/**
	 * Sets current spot price
	 * 
	 * @param spot Spot price of the instrument
	 */
	public void setSpot(Double spot) {
		this.spot = spot;
	}
	
	/**
	 * Gets current spot price
	 * 
	 * @return Current spot price of the instrument
	 */
	public Double getSpot() {
		return(this.spot);
	}
	
	/**
	 * Sets current bid price
	 * 
	 * @param bid Bid price of the instrument
	 */
	public void setBid(Double bid) {
		this.bid = bid;
	}
	
	/**
	 * Gets current bid price
	 * 
	 * @return Current bid price of the instrument
	 */
	public Double getBid() {
		return(this.bid);
	}
	
	/**
	 * Sets current ask price
	 * 
	 * @param ask Ask price of the instrument
	 */
	public void setAsk(Double ask) {
		this.ask = ask;
	}
	
	/**
	 * Gets current ask price
	 * 
	 * @return Current ask price of the instrument
	 */
	public Double getAsk() {
		return(this.ask);
	}
	
	/**
	 * Sets current ask size
	 * 
	 * @param ask Ask size of the instrument
	 */
	public void setAskSize(int ask) {
		this.askSize = ask;
	}
	
	/**
	 * Gets current ask size
	 * 
	 * @return Current ask size of the instrument
	 */
	public int getAskSize() {
		return(this.askSize);
	}
	
	/**
	 * Sets current bid size
	 * 
	 * @param bid Bid size of the instrument
	 */
	public void setBidSize(int bid) {
		this.bidSize = bid;
	}
	
	/**
	 * Gets current bid size
	 * 
	 * @return Current bid size of the instrument
	 */
	public int getBidSize() {
		return(this.bidSize);
	}
	
	/**
	 * Sets current last transaction size
	 * 
	 * @param spot Spot size of the instrument
	 */
	public void setSpotSize(int spot) {
		this.spotSize = spot;
	}
	
	/**
	 * Gets current spot transaction size
	 * 
	 * @return Current spot size of the instrument
	 */
	public int getSpotSize() {
		return(this.spotSize);
	}
	
	/**
	 * Gets symbol name of the instrument
	 * 
	 * @return Symbol name of the instrument
	 */
	public String getSymbol() {
		return(this.symbol);
	}
	
	/**
	 * Sets symbol name of the instrument
	 * 
	 * @param s symbol name of the instrument
	 */
	public void setSymbol(String s) {
		this.symbol = s;
	}
	
	/**
	 * Sets position for the instrument
	 * 
	 * @param l position to be set
	 */
	public void setPos(long l) {
		this.pos = l;
	}
	
	/**
	 * Gets position of the instrument
	 * 
	 * @return Position of the instrument
	 */
	public long getPos() {
		return(this.pos);
	}
}
