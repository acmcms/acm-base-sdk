package ru.myx.ae1runners;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.provide.TaskRunner;
import ru.myx.ae3.Engine;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.exec.ExecProcess;
import ru.myx.ae3.help.Text;
import ru.myx.ae3.report.Report;

/** Title: Scheduler plugin for WSM3 Description: Copyright: Copyright (c) 2001 Company:
 *
 * @author Alexander I. Kharitchev
 * @version 1.0 */
public final class RunnerDatabaseTransfer implements TaskRunner {
	
	static final class Column {
		
		final String name;

		final int type;

		Column(final String name, final int type) {
			
			this.name = name;
			this.type = type;
		}

		@Override
		public String toString() {
			
			return this.name + "(" + this.type + ")";
		}
	}

	private static final String OWNER = "RT3_ST/DB_TRANSFER";

	private static final ControlFieldset<?> settingsDefinition = ControlFieldset.createFieldset()
			.addField(ControlFieldFactory.createFieldString("Source", "Source", "").setFieldType("select").setAttribute("lookup", ru.myx.ae1.know.Know.ALL_POOLS))
			.addField(ControlFieldFactory.createFieldString("Target", "Target", "").setFieldType("select").setAttribute("lookup", ru.myx.ae1.know.Know.ALL_POOLS))
			.addField(ControlFieldFactory.createFieldBoolean("cursors", "Use server cursors", true))
			.addField(ControlFieldFactory.createFieldString("ignore", "Exclude tables", "").setFieldType("text").setFieldHint("table1, table2, table3...")).addField(
					ControlFieldFactory.createFieldString("include", "Include tables", "").setFieldType("text")
							.setFieldHint("Only these tables copied when this field is not empty\r\ntable1, table2, table3..."));

	private static final Object TITLE = MultivariantString
			.getString("Database transfer (raw copy / no checks)", Collections.singletonMap("ru", "Перенос БД (копирование без проверок)"));

	private static Column getColumn(final List<Column> sourceColumns, final List<Column> targetColumns, final int index) {
		
		final Column target = targetColumns.get(index);
		if (target.type == Types.OTHER) {
			return sourceColumns.get(index);
		}
		return target;
	}

	private static List<String> getCommonTables(final Connection source, final Connection target) throws SQLException {
		
		final List<String> sourceTables = new ArrayList<>();
		final List<String> targetTables = new ArrayList<>();
		final String[] types = {
				"TABLE"
		};
		final String sourceSchema = source.getMetaData().getDatabaseProductName().toLowerCase().indexOf("oracle") == -1
			? "%"
			: source.getMetaData().getUserName();
		final String targetSchema = target.getMetaData().getDatabaseProductName().toLowerCase().indexOf("oracle") == -1
			? "%"
			: target.getMetaData().getUserName();
		final boolean lower = target.getMetaData().storesLowerCaseIdentifiers() || source.getMetaData().storesLowerCaseIdentifiers();
		final boolean upper = target.getMetaData().storesUpperCaseIdentifiers() || source.getMetaData().storesUpperCaseIdentifiers();
		final boolean insencetive = lower || upper;
		try (final ResultSet rs = source.getMetaData().getTables(source.getCatalog(), sourceSchema, "%", types)) {
			Report.event(RunnerDatabaseTransfer.OWNER, "CASE-CHECK", "Target table name case-insencetivity: " + insencetive);
			Report.event(RunnerDatabaseTransfer.OWNER, "Source table", "Start scan, catalog: " + source.getCatalog() + ", scheme name: " + sourceSchema);
			while (rs.next()) {
				final String tableName = rs.getString("TABLE_NAME");
				sourceTables.add(tableName);
				Report.event(RunnerDatabaseTransfer.OWNER, "Source table", tableName + ", type=" + rs.getString("TABLE_TYPE"));
			}
			Report.event(RunnerDatabaseTransfer.OWNER, "Source table", "Done scan");
		}
		try (final ResultSet rt = target.getMetaData().getTables(target.getCatalog(), targetSchema, "%", types)) {
			Report.event(RunnerDatabaseTransfer.OWNER, "Target table", "Start scan, catalog: " + target.getCatalog() + ", scheme name: " + targetSchema);
			while (rt.next()) {
				final String tableName = rt.getString("TABLE_NAME");
				targetTables.add(
						insencetive
							? tableName.toUpperCase()
							: tableName);
				Report.event(RunnerDatabaseTransfer.OWNER, "Target table", tableName + ", type=" + rt.getString("TABLE_TYPE"));
			}
			Report.event(RunnerDatabaseTransfer.OWNER, "Target table", "Done scan");
		}
		if (insencetive) {
			for (final String tableName : sourceTables.toArray(new String[sourceTables.size()])) {
				if (!targetTables.contains(tableName.toUpperCase())) {
					sourceTables.remove(tableName);
				}
			}
		} else {
			sourceTables.retainAll(targetTables);
		}
		return sourceTables;
	}

	private static void transferRow(final Connection target, final List<Column> sourceColumns, final List<Column> targetColumns, final String tableName, final ResultSet rs)
			throws SQLException {
		
		final StringBuilder fieldsPart = new StringBuilder();
		final StringBuilder valuesPart = new StringBuilder();
		{
			for (int i = 0; i < targetColumns.size(); ++i) {
				String current;
				final Column column = RunnerDatabaseTransfer.getColumn(sourceColumns, targetColumns, i);
				if (column.type == Types.BIGINT || column.type == Types.BIT || column.type == Types.DOUBLE || column.type == Types.FLOAT || column.type == Types.INTEGER
						|| column.type == Types.NUMERIC || column.type == Types.DECIMAL || column.type == Types.TINYINT || column.type == Types.SMALLINT) {
					current = rs.getString(column.name);
				} else {
					current = "?";
				}
				fieldsPart.append(
						i == 0
							? column.name
							: "," + column.name);
				valuesPart.append(
						i == 0
							? current
							: "," + current);
			}
		}
		final String insertQuery = "INSERT INTO " + tableName + "(" + fieldsPart + ") VALUES (" + valuesPart + ")";
		Report.event(RunnerDatabaseTransfer.OWNER, "QUERY", insertQuery);
		try (final PreparedStatement insert = target.prepareStatement(insertQuery)) {
			int index = 0;
			for (int i = 0; i < targetColumns.size(); ++i) {
				final Column column = RunnerDatabaseTransfer.getColumn(sourceColumns, targetColumns, i);
				if (column.type == Types.BIGINT || column.type == Types.BIT || column.type == Types.DOUBLE || column.type == Types.FLOAT || column.type == Types.INTEGER
						|| column.type == Types.NUMERIC || column.type == Types.DECIMAL || column.type == Types.TINYINT || column.type == Types.SMALLINT) {
					continue;
				}
				if (column.type == Types.VARCHAR) {
					final String string = rs.getString(column.name);
					if (string != null) {
						if (string.length() > 128 * 1024) {
							final Clob clob = rs.getClob(column.name);
							try {
								insert.setClob(++index, clob);
							} catch (final SQLException e) {
								Report.exception(
										RunnerDatabaseTransfer.OWNER,
										"SET-FAIL",
										"While setting " + index + " parameter (type=CLOB) in [ " + insertQuery + " ] statement",
										e);
								return;
							}
						} else {
							try {
								insert.setString(++index, string);
							} catch (final SQLException e) {
								Report.exception(
										RunnerDatabaseTransfer.OWNER,
										"SET-FAIL",
										"While setting " + index + " parameter (type=STRING) in [ " + insertQuery + " ] statement",
										e);
								return;
							}
						}
					} else {
						insert.setNull(++index, column.type);
					}
				} else if (column.type == Types.LONGVARCHAR || column.type == Types.CLOB) {
					final Clob clob = rs.getClob(column.name);
					if (clob != null) {
						final int length = (int) clob.length();
						if (length < 1024 * 128) {
							insert.setString(++index, clob.getSubString(1L, (int) clob.length()));
						} else if (length < 1024 * 1024) {
							++index;
							try {
								insert.setClob(index, clob);
							} catch (final Throwable t) {
								t.printStackTrace();
								try {
									insert.setCharacterStream(index, clob.getCharacterStream(), length);
								} catch (final Throwable tt) {
									tt.printStackTrace();
									insert.setAsciiStream(index, clob.getAsciiStream(), length);
								}
							}
						} else {
							insert.setAsciiStream(++index, rs.getAsciiStream(column.name), length);
						}
					} else {
						insert.setNull(++index, column.type);
					}
				} else if (column.type == Types.BLOB || column.type == Types.LONGVARBINARY || column.type == Types.VARBINARY) {
					final byte[] bytes = rs.getBytes(column.name);
					if (bytes != null) {
						insert.setBytes(++index, bytes);
					} else {
						insert.setNull(++index, column.type);
					}
				} else if (column.type == Types.DATE || column.type == Types.TIME || column.type == Types.TIMESTAMP) {
					final Timestamp timestamp = rs.getTimestamp(column.name);
					if (timestamp != null) {
						insert.setTimestamp(++index, timestamp);
					} else {
						insert.setNull(++index, column.type);
					}
				} else {
					final Object object = rs.getObject(column.name);
					if (object != null) {
						try {
							insert.setObject(++index, object);
						} catch (final SQLException e) {
							Report.exception(
									RunnerDatabaseTransfer.OWNER,
									"SET-FAIL",
									"While setting " + index + " parameter (type=" + column.type + ") to a " + object.getClass().getName() + " instance in [ " + insertQuery
											+ " ] statement",
									e);
							return;
						}
					} else {
						try {
							insert.setNull(++index, column.type);
						} catch (final SQLException e) {
							Report.exception(
									RunnerDatabaseTransfer.OWNER,
									"SET-FAIL",
									"While setting " + index + " parameter (type=" + column.type + ") to a NULL value in [ " + insertQuery + " ] statement",
									e);
							return;
						}
					}
				}
			}
			try {
				insert.executeUpdate();
			} catch (final SQLException e) {
				if (Report.MODE_DEBUG) {
					Report.exception(RunnerDatabaseTransfer.OWNER, "INSERT-FAIL", "Error while executing: " + insertQuery, e);
				} else {
					Report.event(RunnerDatabaseTransfer.OWNER, "INSERT-FAIL", "Error inserting: " + e.getMessage());
				}
			}
		}
	}

	private static void transferTable(final Connection source, final Connection target, final String table, final boolean cursors) throws SQLException {
		
		Report.event(RunnerDatabaseTransfer.OWNER, "TABLE-TRANSFER-STARTED", "Table name: " + table);
		try {
			final List<Column> sourceColumns = new ArrayList<>();
			final List<Column> targetColumns = new ArrayList<>();
			final String sourceSchema = source.getMetaData().getDatabaseProductName().toLowerCase().indexOf("oracle") == -1
				? "%"
				: source.getMetaData().getUserName();
			final String targetSchema = target.getMetaData().getDatabaseProductName().toLowerCase().indexOf("oracle") == -1
				? "%"
				: target.getMetaData().getUserName();
			try (final ResultSet rs = source.getMetaData().getColumns(source.getCatalog(), sourceSchema, table, "%")) {
				final boolean upper = target.getMetaData().storesUpperCaseIdentifiers();
				final boolean lower = target.getMetaData().storesLowerCaseIdentifiers();
				if (target.getMetaData().getDatabaseProductName().equalsIgnoreCase("Microsoft SQL Server")) {
					try (final Statement st = target.createStatement()) {
						st.execute("SET IDENTITY_INSERT " + table + " ON");
					} catch (final SQLException e) {
						Report.exception(RunnerDatabaseTransfer.OWNER, "Error setting identity insert", e);
					}
				}
				try (final ResultSet rt = target.getMetaData().getColumns(
						target.getCatalog(),
						targetSchema,
						upper
							? table.toUpperCase()
							: lower
								? table.toLowerCase()
								: table,
						"%")) {
					while (rs.next()) {
						sourceColumns.add(new Column(rs.getString("COLUMN_NAME"), rs.getInt("DATA_TYPE")));
					}
					while (rt.next()) {
						targetColumns.add(new Column(rt.getString("COLUMN_NAME"), rt.getInt("DATA_TYPE")));
					}
				}
			}
			if (targetColumns.size() != sourceColumns.size()) {
				Report.error(
						RunnerDatabaseTransfer.OWNER,
						"tables named '" + table + "' have different column counts!\r\nSource: " + sourceColumns.size() + ", " + sourceColumns + "\r\nTarget: "
								+ targetColumns.size() + ", " + targetColumns);
				throw new Error("Wrong column number at " + table + " table!");
			}
			if (targetColumns.size() == 0) {
				Report.error(RunnerDatabaseTransfer.OWNER, "tables named '" + table + "' have 0 columns!");
				throw new Error("Wrong column number at " + table + " table!");
			}
			try (final Statement st = source.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)) {
				if (cursors) {
					try {
						st.setCursorName("dbTransfer_" + Engine.createGuid());
					} catch (final Throwable t) {
						// ignore
					}
				}
				try (final ResultSet rs = st.executeQuery("SELECT * FROM " + table)) {
					while (rs.next()) {
						RunnerDatabaseTransfer.transferRow(target, sourceColumns, targetColumns, table, rs);
					}
				}
			}
		} finally {
			if (target.getMetaData().getDatabaseProductName().equalsIgnoreCase("Microsoft SQL Server")) {
				try (final Statement st = target.createStatement()) {
					st.execute("SET IDENTITY_INSERT " + table + " OFF");
				} catch (final SQLException e) {
					Report.exception(RunnerDatabaseTransfer.OWNER, "Error re-setting identity insert", e);
				}
			}
			Report.event(RunnerDatabaseTransfer.OWNER, "TABLE-TRANSFER-FINISHED", "Table name: " + table);
		}
	}

	/**
	 *
	 */
	public RunnerDatabaseTransfer() {
		
		// empty
	}

	@Override
	public String describe(final BaseObject settings) {
		
		final String sourceName = Base.getString(settings, "Source", "");
		final String targetName = Base.getString(settings, "Target", "");
		return "transfer " + sourceName + " database to a " + targetName + " database";
	}

	@Override
	public ControlFieldset<?> getFieldset() {
		
		return RunnerDatabaseTransfer.settingsDefinition;
	}

	@Override
	public Class<?> getParameterClass() {
		
		return null;
	}

	@Override
	public String getTitle() {
		
		return RunnerDatabaseTransfer.TITLE.toString();
	}

	@Override
	public Object run(final ExecProcess process, final BaseObject settings) throws java.lang.Throwable {
		
		final String sourceName = Base.getString(settings, "Source", "").trim();
		final String targetName = Base.getString(settings, "Target", "").trim();
		final String ignoreTables = Base.getString(settings, "ignore", "").trim();
		final String includeTables = Base.getString(settings, "include", "").trim();
		final boolean cursors = Base.getBoolean(settings, "cursors", true);
		Report.event(RunnerDatabaseTransfer.OWNER, "TRANSFER-INIT", "sourceId=" + sourceName + ", targetId=" + targetName + ", ignore=" + ignoreTables);
		try (final Connection source = Context.getServer(process).getServerConnection(sourceName);
				final Connection target = Context.getServer(process).getServerConnection(targetName)) {
			if (source == null) {
				throw new Error("Can't proceed - Source pool is unknown!");
			}
			if (target == null) {
				throw new Error("Can't proceed - Target pool is unknown!");
			}

			Report.event(
					RunnerDatabaseTransfer.OWNER,
					"CONNECTED",
					"sourceCatalog=" + source.getCatalog() + ", sourceSchema=" + source.getMetaData().getUserName() + ", targetCatalog=" + target.getCatalog() + ", targetSchema="
							+ target.getMetaData().getUserName());

			final List<String> tables = includeTables.length() == 0
				? RunnerDatabaseTransfer.getCommonTables(source, target)
				: Arrays.asList(Text.toStringArray(includeTables, ",", Integer.MAX_VALUE));
			Report.event(RunnerDatabaseTransfer.OWNER, "COMMON", "Common tables: " + String.valueOf(tables));
			final List<?> ignoreList = Arrays.asList(Text.toStringArray(ignoreTables.toLowerCase(), ",", Integer.MAX_VALUE));
			for (int i = 0; i < tables.size(); ++i) {
				try {
					final String tableName = tables.get(i);
					if (ignoreList.contains(tableName.toLowerCase())) {
						Report.event(RunnerDatabaseTransfer.OWNER, "IGNORE", "skipping table: " + tableName);
						continue;
					}
					RunnerDatabaseTransfer.transferTable(source, target, tableName, cursors);
				} catch (final SQLException e) {
					Report.exception(RunnerDatabaseTransfer.OWNER, "TABLE-FAIL", "DatabaseTransfer: transferring '" + tables.get(i) + "'...", e);
				}
			}
		} catch (final SQLException e) {
			throw new RuntimeException("Error while transfering", e);
		} catch (final Error | RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
		return null;
	}
}
