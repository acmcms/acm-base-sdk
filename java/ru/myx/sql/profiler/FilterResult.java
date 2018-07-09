package ru.myx.sql.profiler;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * @author myx
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
final class FilterResult implements ResultSet {
	private ResultSet	parent;
	
	FilterResult(final ResultSet parent) {
		this.parent = parent;
	}
	
	@Override
	public boolean absolute(final int row) throws SQLException {
		return this.r().absolute( row );
	}
	
	@Override
	public void afterLast() throws SQLException {
		this.r().afterLast();
	}
	
	@Override
	public void beforeFirst() throws SQLException {
		this.r().beforeFirst();
	}
	
	@Override
	public void cancelRowUpdates() throws SQLException {
		this.r().cancelRowUpdates();
	}
	
	@Override
	public void clearWarnings() throws SQLException {
		this.r().clearWarnings();
	}
	
	@Override
	public void close() throws SQLException {
		this.r();
		this.parent.close();
		this.parent = null;
	}
	
	@Override
	public void deleteRow() throws SQLException {
		this.r().deleteRow();
	}
	
	@Override
	public int findColumn(final String columnName) throws SQLException {
		return this.r().findColumn( columnName );
	}
	
	@Override
	public boolean first() throws SQLException {
		return this.r().first();
	}
	
	@Override
	public Array getArray(final int i) throws SQLException {
		return this.r().getArray( i );
	}
	
	@Override
	public Array getArray(final String colName) throws SQLException {
		return this.r().getArray( colName );
	}
	
	@Override
	public InputStream getAsciiStream(final int columnIndex) throws SQLException {
		return this.r().getAsciiStream( columnIndex );
	}
	
	@Override
	public InputStream getAsciiStream(final String columnName) throws SQLException {
		return this.r().getAsciiStream( columnName );
	}
	
	@Override
	public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
		return this.r().getBigDecimal( columnIndex );
	}
	
	@Override
	public BigDecimal getBigDecimal(final int columnIndex, final int scale) {
		throw new NoSuchMethodError( "Deprecated!" );
	}
	
	@Override
	public BigDecimal getBigDecimal(final String columnName) throws SQLException {
		return this.r().getBigDecimal( columnName );
	}
	
	@Override
	public BigDecimal getBigDecimal(final String columnName, final int scale) {
		throw new NoSuchMethodError( "Deprecated!" );
	}
	
	@Override
	public InputStream getBinaryStream(final int columnIndex) throws SQLException {
		return this.r().getBinaryStream( columnIndex );
	}
	
	@Override
	public InputStream getBinaryStream(final String columnName) throws SQLException {
		return this.r().getBinaryStream( columnName );
	}
	
	@Override
	public Blob getBlob(final int i) throws SQLException {
		return this.r().getBlob( i );
	}
	
	@Override
	public Blob getBlob(final String colName) throws SQLException {
		return this.r().getBlob( colName );
	}
	
	@Override
	public boolean getBoolean(final int columnIndex) throws SQLException {
		return this.r().getBoolean( columnIndex );
	}
	
	@Override
	public boolean getBoolean(final String columnName) throws SQLException {
		return this.r().getBoolean( columnName );
	}
	
	@Override
	public byte getByte(final int columnIndex) throws SQLException {
		return this.r().getByte( columnIndex );
	}
	
	@Override
	public byte getByte(final String columnName) throws SQLException {
		return this.r().getByte( columnName );
	}
	
	@Override
	public byte[] getBytes(final int columnIndex) throws SQLException {
		return this.r().getBytes( columnIndex );
	}
	
	@Override
	public byte[] getBytes(final String columnName) throws SQLException {
		return this.r().getBytes( columnName );
	}
	
	@Override
	public Reader getCharacterStream(final int columnIndex) throws SQLException {
		return this.r().getCharacterStream( columnIndex );
	}
	
	@Override
	public Reader getCharacterStream(final String columnName) throws SQLException {
		return this.r().getCharacterStream( columnName );
	}
	
	@Override
	public Clob getClob(final int i) throws SQLException {
		return this.r().getClob( i );
	}
	
	@Override
	public Clob getClob(final String colName) throws SQLException {
		return this.r().getClob( colName );
	}
	
	@Override
	public int getConcurrency() throws SQLException {
		return this.r().getConcurrency();
	}
	
	@Override
	public String getCursorName() throws SQLException {
		return this.r().getCursorName();
	}
	
	@Override
	public Date getDate(final int columnIndex) throws SQLException {
		return this.r().getDate( columnIndex );
	}
	
	@Override
	public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
		return this.r().getDate( columnIndex, cal );
	}
	
	@Override
	public Date getDate(final String columnName) throws SQLException {
		return this.r().getDate( columnName );
	}
	
	@Override
	public Date getDate(final String columnName, final Calendar cal) throws SQLException {
		return this.r().getDate( columnName, cal );
	}
	
	@Override
	public double getDouble(final int columnIndex) throws SQLException {
		return this.r().getDouble( columnIndex );
	}
	
	@Override
	public double getDouble(final String columnName) throws SQLException {
		return this.r().getDouble( columnName );
	}
	
	@Override
	public int getFetchDirection() throws SQLException {
		return this.r().getFetchDirection();
	}
	
	@Override
	public int getFetchSize() throws SQLException {
		return this.r().getFetchSize();
	}
	
	@Override
	public float getFloat(final int columnIndex) throws SQLException {
		return this.r().getFloat( columnIndex );
	}
	
	@Override
	public float getFloat(final String columnName) throws SQLException {
		return this.r().getFloat( columnName );
	}
	
	@Override
	public int getHoldability() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getInt(final int columnIndex) throws SQLException {
		return this.r().getInt( columnIndex );
	}
	
	@Override
	public int getInt(final String columnName) throws SQLException {
		return this.r().getInt( columnName );
	}
	
	@Override
	public long getLong(final int columnIndex) throws SQLException {
		return this.r().getLong( columnIndex );
	}
	
	@Override
	public long getLong(final String columnName) throws SQLException {
		return this.r().getLong( columnName );
	}
	
	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return this.r().getMetaData();
	}
	
	@Override
	public Reader getNCharacterStream(final int columnIndex) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Reader getNCharacterStream(final String columnLabel) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public NClob getNClob(final int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public NClob getNClob(final String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getNString(final int columnIndex) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getNString(final String columnLabel) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Object getObject(final int columnIndex) throws SQLException {
		return this.r().getObject( columnIndex );
	}
	
	@Override
	public <T> T getObject(final int columnIndex, final Class<T> type) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Object getObject(final int i, final Map<String, Class<?>> map) throws SQLException {
		return this.r().getObject( i, map );
	}
	
	@Override
	public Object getObject(final String columnName) throws SQLException {
		return this.r().getObject( columnName );
	}
	
	@Override
	public <T> T getObject(final String columnLabel, final Class<T> type) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Object getObject(final String colName, final Map<String, Class<?>> map) throws SQLException {
		return this.r().getObject( colName, map );
	}
	
	@Override
	public Ref getRef(final int i) throws SQLException {
		return this.r().getRef( i );
	}
	
	@Override
	public Ref getRef(final String colName) throws SQLException {
		return this.r().getRef( colName );
	}
	
	@Override
	public int getRow() throws SQLException {
		return this.r().getRow();
	}
	
	@Override
	public RowId getRowId(final int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public RowId getRowId(final String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public short getShort(final int columnIndex) throws SQLException {
		return this.r().getShort( columnIndex );
	}
	
	@Override
	public short getShort(final String columnName) throws SQLException {
		return this.r().getShort( columnName );
	}
	
	@Override
	public SQLXML getSQLXML(final int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public SQLXML getSQLXML(final String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Statement getStatement() throws SQLException {
		return this.r().getStatement();
	}
	
	@Override
	public String getString(final int columnIndex) throws SQLException {
		return this.r().getString( columnIndex );
	}
	
	@Override
	public String getString(final String columnName) throws SQLException {
		return this.r().getString( columnName );
	}
	
	@Override
	public Time getTime(final int columnIndex) throws SQLException {
		return this.r().getTime( columnIndex );
	}
	
	@Override
	public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
		return this.r().getTime( columnIndex, cal );
	}
	
	@Override
	public Time getTime(final String columnName) throws SQLException {
		return this.r().getTime( columnName );
	}
	
	@Override
	public Time getTime(final String columnName, final Calendar cal) throws SQLException {
		return this.r().getTime( columnName, cal );
	}
	
	@Override
	public Timestamp getTimestamp(final int columnIndex) throws SQLException {
		return this.r().getTimestamp( columnIndex );
	}
	
	@Override
	public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
		
		return this.r().getTimestamp( columnIndex, cal );
	}
	
	@Override
	public Timestamp getTimestamp(final String columnName) throws SQLException {
		return this.r().getTimestamp( columnName );
	}
	
	@Override
	public Timestamp getTimestamp(final String columnName, final Calendar cal) throws SQLException {
		return this.r().getTimestamp( columnName, cal );
	}
	
	@Override
	public int getType() throws SQLException {
		return this.r().getType();
	}
	
	@Override
	public InputStream getUnicodeStream(final int columnIndex) {
		throw new NoSuchMethodError( "Deprecated!" );
	}
	
	@Override
	public InputStream getUnicodeStream(final String columnName) {
		throw new NoSuchMethodError( "Deprecated!" );
	}
	
	@Override
	public URL getURL(final int columnIndex) throws SQLException {
		return this.r().getURL( columnIndex );
	}
	
	@Override
	public URL getURL(final String columnName) throws SQLException {
		return this.r().getURL( columnName );
	}
	
	@Override
	public SQLWarning getWarnings() throws SQLException {
		return this.r().getWarnings();
	}
	
	@Override
	public void insertRow() throws SQLException {
		this.r().insertRow();
	}
	
	@Override
	public boolean isAfterLast() throws SQLException {
		return this.r().isAfterLast();
	}
	
	@Override
	public boolean isBeforeFirst() throws SQLException {
		return this.r().isBeforeFirst();
	}
	
	@Override
	public boolean isClosed() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isFirst() throws SQLException {
		return this.r().isFirst();
	}
	
	@Override
	public boolean isLast() throws SQLException {
		return this.r().isLast();
	}
	
	@Override
	public boolean isWrapperFor(final Class<?> iface) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean last() throws SQLException {
		return this.r().last();
	}
	
	@Override
	public void moveToCurrentRow() throws SQLException {
		this.r().moveToCurrentRow();
	}
	
	@Override
	public void moveToInsertRow() throws SQLException {
		this.r().moveToInsertRow();
	}
	
	@Override
	public boolean next() throws SQLException {
		return this.r().next();
	}
	
	@Override
	public boolean previous() throws SQLException {
		return this.r().previous();
	}
	
	private final ResultSet r() throws SQLException {
		if (this.parent == null) {
			throw new SQLException( "Result set is closed!" );
		}
		return this.parent;
	}
	
	@Override
	public void refreshRow() throws SQLException {
		this.r().refreshRow();
	}
	
	@Override
	public boolean relative(final int rows) throws SQLException {
		return this.r().relative( rows );
	}
	
	@Override
	public boolean rowDeleted() throws SQLException {
		return this.r().rowDeleted();
	}
	
	@Override
	public boolean rowInserted() throws SQLException {
		return this.r().rowInserted();
	}
	
	@Override
	public boolean rowUpdated() throws SQLException {
		return this.r().rowUpdated();
	}
	
	@Override
	public void setFetchDirection(final int direction) throws SQLException {
		this.r().setFetchDirection( direction );
	}
	
	@Override
	public void setFetchSize(final int rows) throws SQLException {
		this.r().setFetchSize( rows );
	}
	
	@Override
	public <T> T unwrap(final Class<T> iface) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateArray(final int columnIndex, final Array array) throws SQLException {
		this.r().updateArray( columnIndex, array );
	}
	
	@Override
	public void updateArray(final String columnName, final Array array) throws SQLException {
		this.r().updateArray( columnName, array );
	}
	
	@Override
	public void updateAsciiStream(final int columnIndex, final InputStream x) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateAsciiStream(final int columnIndex, final InputStream input, final int length) throws SQLException {
		this.r().updateAsciiStream( columnIndex, input, length );
	}
	
	@Override
	public void updateAsciiStream(final int columnIndex, final InputStream x, final long length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateAsciiStream(final String columnLabel, final InputStream x) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateAsciiStream(final String columnName, final InputStream input, final int length)
			throws SQLException {
		this.r().updateAsciiStream( columnName, input, length );
	}
	
	@Override
	public void updateAsciiStream(final String columnLabel, final InputStream x, final long length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateBigDecimal(final int columnIndex, final BigDecimal decimal) throws SQLException {
		this.r().updateBigDecimal( columnIndex, decimal );
	}
	
	@Override
	public void updateBigDecimal(final String columnName, final BigDecimal decimal) throws SQLException {
		this.r().updateBigDecimal( columnName, decimal );
	}
	
	@Override
	public void updateBinaryStream(final int columnIndex, final InputStream x) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateBinaryStream(final int columnIndex, final InputStream input, final int length)
			throws SQLException {
		this.r().updateBinaryStream( columnIndex, input, length );
	}
	
	@Override
	public void updateBinaryStream(final int columnIndex, final InputStream x, final long length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateBinaryStream(final String columnLabel, final InputStream x) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateBinaryStream(final String columnName, final InputStream input, final int length)
			throws SQLException {
		this.r().updateBinaryStream( columnName, input, length );
	}
	
	@Override
	public void updateBinaryStream(final String columnLabel, final InputStream x, final long length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateBlob(final int columnIndex, final Blob blob) throws SQLException {
		this.r().updateBlob( columnIndex, blob );
	}
	
	@Override
	public void updateBlob(final int columnIndex, final InputStream inputStream) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateBlob(final int columnIndex, final InputStream inputStream, final long length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateBlob(final String columnName, final Blob blob) throws SQLException {
		this.r().updateBlob( columnName, blob );
	}
	
	@Override
	public void updateBlob(final String columnLabel, final InputStream inputStream) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateBlob(final String columnLabel, final InputStream inputStream, final long length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateBoolean(final int columnIndex, final boolean x) throws SQLException {
		this.r().updateBoolean( columnIndex, x );
	}
	
	@Override
	public void updateBoolean(final String columnName, final boolean x) throws SQLException {
		this.r().updateBoolean( columnName, x );
	}
	
	@Override
	public void updateByte(final int columnIndex, final byte x) throws SQLException {
		this.r().updateByte( columnIndex, x );
	}
	
	@Override
	public void updateByte(final String columnName, final byte x) throws SQLException {
		this.r().updateByte( columnName, x );
	}
	
	@Override
	public void updateBytes(final int columnIndex, final byte[] bytes) throws SQLException {
		this.r().updateBytes( columnIndex, bytes );
	}
	
	@Override
	public void updateBytes(final String columnName, final byte[] bytes) throws SQLException {
		this.r().updateBytes( columnName, bytes );
	}
	
	@Override
	public void updateCharacterStream(final int columnIndex, final Reader x) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateCharacterStream(final int columnIndex, final Reader reader, final int length) throws SQLException {
		this.r().updateCharacterStream( columnIndex, reader, length );
	}
	
	@Override
	public void updateCharacterStream(final int columnIndex, final Reader x, final long length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateCharacterStream(final String columnLabel, final Reader reader) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateCharacterStream(final String columnName, final Reader reader, final int length)
			throws SQLException {
		this.r().updateCharacterStream( columnName, reader, length );
	}
	
	@Override
	public void updateCharacterStream(final String columnLabel, final Reader reader, final long length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateClob(final int columnIndex, final Clob clob) throws SQLException {
		this.r().updateClob( columnIndex, clob );
	}
	
	@Override
	public void updateClob(final int columnIndex, final Reader reader) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateClob(final int columnIndex, final Reader reader, final long length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateClob(final String columnName, final Clob clob) throws SQLException {
		this.r().updateClob( columnName, clob );
	}
	
	@Override
	public void updateClob(final String columnLabel, final Reader reader) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateClob(final String columnLabel, final Reader reader, final long length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateDate(final int columnIndex, final Date date) throws SQLException {
		this.r().updateDate( columnIndex, date );
	}
	
	@Override
	public void updateDate(final String columnName, final Date date) throws SQLException {
		this.r().updateDate( columnName, date );
	}
	
	@Override
	public void updateDouble(final int columnIndex, final double x) throws SQLException {
		this.r().updateDouble( columnIndex, x );
	}
	
	@Override
	public void updateDouble(final String columnName, final double x) throws SQLException {
		this.r().updateDouble( columnName, x );
	}
	
	@Override
	public void updateFloat(final int columnIndex, final float x) throws SQLException {
		this.r().updateFloat( columnIndex, x );
	}
	
	@Override
	public void updateFloat(final String columnName, final float x) throws SQLException {
		this.r().updateFloat( columnName, x );
	}
	
	@Override
	public void updateInt(final int columnIndex, final int x) throws SQLException {
		this.r().updateInt( columnIndex, x );
	}
	
	@Override
	public void updateInt(final String columnName, final int x) throws SQLException {
		this.r().updateInt( columnName, x );
	}
	
	@Override
	public void updateLong(final int columnIndex, final long x) throws SQLException {
		this.r().updateLong( columnIndex, x );
	}
	
	@Override
	public void updateLong(final String columnName, final long x) throws SQLException {
		this.r().updateLong( columnName, x );
	}
	
	@Override
	public void updateNCharacterStream(final int columnIndex, final Reader x) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateNCharacterStream(final int columnIndex, final Reader x, final long length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateNCharacterStream(final String columnLabel, final Reader reader) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateNCharacterStream(final String columnLabel, final Reader reader, final long length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateNClob(final int columnIndex, final NClob nClob) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateNClob(final int columnIndex, final Reader reader) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateNClob(final int columnIndex, final Reader reader, final long length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateNClob(final String columnLabel, final NClob nClob) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateNClob(final String columnLabel, final Reader reader) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateNClob(final String columnLabel, final Reader reader, final long length) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateNString(final int columnIndex, final String nString) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateNString(final String columnLabel, final String nString) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateNull(final int columnIndex) throws SQLException {
		this.r().updateNull( columnIndex );
	}
	
	@Override
	public void updateNull(final String columnName) throws SQLException {
		this.r().updateNull( columnName );
	}
	
	@Override
	public void updateObject(final int columnIndex, final Object object) throws SQLException {
		this.r().updateObject( columnIndex, object );
	}
	
	@Override
	public void updateObject(final int columnIndex, final Object object, final int scale) throws SQLException {
		this.r().updateObject( columnIndex, object, scale );
	}
	
	@Override
	public void updateObject(final String columnName, final Object object) throws SQLException {
		this.r().updateObject( columnName, object );
	}
	
	@Override
	public void updateObject(final String columnName, final Object object, final int scale) throws SQLException {
		this.r().updateObject( columnName, object, scale );
	}
	
	@Override
	public void updateRef(final int columnIndex, final Ref ref) throws SQLException {
		this.r().updateRef( columnIndex, ref );
	}
	
	@Override
	public void updateRef(final String columnName, final Ref ref) throws SQLException {
		this.r().updateRef( columnName, ref );
	}
	
	@Override
	public void updateRow() throws SQLException {
		this.r().updateRow();
	}
	
	@Override
	public void updateRowId(final int columnIndex, final RowId x) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateRowId(final String columnLabel, final RowId x) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateShort(final int columnIndex, final short x) throws SQLException {
		this.r().updateShort( columnIndex, x );
	}
	
	@Override
	public void updateShort(final String columnName, final short x) throws SQLException {
		this.r().updateShort( columnName, x );
	}
	
	@Override
	public void updateSQLXML(final int columnIndex, final SQLXML xmlObject) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateSQLXML(final String columnLabel, final SQLXML xmlObject) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateString(final int columnIndex, final String string) throws SQLException {
		this.r().updateString( columnIndex, string );
	}
	
	@Override
	public void updateString(final String columnName, final String string) throws SQLException {
		this.r().updateString( columnName, string );
	}
	
	@Override
	public void updateTime(final int columnIndex, final Time time) throws SQLException {
		this.r().updateTime( columnIndex, time );
	}
	
	@Override
	public void updateTime(final String columnName, final Time time) throws SQLException {
		this.r().updateTime( columnName, time );
	}
	
	@Override
	public void updateTimestamp(final int columnIndex, final Timestamp timestamp) throws SQLException {
		this.r().updateTimestamp( columnIndex, timestamp );
	}
	
	@Override
	public void updateTimestamp(final String columnName, final Timestamp timestamp) throws SQLException {
		this.r().updateTimestamp( columnName, timestamp );
	}
	
	@Override
	public boolean wasNull() throws SQLException {
		return this.r().wasNull();
	}
}
