/*
 * Created on 21.06.2004
 */
package ru.myx.xstore.forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.BaseSync;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.ControlBasic;
import ru.myx.ae3.control.ControlLookupStatic;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.help.Create;
import ru.myx.jdbc.lock.Locker;
import ru.myx.xstore.basics.Helper;

/**
 * @author myx
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FormEntrySynchronization extends AbstractForm<FormEntrySynchronization> {
	private final BaseEntry<?>				entry;
	
	private final Locker					lock;
	
	private final BaseSync					sync;
	
	private final ControlFieldset<?>		fieldset;
	
	private static final BaseHostLookup	LOOKUP_DIRECTION	= new ControlLookupStatic()
																		.putAppend( "import",
																				MultivariantString.getString( "import",
																						Collections.singletonMap( "ru",
																								"импорт" ) ) )
																		.putAppend( "export",
																				MultivariantString.getString( "export",
																						Collections.singletonMap( "ru",
																								"экспорт" ) ) )
																		.putAppend( "both",
																				MultivariantString.getString( "both",
																						Collections.singletonMap( "ru",
																								"оба направления" ) ) );
	
	private static final Object				STR_PATH			= MultivariantString.getString( "Path",
																		Collections.singletonMap( "ru", "Путь" ) );
	
	private static final Object				STR_DIRECTION		= MultivariantString
																		.getString( "Direction", Collections
																				.singletonMap( "ru", "Направление" ) );
	
	private static final Object				STR_LISTING			= MultivariantString
																		.getString( "Synchronization relations to",
																				Collections.singletonMap( "ru",
																						"Отношение к синхронизации" ) );
	
	private static final ControlCommand<?>	CMD_SAVE			= Control.createCommand( "save", " OK " )
																		.setCommandPermission( "publish" )
																		.setCommandIcon( "command-save" );
	
	private static final ControlCommand<?>	CMD_CLOSE			= Control.createCommand( "close", " * " )
																		.setCommandIcon( "command-close" );
	
	private static final ControlCommand<?>	CMD_CLEAR_ALL		= Control
																		.createCommand( "clearall",
																				MultivariantString
																						.getString( "Clear all",
																								Collections
																										.singletonMap( "ru",
																												"Удалить все" ) ) )
																		.setCommandPermission( "publish" )
																		.setCommandIcon( "command-delete" );
	
	private static final ControlCommand<?>	CMD_CLEAR_EXPORT	= Control
																		.createCommand( "clearexport",
																				MultivariantString
																						.getString( "Clear export",
																								Collections
																										.singletonMap( "ru",
																												"Удалить весь экспорт" ) ) )
																		.setCommandPermission( "publish" )
																		.setCommandIcon( "command-delete" );
	
	private static final ControlCommand<?>	CMD_CLEAR_IMPORT	= Control
																		.createCommand( "clearimport",
																				MultivariantString
																						.getString( "Clear import",
																								Collections
																										.singletonMap( "ru",
																												"Удалить весь импорт" ) ) )
																		.setCommandPermission( "publish" )
																		.setCommandIcon( "command-delete" );
	
	/**
	 * @param entry
	 * @param lock
	 */
	public FormEntrySynchronization(final BaseEntry<?> entry, final Locker lock) {
		this.entry = entry;
		this.lock = lock;
		this.sync = this.entry.getSynchronization();
		this.fieldset = ControlFieldset.createFieldset();
		final StorageImpl plugin = entry.getStorageImpl();
		final ControlFieldset<?> listingFieldset = ControlFieldset.createFieldset();
		listingFieldset.addField( ControlFieldFactory.createFieldString( "$path",
				FormEntrySynchronization.STR_PATH,
				"",
				1,
				255 ) );
		listingFieldset.addField( ControlFieldFactory
				.createFieldString( "$dir", FormEntrySynchronization.STR_DIRECTION, "" ).setFieldType( "select" )
				.setAttribute( "lookup", FormEntrySynchronization.LOOKUP_DIRECTION ) );
		listingFieldset.addFields( Helper.getContentListingFieldset( null ) );
		this.fieldset.addField( Control.createFieldList( "listing", FormEntrySynchronization.STR_LISTING, null )
				.setAttribute( "content_fieldset", listingFieldset )
				.setAttribute( "content_handler", new EntryListingContainerProvider( plugin, this.sync ) ) );
		{
			final Set<String> lookupExport = Create.tempSet();
			final Set<String> lookupImport = Create.tempSet();
			final Set<String> guids = Create.tempSet();
			final List<ControlBasic<?>> listing = new ArrayList<>();
			final BaseSync sync = entry.getSynchronization();
			if (sync != null) {
				final String[] export = sync.getExportSynchronizations();
				if (export != null) {
					for (final String guid : export) {
						final BaseEntry<?> candidate = plugin.getInterface().getByGuid( guid );
						if (candidate == null) {
							continue;
						}
						lookupExport.add( candidate.getGuid() );
						if (!guids.contains( candidate.getGuid() )) {
							listing.add( candidate );
							guids.add( candidate.getGuid() );
						}
					}
				}
				final String[] listimport = sync.getImportSynchronizations();
				if (listimport != null) {
					for (final String guid : listimport) {
						final BaseEntry<?> candidate = plugin.getInterface().getByGuid( guid );
						if (candidate == null) {
							continue;
						}
						lookupImport.add( candidate.getGuid() );
						if (!guids.contains( candidate.getGuid() )) {
							listing.add( candidate );
							guids.add( candidate.getGuid() );
						}
					}
				}
			}
			final BaseObject data = new BaseNativeObject()//
					.putAppend( "listing", //
							Base.forArray( new EntryListingFilter( Helper.getContentListingFieldset( null ), //
									listing,
									entry,
									lookupExport,
									lookupImport ) //
							) //
					)//
			;
			this.setData( data );
		}
		this.setAttributeIntern( "id", "sync" );
		this.setAttributeIntern( "title",
				MultivariantString.getString( "Synchronization settings",
						Collections.singletonMap( "ru", "Свойства синхронизации" ) ) );
		this.setAttributeIntern( "path", entry.getLocationControl() );
		if (lock != null) {
			this.setAttributeIntern( "on_close", FormEntrySynchronization.CMD_CLOSE );
		}
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		if (command == FormEntrySynchronization.CMD_SAVE) {
			this.sync.commit();
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		}
		if (command == FormEntrySynchronization.CMD_CLOSE) {
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		}
		if (command == FormEntrySynchronization.CMD_CLEAR_ALL) {
			this.sync.clear();
			this.sync.commit();
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		}
		if (command == FormEntrySynchronization.CMD_CLEAR_EXPORT) {
			for (final String current : this.sync.getExportSynchronizations()) {
				this.sync.synchronizeExportCancel( current );
			}
			this.sync.commit();
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		}
		if (command == FormEntrySynchronization.CMD_CLEAR_IMPORT) {
			for (final String current : this.sync.getImportSynchronizations()) {
				this.sync.synchronizeImportCancel( current );
			}
			this.sync.commit();
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		}
		throw new IllegalArgumentException( "Unknown command: " + command.getKey() );
	}
	
	@Override
	public ControlCommandset getCommands() {
		final ControlCommandset result = Control.createOptions();
		result.add( FormEntrySynchronization.CMD_SAVE );
		result.add( FormEntrySynchronization.CMD_CLEAR_ALL );
		result.add( FormEntrySynchronization.CMD_CLEAR_EXPORT );
		result.add( FormEntrySynchronization.CMD_CLEAR_IMPORT );
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		return this.fieldset;
	}
}
