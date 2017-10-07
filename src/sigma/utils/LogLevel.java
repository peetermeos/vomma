package sigma.utils;

/**
 * Simple logging level enum
 * @author Peeter Meos
 * @version 0.1
 *
 */
public enum LogLevel {
	ERROR, WARN, INFO, VERBOSE;
	
	/**
	 * Standard toString method for LogLevel class
	 * @return String representation of logging level.
	 */
	@Override
	public String toString() {
		switch(this) {
		case ERROR:
			return("ERROR");
		case WARN:
			return("WARN");
		case INFO: 
			return("INFO");
		default:
			return("VERBOSE");
		}
	}
}
