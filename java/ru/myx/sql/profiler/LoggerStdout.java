/**
 * 
 */
package ru.myx.sql.profiler;

import java.text.SimpleDateFormat;
import java.util.Date;

final class LoggerStdout extends Logger {
	private final String			logName;
	
	private final SimpleDateFormat	dateformat	= new SimpleDateFormat( "HH:mm:ss" );
	
	private final Date				date		= new Date();
	
	LoggerStdout(final String logName) {
		this.logName = logName;
	}
	
	@Override
	public boolean event(final String owner, final String title, final String text) {
		this.date.setTime( System.currentTimeMillis() );
		System.out.println( this.dateformat.format( this.date )
				+ " ["
				+ this.logName
				+ "] "
				+ owner
				+ " ["
				+ Thread.currentThread().getId()
				+ "]-"
				+ title
				+ ": "
				+ text );
		return true;
	}
	
	@Override
	public final String formatPeriod(final long millis) {
		return millis + " ms";
	}
}
