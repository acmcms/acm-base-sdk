package ru.myx.sql.profiler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/** Title: ae1 Base definitions Description: Copyright: Copyright (c) 2001 Company:
 *
 * @author Alexander I. Kharitchev
 * @version 1.0 */
class FilterStatement implements Statement {
	
	protected final String owner;

	protected final FilterConnection connection;

	protected Statement parent;

	protected int batchSize = 0;

	private StringBuilder batchBuffer;
	
	FilterStatement(final String owner, final FilterConnection connection, final Statement parent) {
		
		this.owner = owner;
		this.connection = connection;
		this.parent = parent;
	}
	
	@Override
	public void addBatch(final String sql) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		this.batchSize++;
		if (this.batchBuffer == null) {
			this.batchBuffer = new StringBuilder();
		}
		this.batchBuffer.append(sql).append("\r\n");
		this.parent.addBatch(sql);
	}
	
	@Override
	public void cancel() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		this.parent.cancel();
	}
	
	@Override
	public void clearBatch() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		this.batchSize = 0;
		this.batchBuffer = null;
		this.parent.clearBatch();
	}
	
	@Override
	public void clearWarnings() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		this.parent.clearWarnings();
	}
	
	@Override
	public void close() throws SQLException {
		
		if (this.parent != null) {
			this.parent.close();
			this.parent = null;
		}
		this.batchBuffer = null;
	}
	
	@Override
	public void closeOnCompletion() throws SQLException {
		
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean execute(final String sql) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		final long timeStart = System.currentTimeMillis();
		try {
			return this.parent.execute(sql);
		} catch (final SQLException e) {
			this.connection.logger.event(this.owner, "EXECUTE", "error=" + e.getMessage() + ", query=" + sql);
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.connection.treshold <= timeTook) {
				this.connection.logger.event(this.owner, "EXECUTE", "time=" + this.connection.logger.formatPeriod(timeTook) + ", query=" + sql);
			}
		}
	}
	
	@Override
	public boolean execute(final String sql, final int agKeys) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		final long timeStart = System.currentTimeMillis();
		try {
			return this.parent.execute(sql, agKeys);
		} catch (final SQLException e) {
			this.connection.logger.event(this.owner, "EXECUTE_EX", "error=" + e.getMessage() + ", query=" + sql);
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.connection.treshold <= timeTook) {
				this.connection.logger.event(this.owner, "EXECUTE_EX", "time=" + this.connection.logger.formatPeriod(timeTook) + ", query=" + sql);
			}
		}
	}
	
	@Override
	public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		final long timeStart = System.currentTimeMillis();
		try {
			return this.parent.execute(sql, columnIndexes);
		} catch (final SQLException e) {
			this.connection.logger.event(this.owner, "EXECUTE_EX", "error=" + e.getMessage() + ", query=" + sql);
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.connection.treshold <= timeTook) {
				this.connection.logger.event(this.owner, "EXECUTE_EX", "time=" + this.connection.logger.formatPeriod(timeTook) + ", query=" + sql);
			}
		}
	}
	
	@Override
	public boolean execute(final String sql, final String[] columnNames) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		final long timeStart = System.currentTimeMillis();
		try {
			return this.parent.execute(sql, columnNames);
		} catch (final SQLException e) {
			this.connection.logger.event(this.owner, "EXECUTE_EX", "error=" + e.getMessage() + ", query=" + sql);
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.connection.treshold <= timeTook) {
				this.connection.logger.event(this.owner, "EXECUTE_EX", "time=" + this.connection.logger.formatPeriod(timeTook) + ", query=" + sql);
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
						.event(this.owner, "EXECUTE_BATCH", "time=" + this.connection.logger.formatPeriod(timeTook) + ", size=" + this.batchSize + "\r\n" + this.batchBuffer);
			}
			this.batchSize = 0;
			this.batchBuffer = null;
		}
	}
	
	@Override
	public ResultSet executeQuery(final String sql) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		final long timeStart = System.currentTimeMillis();
		try {
			return new FilterResult(this.parent.executeQuery(sql));
		} catch (final SQLException e) {
			this.connection.logger.event(this.owner, "EXECUTE_QUERY", "error=" + e.getMessage() + ", query=" + sql);
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.connection.treshold <= timeTook) {
				this.connection.logger.event(this.owner, "EXECUTE_QUERY", "time=" + this.connection.logger.formatPeriod(timeTook) + ", query=" + sql);
			}
		}
	}
	
	@Override
	public int executeUpdate(final String sql) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		final long timeStart = System.currentTimeMillis();
		try {
			return this.parent.executeUpdate(sql);
		} catch (final SQLException e) {
			this.connection.logger.event(this.owner, "EXECUTE_UPDATE", "error=" + e.getMessage() + ", query=" + sql);
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.connection.treshold <= timeTook) {
				this.connection.logger.event(this.owner, "EXECUTE_UPDATE", "time=" + this.connection.logger.formatPeriod(timeTook) + ", query=" + sql);
			}
		}
	}
	
	@Override
	public int executeUpdate(final String sql, final int agKeys) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		final long timeStart = System.currentTimeMillis();
		try {
			return this.parent.executeUpdate(sql, agKeys);
		} catch (final SQLException e) {
			this.connection.logger.event(this.owner, "EXECUTE_UPDATE_EX", "error=" + e.getMessage() + ", query=" + sql);
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.connection.treshold <= timeTook) {
				this.connection.logger.event(this.owner, "EXECUTE_UPDATE_EX", "time=" + this.connection.logger.formatPeriod(timeTook) + ", query=" + sql);
			}
		}
	}
	
	@Override
	public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		final long timeStart = System.currentTimeMillis();
		try {
			return this.parent.executeUpdate(sql, columnIndexes);
		} catch (final SQLException e) {
			this.connection.logger.event(this.owner, "EXECUTE_UPDATE_EX", "error=" + e.getMessage() + ", query=" + sql);
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.connection.treshold <= timeTook) {
				this.connection.logger.event(this.owner, "EXECUTE_UPDATE_EX", "time=" + this.connection.logger.formatPeriod(timeTook) + ", query=" + sql);
			}
		}
	}
	
	@Override
	public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		final long timeStart = System.currentTimeMillis();
		try {
			return this.parent.executeUpdate(sql, columnNames);
		} catch (final SQLException e) {
			this.connection.logger.event(this.owner, "EXECUTE_UPDATE_EX", "error=" + e.getMessage() + ", query=" + sql);
			throw e;
		} finally {
			final long timeTook = System.currentTimeMillis() - timeStart;
			if (this.connection.treshold <= timeTook) {
				this.connection.logger.event(this.owner, "EXECUTE_UPDATE_EX", "time=" + this.connection.logger.formatPeriod(timeTook) + ", query=" + sql);
			}
		}
	}
	
	@Override
	public Connection getConnection() {
		
		return this.connection;
	}
	
	@Override
	public int getFetchDirection() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		return this.parent.getFetchDirection();
	}
	
	@Override
	public int getFetchSize() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		return this.parent.getFetchSize();
	}
	
	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		return this.parent.getGeneratedKeys();
	}
	
	@Override
	public int getMaxFieldSize() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		return this.parent.getMaxFieldSize();
	}
	
	@Override
	public int getMaxRows() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		return this.parent.getMaxRows();
	}
	
	@Override
	public boolean getMoreResults() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		return this.parent.getMoreResults();
	}
	
	@Override
	public boolean getMoreResults(final int current) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		return this.parent.getMoreResults(current);
	}
	
	@Override
	public int getQueryTimeout() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		return this.parent.getQueryTimeout();
	}
	
	@Override
	public ResultSet getResultSet() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		final ResultSet rs = this.parent.getResultSet();
		return rs == null
			? null
			: new FilterResult(rs);
	}
	
	@Override
	public int getResultSetConcurrency() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		return this.parent.getResultSetConcurrency();
	}
	
	@Override
	public int getResultSetHoldability() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		return this.parent.getResultSetHoldability();
	}
	
	@Override
	public int getResultSetType() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		return this.parent.getResultSetType();
	}
	
	@Override
	public int getUpdateCount() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		return this.parent.getUpdateCount();
	}
	
	@Override
	public SQLWarning getWarnings() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		return this.parent.getWarnings();
	}
	
	@Override
	public boolean isClosed() {
		
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		
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
	
	@Override
	public void setCursorName(final String name) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		this.parent.setCursorName(name);
	}
	
	@Override
	public void setEscapeProcessing(final boolean enable) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		this.parent.setEscapeProcessing(enable);
	}
	
	@Override
	public void setFetchDirection(final int direction) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		this.parent.setFetchDirection(direction);
	}
	
	@Override
	public void setFetchSize(final int rows) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		this.parent.setFetchSize(rows);
	}
	
	@Override
	public void setMaxFieldSize(final int max) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		this.parent.setMaxFieldSize(max);
	}
	
	@Override
	public void setMaxRows(final int max) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		this.parent.setMaxRows(max);
	}
	
	@Override
	public void setPoolable(final boolean poolable) {
		
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setQueryTimeout(final int seconds) throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed!");
		}
		this.parent.setQueryTimeout(seconds);
	}
	
	@Override
	public <T> T unwrap(final Class<T> iface) {
		
		throw new UnsupportedOperationException();
	}
}
