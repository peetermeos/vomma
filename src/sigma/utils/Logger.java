package sigma.utils;

import java.util.Date;

/**
 * Standard logging functionality
 * 
 * @author Peeter Meos
 * @version 0.1
 *
 */
public class Logger {
	private LogLevel myLogLevel;
	
	
	/**
	 * Default constructor sets the loglevel to INFO
	 */
	public Logger() {
		this.setMyLogLevel(LogLevel.INFO);
	}

	/**
	 * Prints out timestamped text to be logged
	 * 
	 * @param s String to be logged
	 */
	public void log(String s) {
		Date dtg;
		
		dtg = new Date();
		
		System.out.println(dtg + " " + s);
	}
	
	/**
	 * Error logging functionality for error text
	 * Do not use this for exceptions.
	 * 
	 * @param s error text to be logged
	 */
	public void error(String s) {
		this.log(s);
	}

	/**
	 * @return the myLogLevel
	 */
	public LogLevel getMyLogLevel() {
		return myLogLevel;
	}

	/**
	 * @param myLogLevel the myLogLevel to set
	 */
	public void setMyLogLevel(LogLevel myLogLevel) {
		this.myLogLevel = myLogLevel;
	}
}
