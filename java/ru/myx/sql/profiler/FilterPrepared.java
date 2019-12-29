package ru.myx.sql.profiler;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/** Title: ae1 Base definitions Description: Copyright: Copyright (c) 2001 Company:
 *
 * @author Alexander I. Kharitchev
 * @version 1.0 */
class FilterPrepared extends FilterStatement implements PreparedStatement {
	
	protected final String sql;

	FilterPrepared(final String owner, final FilterConnection connection, final PreparedStatement parent, final String sql) {
		
		super(owner, connection, parent);
		this.sql = sql;
	}

	@Override
	public void addBatch() throws SQLException {
		
		this.batchSize++;
		this.p().addBatch();
	}

	@Override
	public void clearParameters() throws SQLException {
		
		this.p().clearParameters();
	}

	@Override
	public boolean execute() throws SQLException {
		
		final long timeStart = System.currentTimeMillis();
		try {
			return this.p().execute();
		} catch (final SQLException e) {
			this.connection.logger.event(this.owner, "EXECUTE", "error=" + e.getMessage() + ", query=" + this.sql);
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.connection.treshold <= timeTook) {
				this.connection.logger.event(this.owner, "EXECUTE", "time=" + this.connection.logger.formatPeriod(timeTook) + ", query=" + this.sql);
			}
		}
	}

	@Override
	public int[] executeBatch() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		final long timeStart = System.currentTimeMillis();
		try {
			return this.parent.executeBatch();
		} catch (final SQLException e) {
			this.connection.logger.event(this.owner, "EXECUTE_BATCH", "error=" + e.getMessage() + ", size=" + this.batchSize);
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.connection.treshold <= timeTook) {
				this.connection.logger
						.event(this.owner, "EXECUTE_BATCH", "time=" + this.connection.logger.formatPeriod(timeTook) + ", size=" + this.batchSize + ", query=" + this.sql);
			}
			this.batchSize = 0;
		}
	}

	@Override
	public ResultSet executeQuery() throws SQLException {
		
		final long timeStart = System.currentTimeMillis();
		try {
			return new FilterResult(this.p().executeQuery());
		} catch (final SQLException e) {
			this.connection.logger.event(this.owner, "EXECUTE_QUERY", "error=" + e.getMessage() + ", query=" + this.sql);
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.connection.treshold <= timeTook) {
				this.connection.logger.event(this.owner, "EXECUTE_QUERY", "time=" + this.connection.logger.formatPeriod(timeTook) + ", query=" + this.sql);
			}
		}
	}

	@Override
	public int executeUpdate() throws SQLException {
		
		final long timeStart = System.currentTimeMillis();
		try {
			return this.p().executeUpdate();
		} catch (final SQLException e) {
			this.connection.logger.event(this.owner, "EXECUTE_UPDATE", "error=" + e.getMessage() + ", query=" + this.sql);
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.connection.treshold <= timeTook) {
				this.connection.logger.event(this.owner, "EXECUTE_UPDATE", "time=" + this.connection.logger.formatPeriod(timeTook) + ", query=" + this.sql);
			}
		}
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		
		return this.p().getMetaData();
	}

	@Override
	public java.sql.ParameterMetaData getParameterMetaData() throws SQLException {
		
		return this.p().getParameterMetaData();
	}

	@Override
	public boolean isClosed() {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPoolable() {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) {
		
		throw new UnsupportedOperationException();
	}

	protected final PreparedStatement p() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		return (PreparedStatement) this.parent;
	}

	@Override
	public void setArray(final int i, final Array array) throws SQLException {
		
		this.p().setArray(i, array);
	}

	@Override
	public void setAsciiStream(final int parameterIndex, final InputStream x) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAsciiStream(final int parameterIndex, final InputStream input, final int length) throws SQLException {
		
		this.p().setAsciiStream(parameterIndex, input, length);
	}

	@Override
	public void setAsciiStream(final int parameterIndex, final InputStream x, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBigDecimal(final int parameterIndex, final BigDecimal decimal) throws SQLException {
		
		this.p().setBigDecimal(parameterIndex, decimal);
	}

	@Override
	public void setBinaryStream(final int parameterIndex, final InputStream x) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBinaryStream(final int parameterIndex, final InputStream input, final int length) throws SQLException {
		
		this.p().setBinaryStream(parameterIndex, input, length);
	}

	@Override
	public void setBinaryStream(final int parameterIndex, final InputStream x, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBlob(final int i, final Blob blob) throws SQLException {
		
		this.p().setBlob(i, blob);
	}

	@Override
	public void setBlob(final int parameterIndex, final InputStream inputStream) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBlob(final int parameterIndex, final InputStream inputStream, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
		
		this.p().setBoolean(parameterIndex, x);
	}

	@Override
	public void setByte(final int parameterIndex, final byte x) throws SQLException {
		
		this.p().setByte(parameterIndex, x);
	}

	@Override
	public void setBytes(final int parameterIndex, final byte[] bytes) throws SQLException {
		
		this.p().setBytes(parameterIndex, bytes);
	}

	@Override
	public void setCharacterStream(final int parameterIndex, final Reader reader) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) throws SQLException {
		
		this.p().setCharacterStream(parameterIndex, reader, length);
	}

	@Override
	public void setCharacterStream(final int parameterIndex, final Reader reader, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setClob(final int i, final Clob clob) throws SQLException {
		
		this.p().setClob(i, clob);
	}

	@Override
	public void setClob(final int parameterIndex, final Reader reader) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setClob(final int parameterIndex, final Reader reader, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDate(final int parameterIndex, final Date date) throws SQLException {
		
		this.p().setDate(parameterIndex, date);
	}

	@Override
	public void setDate(final int parameterIndex, final Date date, final Calendar cal) throws SQLException {
		
		this.p().setDate(parameterIndex, date, cal);
	}

	@Override
	public void setDouble(final int parameterIndex, final double x) throws SQLException {
		
		this.p().setDouble(parameterIndex, x);
	}

	@Override
	public void setFloat(final int parameterIndex, final float x) throws SQLException {
		
		this.p().setFloat(parameterIndex, x);
	}

	@Override
	public void setInt(final int parameterIndex, final int x) throws SQLException {
		
		this.p().setInt(parameterIndex, x);
	}

	@Override
	public void setLong(final int parameterIndex, final long x) throws SQLException {
		
		this.p().setLong(parameterIndex, x);
	}

	@Override
	public void setNCharacterStream(final int parameterIndex, final Reader value) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNCharacterStream(final int parameterIndex, final Reader value, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNClob(final int parameterIndex, final NClob value) throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNClob(final int parameterIndex, final Reader reader) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNClob(final int parameterIndex, final Reader reader, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNString(final int parameterIndex, final String value) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
		
		this.p().setNull(parameterIndex, sqlType);
	}

	@Override
	public void setNull(final int paramIndex, final int sqlType, final String typeName) throws SQLException {
		
		this.p().setNull(paramIndex, sqlType, typeName);
	}

	@Override
	public void setObject(final int parameterIndex, final Object object) throws SQLException {
		
		this.p().setObject(parameterIndex, object);
	}

	@Override
	public void setObject(final int parameterIndex, final Object object, final int targetSqlType) throws SQLException {
		
		this.p().setObject(parameterIndex, object, targetSqlType);
	}

	@Override
	public void setObject(final int parameterIndex, final Object object, final int targetSqlType, final int scale) throws SQLException {
		
		this.p().setObject(parameterIndex, object, targetSqlType, scale);
	}

	@Override
	public void setPoolable(final boolean poolable) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRef(final int i, final Ref ref) throws SQLException {
		
		this.p().setRef(i, ref);
	}

	@Override
	public void setRowId(final int parameterIndex, final RowId x) throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setShort(final int parameterIndex, final short x) throws SQLException {
		
		this.p().setShort(parameterIndex, x);
	}

	@Override
	public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setString(final int parameterIndex, final String string) throws SQLException {
		
		this.p().setString(parameterIndex, string);
	}

	@Override
	public void setTime(final int parameterIndex, final Time time) throws SQLException {
		
		this.p().setTime(parameterIndex, time);
	}

	@Override
	public void setTime(final int parameterIndex, final Time time, final Calendar cal) throws SQLException {
		
		this.p().setTime(parameterIndex, time, cal);
	}

	@Override
	public void setTimestamp(final int parameterIndex, final Timestamp timestamp) throws SQLException {
		
		this.p().setTimestamp(parameterIndex, timestamp);
	}

	@Override
	public void setTimestamp(final int parameterIndex, final Timestamp timestamp, final Calendar cal) throws SQLException {
		
		this.p().setTimestamp(parameterIndex, timestamp, cal);
	}

	@Deprecated
	@Override
	public void setUnicodeStream(final int parameterIndex, final InputStream input, final int length) {
		
		throw new NoSuchMethodError("Deprecated!");
	}

	@Override
	public void setURL(final int parameterIndex, final java.net.URL url) throws SQLException {
		
		this.p().setURL(parameterIndex, url);
	}

	@Override
	public <T> T unwrap(final Class<T> iface) {
		
		throw new UnsupportedOperationException();
	}
}
