package sigma.quant;

/**
 * Future implementation
 * 
 * @author Peeter Meos
 * @version 0.1
 */
public class Future extends Instrument {

	protected String expiry;
	
	public Future() {
		super();
		
		secType = "FUT";
		expiry = "";
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}
	
	
}
