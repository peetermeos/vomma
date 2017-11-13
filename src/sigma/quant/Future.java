package sigma.quant;

/**
 * Future implementation
 * 
 * @author Peeter Meos
 * @version 0.1
 */
public class Future extends Instrument {

	/**
	 * Generated serial
	 */
	private static final long serialVersionUID = 8783229229010289532L;
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
