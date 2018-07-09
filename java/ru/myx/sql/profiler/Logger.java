/*
 * Created on 27.09.2003
 * 
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ru.myx.sql.profiler;

/**
 * @author barachta
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
abstract class Logger {
	/**
	 * 
	 * @param owner
	 * @param title
	 * @param text
	 * @return 'true' to be compatible with assertions
	 */
	abstract boolean event(final String owner, final String title, final String text);
	
	abstract String formatPeriod(final long millis);
}
