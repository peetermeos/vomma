package sigma.quant;

/**
 * Option side enum
 * @author Peeter Meos
 * @version 1.0
 *
 */
public enum OptSide {
	CALL, PUT;
	
	@Override
	public String toString() {
		switch(this) {
		case CALL:
			return("CALL");
		default:
			return("PUT");
		}
	}

}
