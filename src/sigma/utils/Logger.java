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
		this(LogLevel.INFO);
	}
	
	/**
	 * Constructor that sets the maximal loglevel
	 * 
	 * @param l loglevel threshold
	 */
	public Logger(LogLevel l) {
		this.myLogLevel = l;
	}

	/** 
	 * Logging with specified loglevel
	 * 
	 * @param l loglevel
	 * @param s string to be logged
	 */
	private void log(LogLevel l, String s) {
		Date dtg;
		
		dtg = new Date();		
		
		// Only output stuff that are not higher than prescribed loglevel
		if(l.compareTo(myLogLevel) <= 0)
			System.out.println(dtg + " " + l.toString() + " : " + s);
	}
	
	/**
	 * Prints out timestamped text to be logged
	 * 
	 * @param s String to be logged
	 */
	public void log(String s) {
		this.log(LogLevel.INFO, s);
	}
	
	/**
	 * Error logging functionality for error text
	 * Do not use this for exceptions.
	 * 
	 * @param s error text to be logged
	 */
	public void error(String s) {
		this.log(LogLevel.ERROR, s);
	}
	
	/**
	 * Warning logging functionality for warning text
	 * 
	 * @param s warning text to be logged
	 */
	public void warning(String s) {
		this.log(LogLevel.WARN, s);
	}
	
	/**
	 * Verbose logging functionality for text
	 * 
	 * @param s text to be logged
	 */
	public void verbose(String s) {
		this.log(LogLevel.VERBOSE, s);
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
