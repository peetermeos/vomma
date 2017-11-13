package sigma.quant;

import java.io.Serializable;

/**
 * Simple generic instrument class
 * 
 * @author Peeter Meos
 * @version 0.1
 */
public class Instrument implements Serializable {


	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 6462342965485399405L;

	protected int id;
	
	protected String symbol;
	protected String exchange;
	protected String secType;
	
	protected Double price;
	protected Double bid;
	protected Double ask;
	
	protected Double pos;
	
	/**
	 * Simple constructor for Instrument class
	 */
	public Instrument() {
		id = -1;
		
		symbol = "";
		exchange = "";
		secType = "";
		
		price = -1.0;
		bid = -1.0;
		ask = -1.0;
		
		pos = 0.0;
	}
	
	/**
	 * Constructor that sets the symbol.
	 * 
	 * @param symbol String containing instrument symbol (eg. CL)
	 */
	public Instrument(String symbol) {
		this();
		
		this.symbol = symbol;
	}
	
	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Double getBid() {
		return bid;
	}

	public void setBid(Double bid) {
		this.bid = bid;
	}

	public Double getAsk() {
		return ask;
	}

	public void setAsk(Double ask) {
		this.ask = ask;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Double getPos() {
		return pos;
	}

	public void setPos(Double pos) {
		this.pos = pos;
	}
	
	
}
