package ru.myx.sql.profiler;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import ru.myx.util.WeakFinalizer;

/** Title: ae1 Base definitions Description: Copyright: Copyright (c) 2001 Company:
 *
 * @author Alexander I. Kharitchev
 * @version 1.0 */
final class FilterConnection implements Connection {

	private static void finalizeStatic(final FilterConnection x) {

		if (x.parent != null) {
			try {
				x.close();
			} catch (final Throwable t) {
				t.printStackTrace();
			} finally {
				x.parent = null;
				x.logger.event(x.owner, "CLOSE-FIN", "lifetime=" + x.logger.formatPeriod(System.currentTimeMillis() - x.birthDate));
			}
		}
	}
	
	private final long birthDate;
	
	private final String owner;
	
	protected final long treshold;
	
	protected final Logger logger;
	
	protected Connection parent;
	
	protected int index = -1;
	
	FilterConnection(final String owner, final Logger reciever, final long treshold, final Connection parent) {

		if (parent == null) {
			throw new RuntimeException("Cannot wrap null connection!");
		}
		this.birthDate = System.currentTimeMillis();
		this.owner = owner;
		this.logger = reciever;
		this.treshold = treshold;
		this.parent = parent;

		WeakFinalizer.register(this, FilterConnection::finalizeStatic);
	}
	
	@Override
	public void abort(final Executor executor) throws SQLException {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public void clearWarnings() throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		this.parent.clearWarnings();
	}
	
	@Override
	public void close() throws SQLException {

		if (this.parent != null) {
			try {
				this.parent.close();
			} finally {
				this.parent = null;
				final long period = System.currentTimeMillis() - this.birthDate;
				if (period >= this.treshold) {
					this.logger.event(this.owner, "CLOSE", "lifetime=" + this.logger.formatPeriod(period));
				}
			}
		}
	}
	
	@Override
	public void commit() throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		final long timeStart = System.currentTimeMillis();
		try {
			this.parent.commit();
		} catch (final SQLException e) {
			this.logger.event(this.owner, "COMMIT", "error=" + e.getMessage());
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.treshold <= timeTook) {
				this.logger.event(this.owner, "COMMIT", "time=" + this.logger.formatPeriod(timeTook));
			}
		}
	}
	
	protected Statement convert(final Statement st) {

		return new FilterStatement(this.owner + "/STATEMENT[" + ++this.index + "]", this, st);
	}
	
	protected CallableStatement convert(final String sql, final CallableStatement st) {

		return new FilterCallable(this.owner + "/CALLABLE[" + ++this.index + "]", this, st, sql);
	}
	
	protected PreparedStatement convert(final String sql, final PreparedStatement st) {

		return new FilterPrepared(this.owner + "/PREPARED[" + ++this.index + "]", this, st, sql);
	}
	
	@Override
	public Array createArrayOf(final String typeName, final Object[] elements) {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public Blob createBlob() {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public Clob createClob() {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public NClob createNClob() throws SQLException {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public SQLXML createSQLXML() throws SQLException {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public Statement createStatement() throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.convert(this.parent.createStatement());
	}
	
	@Override
	public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.convert(this.parent.createStatement(resultSetType, resultSetConcurrency));
	}
	
	@Override
	public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.convert(this.parent.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
	}
	
	@Override
	public Struct createStruct(final String typeName, final Object[] attributes) {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean getAutoCommit() throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.parent.getAutoCommit();
	}
	
	@Override
	public String getCatalog() throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.parent.getCatalog();
	}
	
	@Override
	public Properties getClientInfo() {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getClientInfo(final String name) {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getHoldability() throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.parent.getHoldability();
	}
	
	@Override
	public DatabaseMetaData getMetaData() throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.parent.getMetaData();
	}
	
	@Override
	public int getNetworkTimeout() throws SQLException {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getSchema() throws SQLException {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getTransactionIsolation() throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.parent.getTransactionIsolation();
	}
	
	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.parent.getTypeMap();
	}
	
	@Override
	public SQLWarning getWarnings() throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.parent.getWarnings();
	}
	
	@Override
	public boolean isClosed() throws SQLException {

		return this.parent == null || this.parent.isClosed();
	}
	
	@Override
	public boolean isReadOnly() throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.parent.isReadOnly();
	}
	
	@Override
	public boolean isValid(final int timeout) {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isWrapperFor(final Class<?> iface) {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public String nativeSQL(final String sql) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.parent.nativeSQL(sql);
	}
	
	@Override
	public CallableStatement prepareCall(final String sql) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.convert(sql, this.parent.prepareCall(sql));
	}
	
	@Override
	public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.convert(sql, this.parent.prepareCall(sql, resultSetType, resultSetConcurrency));
	}
	
	@Override
	public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.convert(sql, this.parent.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
	}
	
	@Override
	public PreparedStatement prepareStatement(final String sql) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.convert(sql, this.parent.prepareStatement(sql));
	}
	
	@Override
	public PreparedStatement prepareStatement(final String sql, final int agKeys) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.convert(sql, this.parent.prepareStatement(sql, agKeys));
	}
	
	@Override
	public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.convert(sql, this.parent.prepareStatement(sql, resultSetType, resultSetConcurrency));
	}
	
	@Override
	public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultHoldability) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.convert(sql, this.parent.prepareStatement(sql, resultSetType, resultSetConcurrency, resultHoldability));
	}
	
	@Override
	public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.convert(sql, this.parent.prepareStatement(sql, columnIndexes));
	}
	
	@Override
	public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.convert(sql, this.parent.prepareStatement(sql, columnNames));
	}
	
	@Override
	public void releaseSavepoint(final java.sql.Savepoint savepoint) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		this.parent.releaseSavepoint(savepoint);
	}
	
	@Override
	public void rollback() throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		final long timeStart = System.currentTimeMillis();
		try {
			this.parent.rollback();
		} catch (final SQLException e) {
			this.logger.event(this.owner, "ROLLBACK", "error=" + e.getMessage());
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.treshold <= timeTook) {
				this.logger.event(this.owner, "ROLLBACK", "time=" + this.logger.formatPeriod(timeTook));
			}
		}
	}
	
	@Override
	public void rollback(final java.sql.Savepoint savepoint) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		final long timeStart = System.currentTimeMillis();
		try {
			this.parent.rollback(savepoint);
		} catch (final SQLException e) {
			this.logger.event(this.owner, "ROLLBACK_EX", "error=" + e.getMessage());
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.treshold <= timeTook) {
				this.logger.event(this.owner, "ROLLBACK_EX", "time=" + this.logger.formatPeriod(timeTook));
			}
		}
	}
	
	@Override
	public void setAutoCommit(final boolean autoCommit) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		this.parent.setAutoCommit(autoCommit);
	}
	
	@Override
	public void setCatalog(final String catalog) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		this.parent.setCatalog(catalog);
	}
	
	@Override
	public void setClientInfo(final Properties properties) throws SQLClientInfoException {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setClientInfo(final String name, final String value) throws SQLClientInfoException {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setHoldability(final int x) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		this.parent.setHoldability(x);
	}
	
	@Override
	public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setReadOnly(final boolean readOnly) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		this.parent.setReadOnly(readOnly);
	}
	
	@Override
	public java.sql.Savepoint setSavepoint() throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.parent.setSavepoint();
	}
	
	@Override
	public java.sql.Savepoint setSavepoint(final String x) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		return this.parent.setSavepoint(x);
	}
	
	@Override
	public void setSchema(final String schema) throws SQLException {

		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setTransactionIsolation(final int level) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		this.parent.setTransactionIsolation(level);
	}
	
	@Override
	public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {

		if (this.parent == null) {
			throw new SQLException("Connection is closed (" + this + ")!");
		}
		this.parent.setTypeMap(map);
	}
	
	@Override
	public <T> T unwrap(final Class<T> iface) {

		throw new UnsupportedOperationException();
	}
}
