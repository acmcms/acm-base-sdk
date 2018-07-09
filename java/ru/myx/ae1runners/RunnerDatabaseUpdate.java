package ru.myx.ae1runners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ru.myx.ae1.provide.TaskRunner;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.exec.ExecProcess;
import ru.myx.ae3.report.Report;

/**
 * Title: Scheduler plugin for WSM3 Description: Copyright: Copyright (c) 2001
 * Company:
 * 
 * @author Alexander I. Kharitchev
 * @version 1.0
 */

public class RunnerDatabaseUpdate implements TaskRunner {
	static final class Column {
		String	name;
		
		int		type;
		
		
		Column(final String name, final int type) {
		
			this.name = name;
			this.type = type;
		}
		
		
		@Override
		public String toString() {
		
			return this.name + "(" + this.type + ")";
		}
	}
	
	private static final String				OWNER				= "RT3_ST/DB_UPDATE";
	
	private static final ControlFieldset<?>	SettingsDefinition	= ControlFieldset
																		.createFieldset( "Database update" )
																		.addField( ControlFieldFactory
																				.createFieldString( "Source", "Source", "" )
																				.setFieldType( "select" )
																				.setAttribute( "lookup",
																						ru.myx.ae1.know.Know.ALL_POOLS ) )
																		.addField( ControlFieldFactory
																				.createFieldString( "Target", "Target", "" )
																				.setFieldType( "select" )
																				.setAttribute( "lookup",
																						ru.myx.ae1.know.Know.ALL_POOLS ) );
	
	
	private static List<String> getCommonTables(
			final Connection source,
			final Connection target) throws java.sql.SQLException {
	
		final List<String> sourceTables = new ArrayList<>();
		final List<String> targetTables = new ArrayList<>();
		
		final String[] types = { "TABLE" };
		
		try (final ResultSet rs = source.getMetaData().getTables( source.getCatalog(), "%", "%", types )) {
			try (final ResultSet rt = target.getMetaData().getTables( target.getCatalog(), "%", "%", types )) {
				while (rs.next()) {
					sourceTables.add( rs.getString( "TABLE_NAME" ) );
				}
				
				while (rt.next()) {
					targetTables.add( rt.getString( "TABLE_NAME" ) );
				}
			}
		}
		
		sourceTables.retainAll( targetTables );
		return sourceTables;
	}
	
	
	private static void transferRow(
			final Connection target,
			final List<Column> targetColumns,
			final String tableName,
			final ResultSet rs) throws SQLException {
	
		int checkCount = 0;
		final StringBuilder checkQuery = new StringBuilder().append( "SELECT COUNT(*) as Cnt FROM " ).append( tableName )
				.append( " WHERE " );
		{
			for (int i = 0; i < targetColumns.size(); ++i) {
				final String current;
				final Column column = targetColumns.get( i );
				switch (column.type) {
				case java.sql.Types.BIGINT:
				case java.sql.Types.BIT:
				case java.sql.Types.DOUBLE:
				case java.sql.Types.FLOAT:
				case java.sql.Types.INTEGER:
				case java.sql.Types.NUMERIC:
				case java.sql.Types.SMALLINT:
				case java.sql.Types.TINYINT:
				case java.sql.Types.REAL:
					current = column.name + "=" + rs.getString( column.name );
					break;
				case java.sql.Types.VARCHAR:
				case java.sql.Types.LONGVARCHAR:
					current = column.name + "=?";
					break;
				case java.sql.Types.DATE:
				case java.sql.Types.TIME:
				case java.sql.Types.TIMESTAMP:
					current = column.name + "=?";
					break;
				default:
					continue;
				}
				
				checkQuery.append( checkCount == 0
						? current
						: " AND " + current );
				checkCount++;
			}
		}
		
		boolean insertIt = checkCount < 2;
		
		if (!insertIt) {
			try (final PreparedStatement checkStatement = target.prepareStatement( checkQuery.toString() )) {
				int current = 0;
				for (int i = 0; i < targetColumns.size(); ++i) {
					final Column column = targetColumns.get( i );
					if (column.type == java.sql.Types.VARCHAR) {
						final String string = rs.getString( column.name );
						if (string != null) {
							checkStatement.setString( ++current, string );
						} else {
							checkStatement.setNull( ++current, column.type );
						}
					} else if (column.type == java.sql.Types.DATE
							|| column.type == java.sql.Types.TIME
							|| column.type == java.sql.Types.TIMESTAMP) {
						
						checkStatement.setTimestamp( ++current, rs.getTimestamp( column.name ) );
					} else {
						continue;
					}
					
				}
				
				try (final ResultSet rt = checkStatement.executeQuery()) {
					if (rt.next()) {
						insertIt = rt.getInt( 1 ) == 0;
					} else {
						insertIt = true;
					}
				}
			}
		}
		
		if (!insertIt) {
			return;
		}
		
		final StringBuilder partFields = new StringBuilder();
		final StringBuilder partValues = new StringBuilder();
		{
			for (int i = 0; i < targetColumns.size(); ++i) {
				String current;
				
				final Column column = targetColumns.get( i );
				if (column.type == java.sql.Types.BIGINT
						|| column.type == java.sql.Types.BIT
						|| column.type == java.sql.Types.DOUBLE
						|| column.type == java.sql.Types.FLOAT
						|| column.type == java.sql.Types.INTEGER
						|| column.type == java.sql.Types.NUMERIC
						|| column.type == java.sql.Types.SMALLINT) {
					current = rs.getString( column.name );
				} else {
					current = "?";
				}
				
				partFields.append( i == 0
						? column.name
						: "," + column.name );
				partValues.append( i == 0
						? current
						: "," + current );
			}
		}
		
		final String insertQuery = "INSERT INTO " + tableName + "(" + partFields + ") VALUES (" + partValues + ")";
		System.out.println( ">>> " + insertQuery );
		try (final PreparedStatement insert = target.prepareStatement( insertQuery )) {
			int index = 0;
			for (int i = 0; i < targetColumns.size(); ++i) {
				
				final Column column = targetColumns.get( i );
				if (column.type == java.sql.Types.BIGINT
						|| column.type == java.sql.Types.BIT
						|| column.type == java.sql.Types.DOUBLE
						|| column.type == java.sql.Types.FLOAT
						|| column.type == java.sql.Types.INTEGER
						|| column.type == java.sql.Types.NUMERIC
						|| column.type == java.sql.Types.SMALLINT) {
					continue;
				}
				
				if (column.type == java.sql.Types.VARCHAR) {
					String string = rs.getString( column.name );
					if (string != null) {
						if (string.length() > 128 * 1024) {
							string = null;
							final java.sql.Clob Clb = rs.getClob( column.name );
							insert.setClob( ++index, Clb );
						} else {
							insert.setString( ++index, string );
						}
					} else {
						insert.setNull( ++index, column.type );
					}
				} else if (column.type == java.sql.Types.LONGVARCHAR || column.type == java.sql.Types.CLOB) {
					java.sql.Clob Clb = rs.getClob( column.name );
					if (Clb != null) {
						final int Length = (int) Clb.length();
						
						if (Length < 1024 * 128) {
							insert.setString( ++index, Clb.getSubString( 1L, (int) Clb.length() ) );
						} else if (Length < 1024 * 1024) {
							++index;
							try {
								insert.setClob( index, Clb );
							} catch (final Throwable t) {
								t.printStackTrace();
								try {
									insert.setCharacterStream( index, Clb.getCharacterStream(), Length );
								} catch (final Throwable tt) {
									tt.printStackTrace();
									insert.setAsciiStream( index, Clb.getAsciiStream(), Length );
								}
							}
						} else {
							Clb = null;
							insert.setAsciiStream( ++index, rs.getAsciiStream( column.name ), Length );
						}
					} else {
						insert.setNull( ++index, column.type );
					}
				} else if (column.type == java.sql.Types.BLOB
						|| column.type == java.sql.Types.LONGVARBINARY
						|| column.type == java.sql.Types.VARBINARY) {
					final byte[] b = rs.getBytes( column.name );
					if (b != null) {
						insert.setBytes( ++index, b );
					} else {
						insert.setNull( ++index, column.type );
						// final java.sql.Blob B =
						// RS.getBlob(C.Name);
						// long length;
						// try{
						// length = B == null ? 0 : B.length();
						// }catch(NullPointerException e){
						// length = 0;
						// }
						// if(length > 0) Insert.setBlob(++Index,B);
						// else Insert.setNull(++Index,C.Type);
					}
				} else if (column.type == java.sql.Types.DATE
						|| column.type == java.sql.Types.TIME
						|| column.type == java.sql.Types.TIMESTAMP) {
					final java.sql.Timestamp T = rs.getTimestamp( column.name );
					if (T != null) {
						insert.setTimestamp( ++index, T );
					} else {
						insert.setNull( ++index, column.type );
					}
				} else {
					final Object O = rs.getObject( column.name );
					if (O != null) {
						insert.setObject( ++index, O );
					} else {
						insert.setNull( ++index, column.type );
					}
				}
			}
			
			insert.executeUpdate();
		}
	}
	
	
	private static void transferTable(
			final Connection source,
			final Connection target,
			final String table) throws java.sql.SQLException {
	
		final List<Column> SourceColumns = new ArrayList<>();
		final List<Column> targetColumns = new ArrayList<>();
		
		try (final ResultSet rs = source.getMetaData().getColumns( source.getCatalog(), "%", table, "%" );
				final ResultSet rt = target.getMetaData().getColumns( target.getCatalog(), "%", table, "%" )) {
			while (rs.next()) {
				SourceColumns.add( new Column( rs.getString( "COLUMN_NAME" ), rs.getInt( "DATA_TYPE" ) ) );
			}
			
			while (rt.next()) {
				targetColumns.add( new Column( rt.getString( "COLUMN_NAME" ), rt.getInt( "DATA_TYPE" ) ) );
			}
		}
		{
			if (targetColumns.size() != SourceColumns.size()) {
				Report.error( RunnerDatabaseUpdate.OWNER, "tables named '" + table + "' have different column counts!" );
				throw new Error( "Wrong column number at " + table + " table!" );
			}
			
			if (targetColumns.size() == 0) {
				Report.error( RunnerDatabaseUpdate.OWNER, "tables named '" + table + "' have 0 columns!" );
				throw new Error( "Wrong column number at " + table + " table!" );
			}
			
			try (final Statement st = source.createStatement()) {
				try {
					st.setCursorName( "dbTransfer" );
				} catch (final Throwable t) {
					// empty
				}
				try (final ResultSet rs = st.executeQuery( "SELECT * FROM " + table )) {
					while (rs.next()) {
						RunnerDatabaseUpdate.transferRow( target, targetColumns, table, rs );
					}
				}
			}
		}
	}
	
	
	/**
	 * 
	 */
	public RunnerDatabaseUpdate() {
	
		// empty
	}
	
	
	@Override
	public String describe(
			final BaseObject settings) {
	
		final String sourceName = Base.getString( settings, "Source", "" );
		final String targetName = Base.getString( settings, "Target", "" );
		return "transfer " + sourceName + " database to a " + targetName + " database";
	}
	
	
	@Override
	public ControlFieldset<?> getFieldset() {
	
		return RunnerDatabaseUpdate.SettingsDefinition;
	}
	
	
	@Override
	public Class<?> getParameterClass() {
	
		return null;
	}
	
	
	@Override
	public String getTitle() {
	
		return "Database update (with checks for existance)";
	}
	
	
	@Override
	public Object run(
			final ExecProcess process,
			final BaseObject settings) throws java.lang.Throwable {
	
		final String sourceName = Base.getString( settings, "Source", "" );
		final String targetName = Base.getString( settings, "Target", "" );
		
		try (final Connection source = Context.getServer( process ).getServerConnection( sourceName );
				final Connection target = Context.getServer( process ).getServerConnection( targetName )) {
			
			if (source == null) {
				throw new Error( "Can't proceed - Source pool is unknown!" );
			}
			if (target == null) {
				throw new Error( "Can't proceed - Target pool is unknown!" );
			}
			
			final List<String> tables = RunnerDatabaseUpdate.getCommonTables( source, target );
			
			for (final String tableName : tables) {
				try {
					RunnerDatabaseUpdate.transferTable( source, target, tableName );
				} catch (final java.sql.SQLException e) {
					Report.exception( RunnerDatabaseUpdate.OWNER, "DatabaseTransfer: transferring '" + tableName + "'...", e );
				}
			}
		} catch (final SQLException e) {
			throw new RuntimeException( "Error while transfering", e );
		} catch (final RuntimeException e) {
			e.printStackTrace();
			throw e;
		} catch (final Error e) {
			e.printStackTrace();
			throw e;
		}
		return null;
	}
}
