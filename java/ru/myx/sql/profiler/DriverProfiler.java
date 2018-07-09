package ru.myx.sql.profiler;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

/**
 * @author myx
 * 
 */
public final class DriverProfiler implements Driver {
	static final String	OWNER				= "SQLPROFILER";
	
	static final String	URL_PREFFIX			= "jdbc:profile:";
	
	static final int	URL_PREFFIX_LENGTH	= DriverProfiler.URL_PREFFIX.length();
	
	private int			index				= -1;
	
	
	/**
	 * 
	 */
	public DriverProfiler() {
	
		// empty
	}
	
	
	@Override
	public boolean acceptsURL(
			final String url) {
	
		return url.startsWith( DriverProfiler.URL_PREFFIX );
	}
	
	
	@Override
	public final Connection connect(
			final String url,
			final Properties info) throws SQLException {
	
		if (url.startsWith( DriverProfiler.URL_PREFFIX )) {
			final String leastUrl = url.substring( DriverProfiler.URL_PREFFIX_LENGTH );
			final int posRealUrl = leastUrl.indexOf( ':' );
			if (posRealUrl == -1) {
				throw new SQLException( "No log name available!" );
			}
			final String logParams = leastUrl.substring( 0, posRealUrl );
			final int posTreshold = logParams.indexOf( ',' );
			final String logName = posTreshold == -1
					? logParams
					: logParams.substring( 0, posTreshold );
			final long treshold = posTreshold == -1
					? 0
					: Long.parseLong( logParams.substring( posTreshold + 1 ) );
			final String realUrl = leastUrl.substring( posRealUrl + 1 );
			final Logger logger = new LoggerAe3( logName );
			final long connectStartTime = System.currentTimeMillis();
			final Driver driver = DriverManager.getDriver( realUrl );
			if (driver == null) {
				logger.event( DriverProfiler.OWNER, "CONNECT_ERROR", "no driver, url=" + realUrl );
				return null;
			}
			final Connection result = driver.connect( realUrl, info );
			final long connectDuration = System.currentTimeMillis() - connectStartTime;
			final String owner = DriverProfiler.OWNER + '[' + ++this.index + ']';
			if (connectDuration >= treshold) {
				logger.event( owner,
						"CONNECT",
						"time=" + logger.formatPeriod( connectDuration ) + ", treshold=" + logger.formatPeriod( treshold ) );
			}
			return result == null
					? null
					: new FilterConnection( owner, logger, treshold, result );
		}
		return null;
	}
	
	
	@Override
	public final int getMajorVersion() {
	
		return 0;
	}
	
	
	@Override
	public final int getMinorVersion() {
	
		return 9;
	}
	
	
	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
	
		throw new SQLFeatureNotSupportedException();
	}
	
	
	@Override
	public final DriverPropertyInfo[] getPropertyInfo(
			final String url,
			final Properties info) throws SQLException {
	
		if (url.startsWith( DriverProfiler.URL_PREFFIX )) {
			final String realUrl = url.substring( DriverProfiler.URL_PREFFIX_LENGTH );
			final Driver real = java.sql.DriverManager.getDriver( realUrl );
			return real.getPropertyInfo( realUrl, info );
		}
		return null;
	}
	
	
	@Override
	public final boolean jdbcCompliant() {
	
		return true;
	}
	
	
	@Override
	public String toString() {
	
		return DriverProfiler.URL_PREFFIX;
	}
}
