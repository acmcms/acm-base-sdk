/**
 * 
 */
package ru.myx.sql.profiler;

import ru.myx.ae3.help.Format;
import ru.myx.ae3.report.Report;
import ru.myx.ae3.report.ReportReceiver;

final class LoggerAe3 extends Logger {
	private final String			logName;
	
	private final ReportReceiver	reciever;
	
	LoggerAe3(final String logName) {
		this.logName = logName;
		this.reciever = Report.createReceiver( this.logName );
	}
	
	@Override
	public boolean event(final String owner, final String title, final String text) {
		return this.reciever.event( owner, title, text );
	}
	
	@Override
	public final String formatPeriod(final long millis) {
		return Format.Compact.toPeriod( millis );
	}
}
