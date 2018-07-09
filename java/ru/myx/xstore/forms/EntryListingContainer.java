/*
 * Created on 31.01.2006
 */
package ru.myx.xstore.forms;

import java.util.Arrays;
import java.util.Collections;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.ControlEntry;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.BaseSync;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseArray;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractContainer;
import ru.myx.ae3.control.ControlActor;
import ru.myx.ae3.control.ControlContainer;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.xstore.basics.ControlNodeImpl;

final class EntryListingContainer extends AbstractContainer<EntryListingContainer> {
	private static final Object	STR_SYNC_CANCEL_EXPORT	= MultivariantString.getString( "Cancel export",
																Collections.singletonMap( "ru", "Отменить экспорт" ) );
	
	private static final Object	STR_SYNC_CANCEL_IMPORT	= MultivariantString.getString( "Cancel import",
																Collections.singletonMap( "ru", "Отменить импорт" ) );
	
	private static final Object	STR_SYNC_CREATE_EXPORT	= MultivariantString.getString( "Set export",
																Collections.singletonMap( "ru", "Установить экспорт" ) );
	
	private static final Object	STR_SYNC_CREATE_IMPORT	= MultivariantString.getString( "Set import",
																Collections.singletonMap( "ru", "Установить импорт" ) );
	
	private final StorageImpl	parent;
	
	private final BaseSync		synchronization;
	
	EntryListingContainer(final StorageImpl parent, final BaseSync synchronization) {
		this.parent = parent;
		this.synchronization = synchronization;
	}
	
	@Override
	public final Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments)
			throws Exception {
		if ("syncCancelExport".equals( command.getKey() )) {
			final String key = Base.getString( command.getAttributes(), "$key", null );
			this.synchronization.synchronizeExportCancel( key );
			return null;
		}
		if ("syncCancelImport".equals( command.getKey() )) {
			final String key = Base.getString( command.getAttributes(), "$key", null );
			this.synchronization.synchronizeImportCancel( key );
			return null;
		}
		if ("syncCreateExport".equals( command.getKey() )) {
			final String key = Base.getString( command.getAttributes(), "$key", null );
			this.synchronization.synchronizeExport( key );
			return null;
		}
		if ("syncCreateImport".equals( command.getKey() )) {
			final String key = Base.getString( command.getAttributes(), "$key", null );
			this.synchronization.synchronizeImport( key );
			return null;
		}
		final ControlActor<?> actor = (ControlActor<?>) command.getAttributes().baseGet( "$actor", null );
		if (actor != null) {
			final ControlCommand<?> requested = (ControlCommand<?>) command.getAttributes().baseGet( "$original", null );
			return actor.getCommandResult( requested, arguments );
		}
		return super.getCommandResult( command, arguments );
	}
	
	@Override
	public final ControlCommandset getContentCommands(final String key) {
		final ControlCommandset result = Control.createOptions();
		{
			final BaseEntry<?> entry = this.parent.getInterface().getByGuid( key );
			if (entry != null) {
				{
					if (this.synchronization != null) {
						final BaseSync sync = this.synchronization;
						if (sync != null) {
							final String[] exportList = sync.getExportSynchronizations();
							if (exportList != null
									&& exportList.length > 0
									&& Arrays.asList( exportList ).contains( key )) {
								result.add( Control
										.createCommand( "syncCancelExport",
												EntryListingContainer.STR_SYNC_CANCEL_EXPORT )
										.setCommandIcon( "command-delete" ).setAttribute( "$key", key ) );
							} else {
								result.add( Control
										.createCommand( "syncCreateExport",
												EntryListingContainer.STR_SYNC_CREATE_EXPORT )
										.setCommandIcon( "command-create" ).setAttribute( "$key", key ) );
							}
							final String[] importList = sync.getImportSynchronizations();
							if (importList != null
									&& importList.length > 0
									&& Arrays.asList( importList ).contains( key )) {
								result.add( Control
										.createCommand( "syncCancelImport",
												EntryListingContainer.STR_SYNC_CANCEL_IMPORT )
										.setCommandIcon( "command-delete" ).setAttribute( "$key", key ) );
							} else {
								result.add( Control
										.createCommand( "syncCreateImport",
												EntryListingContainer.STR_SYNC_CREATE_IMPORT )
										.setCommandIcon( "command-create" ).setAttribute( "$key", key ) );
							}
						}
					}
					final ControlEntry<?> actor = new ControlNodeImpl( this.parent, key );
					{
						final ControlCommandset source = actor.getForms();
						if (source != null && !source.isEmpty()) {
							for (final ControlCommand<?> command : source) {
								result.add( Control.createCommand( command.getKey(), command.getTitle() )
										.setAttributes( command.getAttributes() ).setAttribute( "$original", command )
										.setAttribute( "$actor", actor ) );
							}
						}
					}
					{
						final ControlCommandset source = actor.getCommands();
						if (source != null && !source.isEmpty()) {
							for (final ControlCommand<?> command : source) {
								result.add( Control.createCommand( command.getKey(), command.getTitle() )
										.setAttributes( command.getAttributes() ).setAttribute( "$original", command )
										.setAttribute( "$actor", actor ) );
							}
						}
					}
				}
				final BaseEntry<?> parent = entry.getParent();
				if (parent != null) {
					final ControlContainer<?> actor = new ControlNodeImpl( this.parent, parent.getGuid() );
					final ControlCommandset source = actor.getContentCommands( entry.getKey() );
					if (source != null && !source.isEmpty()) {
						for (final ControlCommand<?> command : source) {
							result.add( Control.createCommand( "cntn" + command.getKey(), command.getTitle() )
									.setAttributes( command.getAttributes() ).setAttribute( "$original", command )
									.setAttribute( "$actor", actor ) );
						}
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public final ControlCommandset getContentMultipleCommands(final BaseArray keys) {
		return null;
	}
	
}
