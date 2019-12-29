package ru.myx.sql.profiler;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/** Title: ae1 Base definitions Description: Copyright: Copyright (c) 2001 Company:
 *
 * @author Alexander I. Kharitchev
 * @version 1.0 */
final class FilterCallable extends FilterPrepared implements CallableStatement {
	
	FilterCallable(final String owner, final FilterConnection connection, final CallableStatement parent, final String sql) {
		
		super(owner, connection, parent, sql);
	}

	protected final CallableStatement c() throws SQLException {
		
		if (this.parent == null) {
			throw new SQLException("Statement is closed (" + this + ")!");
		}
		return (CallableStatement) this.parent;
	}

	@Override
	public void closeOnCompletion() throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Array getArray(final int i) throws SQLException {
		
		return this.c().getArray(i);
	}

	@Override
	public Array getArray(final String name) throws SQLException {
		
		return this.c().getArray(name);
	}

	@Override
	public BigDecimal getBigDecimal(final int parameterIndex) throws SQLException {
		
		return this.c().getBigDecimal(parameterIndex);
	}

	@Deprecated
	@Override
	public BigDecimal getBigDecimal(final int parameterIndex, final int scale) {
		
		throw new NoSuchMethodError("Deprecated!");
	}

	@Override
	public BigDecimal getBigDecimal(final String name) throws SQLException {
		
		return this.c().getBigDecimal(name);
	}

	@Override
	public Blob getBlob(final int i) throws SQLException {
		
		return this.c().getBlob(i);
	}

	@Override
	public Blob getBlob(final String name) throws SQLException {
		
		return this.c().getBlob(name);
	}

	@Override
	public boolean getBoolean(final int parameterIndex) throws SQLException {
		
		return this.c().getBoolean(parameterIndex);
	}

	@Override
	public boolean getBoolean(final String name) throws SQLException {
		
		return this.c().getBoolean(name);
	}

	@Override
	public byte getByte(final int parameterIndex) throws SQLException {
		
		return this.c().getByte(parameterIndex);
	}

	@Override
	public byte getByte(final String name) throws SQLException {
		
		return this.c().getByte(name);
	}

	@Override
	public byte[] getBytes(final int parameterIndex) throws SQLException {
		
		return this.c().getBytes(parameterIndex);
	}

	@Override
	public byte[] getBytes(final String name) throws SQLException {
		
		return this.c().getBytes(name);
	}

	@Override
	public Reader getCharacterStream(final int parameterIndex) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Reader getCharacterStream(final String parameterName) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Clob getClob(final int i) throws SQLException {
		
		return this.c().getClob(i);
	}

	@Override
	public Clob getClob(final String name) throws SQLException {
		
		return this.c().getClob(name);
	}

	@Override
	public java.sql.Date getDate(final int parameterIndex) throws SQLException {
		
		return this.c().getDate(parameterIndex);
	}

	@Override
	public java.sql.Date getDate(final int parameterIndex, final Calendar cal) throws SQLException {
		
		return this.c().getDate(parameterIndex, cal);
	}

	@Override
	public java.sql.Date getDate(final String name) throws SQLException {
		
		return this.c().getDate(name);
	}

	@Override
	public java.sql.Date getDate(final String parameterName, final Calendar cal) throws SQLException {
		
		return this.c().getDate(parameterName, cal);
	}

	@Override
	public double getDouble(final int parameterIndex) throws SQLException {
		
		return this.c().getDouble(parameterIndex);
	}

	@Override
	public double getDouble(final String name) throws SQLException {
		
		return this.c().getDouble(name);
	}

	@Override
	public float getFloat(final int parameterIndex) throws SQLException {
		
		return this.c().getFloat(parameterIndex);
	}

	@Override
	public float getFloat(final String name) throws SQLException {
		
		return this.c().getFloat(name);
	}

	@Override
	public int getInt(final int parameterIndex) throws SQLException {
		
		return this.c().getInt(parameterIndex);
	}

	@Override
	public int getInt(final String name) throws SQLException {
		
		return this.c().getInt(name);
	}

	@Override
	public long getLong(final int parameterIndex) throws SQLException {
		
		return this.c().getLong(parameterIndex);
	}

	@Override
	public long getLong(final String name) throws SQLException {
		
		return this.c().getLong(name);
	}

	@Override
	public Reader getNCharacterStream(final int parameterIndex) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Reader getNCharacterStream(final String parameterName) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public NClob getNClob(final int parameterIndex) throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public NClob getNClob(final String parameterName) throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNString(final int parameterIndex) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNString(final String parameterName) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getObject(final int parameterIndex) throws SQLException {
		
		return this.c().getObject(parameterIndex);
	}

	@Override
	public <T> T getObject(final int parameterIndex, final Class<T> type) throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getObject(final int i, final Map<String, Class<?>> map) throws SQLException {
		
		return this.c().getObject(i, map);
	}

	@Override
	public Object getObject(final String name) throws SQLException {
		
		return this.c().getObject(name);
	}

	@Override
	public <T> T getObject(final String parameterName, final Class<T> type) throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getObject(final String name, final Map<String, Class<?>> map) throws SQLException {
		
		return this.c().getObject(name, map);
	}

	@Override
	public Ref getRef(final int i) throws SQLException {
		
		return this.c().getRef(i);
	}

	@Override
	public Ref getRef(final String name) throws SQLException {
		
		return this.c().getRef(name);
	}

	@Override
	public RowId getRowId(final int parameterIndex) throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public RowId getRowId(final String parameterName) throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public short getShort(final int parameterIndex) throws SQLException {
		
		return this.c().getShort(parameterIndex);
	}

	@Override
	public short getShort(final String name) throws SQLException {
		
		return this.c().getShort(name);
	}

	@Override
	public SQLXML getSQLXML(final int parameterIndex) throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLXML getSQLXML(final String parameterName) throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public String getString(final int parameterIndex) throws SQLException {
		
		return this.c().getString(parameterIndex);
	}

	@Override
	public String getString(final String name) throws SQLException {
		
		return this.c().getString(name);
	}

	@Override
	public Time getTime(final int parameterIndex) throws SQLException {
		
		return this.c().getTime(parameterIndex);
	}

	@Override
	public Time getTime(final int parameterIndex, final Calendar cal) throws SQLException {
		
		return this.c().getTime(parameterIndex, cal);
	}

	@Override
	public Time getTime(final String name) throws SQLException {
		
		return this.c().getTime(name);
	}

	@Override
	public Time getTime(final String parameterName, final Calendar cal) throws SQLException {
		
		return this.c().getTime(parameterName, cal);
	}

	@Override
	public Timestamp getTimestamp(final int parameterIndex) throws SQLException {
		
		return this.c().getTimestamp(parameterIndex);
	}

	@Override
	public Timestamp getTimestamp(final int parameterIndex, final Calendar cal) throws SQLException {
		
		return this.c().getTimestamp(parameterIndex, cal);
	}

	@Override
	public Timestamp getTimestamp(final String name) throws SQLException {
		
		return this.c().getTimestamp(name);
	}

	@Override
	public Timestamp getTimestamp(final String parameterName, final Calendar cal) throws SQLException {
		
		return this.c().getTimestamp(parameterName, cal);
	}

	@Override
	public java.net.URL getURL(final int parameterIndex) throws java.sql.SQLException {
		
		return this.c().getURL(parameterIndex);
	}

	@Override
	public java.net.URL getURL(final String parameterName) throws java.sql.SQLException {
		
		return this.c().getURL(parameterName);
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
	public void registerOutParameter(final int parameterIndex, final int sqlType) throws SQLException {
		
		this.c().registerOutParameter(parameterIndex, sqlType);
	}

	@Override
	public void registerOutParameter(final int parameterIndex, final int sqlType, final int scale) throws SQLException {
		
		this.c().registerOutParameter(parameterIndex, sqlType, scale);
	}

	@Override
	public void registerOutParameter(final int paramIndex, final int sqlType, final String typeName) throws SQLException {
		
		this.c().registerOutParameter(paramIndex, sqlType, typeName);
	}

	@Override
	public void registerOutParameter(final String name, final int sqlType) throws SQLException {
		
		this.c().registerOutParameter(name, sqlType);
	}

	@Override
	public void registerOutParameter(final String name, final int sqlType, final int scale) throws SQLException {
		
		this.c().registerOutParameter(name, sqlType, scale);
	}

	@Override
	public void registerOutParameter(final String name, final int sqlType, final String typeName) throws SQLException {
		
		this.c().registerOutParameter(name, sqlType, typeName);
	}

	@Override
	public void setAsciiStream(final int parameterIndex, final InputStream x) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAsciiStream(final int parameterIndex, final InputStream x, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAsciiStream(final String parameterName, final InputStream x) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAsciiStream(final String parameterName, final InputStream x, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAsciiStream(final String name, final java.io.InputStream input, final int length) throws java.sql.SQLException {
		
		this.c().setAsciiStream(name, input, length);
	}

	@Override
	public void setBigDecimal(final String name, final java.math.BigDecimal decimal) throws java.sql.SQLException {
		
		this.c().setBigDecimal(name, decimal);
	}

	@Override
	public void setBinaryStream(final int parameterIndex, final InputStream x) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBinaryStream(final int parameterIndex, final InputStream x, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBinaryStream(final String parameterName, final InputStream x) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBinaryStream(final String parameterName, final InputStream x, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBinaryStream(final String name, final java.io.InputStream input, final int length) throws java.sql.SQLException {
		
		this.c().setBinaryStream(name, input, length);
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
	public void setBlob(final String parameterName, final Blob x) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBlob(final String parameterName, final InputStream inputStream) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBlob(final String parameterName, final InputStream inputStream, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBoolean(final String name, final boolean x) throws java.sql.SQLException {
		
		this.c().setBoolean(name, x);
	}

	@Override
	public void setByte(final String name, final byte x) throws java.sql.SQLException {
		
		this.c().setByte(name, x);
	}

	@Override
	public void setBytes(final String name, final byte[] bytes) throws java.sql.SQLException {
		
		this.c().setBytes(name, bytes);
	}

	@Override
	public void setCharacterStream(final int parameterIndex, final Reader reader) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCharacterStream(final int parameterIndex, final Reader reader, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCharacterStream(final String name, final java.io.Reader reader, final int length) throws java.sql.SQLException {
		
		this.c().setCharacterStream(name, reader, length);
	}

	@Override
	public void setCharacterStream(final String parameterName, final Reader reader) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCharacterStream(final String parameterName, final Reader reader, final long length) {
		
		throw new UnsupportedOperationException();
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
	public void setClob(final String parameterName, final Clob x) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setClob(final String parameterName, final Reader reader) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setClob(final String parameterName, final Reader reader, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDate(final int index, final java.sql.Date date, final java.util.Calendar cal) throws java.sql.SQLException {
		
		this.c().setDate(index, date, cal);
	}

	@Override
	public void setDate(final String name, final java.sql.Date date) throws java.sql.SQLException {
		
		this.c().setDate(name, date);
	}

	@Override
	public void setDate(final String name, final java.sql.Date date, final java.util.Calendar cal) throws java.sql.SQLException {
		
		this.c().setDate(name, date, cal);
	}

	@Override
	public void setDouble(final String name, final double x) throws java.sql.SQLException {
		
		this.c().setDouble(name, x);
	}

	@Override
	public void setFloat(final String name, final float x) throws java.sql.SQLException {
		
		this.c().setFloat(name, x);
	}

	@Override
	public void setInt(final String name, final int x) throws java.sql.SQLException {
		
		this.c().setInt(name, x);
	}

	@Override
	public void setLong(final String name, final long x) throws java.sql.SQLException {
		
		this.c().setLong(name, x);
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
	public void setNCharacterStream(final String parameterName, final Reader value) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNCharacterStream(final String parameterName, final Reader value, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNClob(final int parameterIndex, final NClob value) {
		
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
	public void setNClob(final String parameterName, final NClob value) throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNClob(final String parameterName, final Reader reader) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNClob(final String parameterName, final Reader reader, final long length) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNString(final int parameterIndex, final String value) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNString(final String parameterName, final String value) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNull(final int pIndex, final int sType) throws java.sql.SQLException {
		
		this.c().setNull(pIndex, sType);
	}

	@Override
	public void setNull(final int pIndex, final int sType, final String typeName) throws java.sql.SQLException {
		
		this.c().setNull(pIndex, sType, typeName);
	}

	@Override
	public void setNull(final String pName, final int sType) throws java.sql.SQLException {
		
		this.c().setNull(pName, sType);
	}

	@Override
	public void setNull(final String pName, final int sType, final String typeName) throws java.sql.SQLException {
		
		this.c().setNull(pName, sType, typeName);
	}

	@Override
	public void setObject(final String name, final Object object) throws java.sql.SQLException {
		
		this.c().setObject(name, object);
	}

	@Override
	public void setObject(final String name, final Object object, final int sType) throws java.sql.SQLException {
		
		this.c().setObject(name, object, sType);
	}

	@Override
	public void setObject(final String name, final Object object, final int sType, final int scale) throws java.sql.SQLException {
		
		this.c().setObject(name, object, sType, scale);
	}

	@Override
	public void setPoolable(final boolean poolable) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRowId(final int parameterIndex, final RowId x) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRowId(final String parameterName, final RowId x) throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setShort(final String name, final short x) throws java.sql.SQLException {
		
		this.c().setShort(name, x);
	}

	@Override
	public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSQLXML(final String parameterName, final SQLXML xmlObject) throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setString(final String name, final String string) throws java.sql.SQLException {
		
		this.c().setString(name, string);
	}

	@Override
	public void setTime(final int index, final java.sql.Time time, final java.util.Calendar cal) throws java.sql.SQLException {
		
		this.c().setTime(index, time, cal);
	}

	@Override
	public void setTime(final String name, final java.sql.Time time) throws java.sql.SQLException {
		
		this.c().setTime(name, time);
	}

	@Override
	public void setTime(final String name, final java.sql.Time time, final java.util.Calendar cal) throws java.sql.SQLException {
		
		this.c().setTime(name, time, cal);
	}

	@Override
	public void setTimestamp(final int index, final java.sql.Timestamp timestamp, final java.util.Calendar cal) throws java.sql.SQLException {
		
		this.c().setTimestamp(index, timestamp, cal);
	}

	@Override
	public void setTimestamp(final String name, final java.sql.Timestamp timestamp) throws java.sql.SQLException {
		
		this.c().setTimestamp(name, timestamp);
	}

	@Override
	public void setTimestamp(final String name, final java.sql.Timestamp timestamp, final java.util.Calendar cal) throws java.sql.SQLException {
		
		this.c().setTimestamp(name, timestamp, cal);
	}

	@Override
	public void setURL(final String name, final java.net.URL url) throws java.sql.SQLException {
		
		this.c().setURL(name, url);
	}

	@Override
	public <T> T unwrap(final Class<T> iface) {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean wasNull() throws SQLException {
		
		return this.c().wasNull();
	}
}
