/*
 * Created on 27.05.2004
 */
package ru.myx.xstore.schedule;

import java.util.Collections;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.provide.ProvideRunner;
import ru.myx.ae1.provide.TaskRunner;
import ru.myx.ae1.schedule.Action;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.types.Type;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractContainer;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;

final class ContainerEntrySchedule extends AbstractContainer<ContainerEntrySchedule> {
	private static final ControlCommand<?>	CMD_CREATE	= Control
																.createCommand( "create",
																		MultivariantString.getString( "Add",
																				Collections.singletonMap( "ru",
																						"Добавить" ) ) )
																.setCommandPermission( "publish" )
																.setCommandIcon( "command-create" );
	
	private static final ControlCommand<?>	CMD_CLEAR	= Control
																.createCommand( "clear",
																		MultivariantString.getString( "Clear",
																				Collections.singletonMap( "ru",
																						"Очистить" ) ) )
																.setCommandPermission( "publish" )
																.setCommandIcon( "command-clear" );
	
	private final BaseEntry<?>				entry;
	
	private final ChangeListing				schedule;
	
	private final BaseHostLookup				commandLookup;
	
	/**
	 * @param entry
	 * @param schedule
	 * @param commandLookup
	 */
	ContainerEntrySchedule(final BaseEntry<?> entry, final ChangeListing schedule, final BaseHostLookup commandLookup) {
		this.entry = entry;
		this.schedule = schedule;
		this.commandLookup = commandLookup;
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) throws Exception {
		if (command == ContainerEntrySchedule.CMD_CREATE) {
			return new FormCreateAction( this.entry, this.schedule, this.commandLookup );
		}
		if (command == ContainerEntrySchedule.CMD_CLEAR) {
			this.schedule.clear();
			return null;
		}
		if ("edit".equals( command.getKey() )) {
			final String key = Base.getString( command.getAttributes(), "key", null );
			final Action<?> action = this.schedule.get( key );
			if (action != null) {
				final String actor = action.getActor();
				final TaskRunner runner = ProvideRunner.forName( actor );
				if (runner == null) {
					final Type<?> type = this.entry.getType();
					if (type == null) {
						return "Type [" + this.entry.getTypeName() + "] was not found!";
					}
					return new FormActionPropertiesCommand( this.entry,
							this.schedule,
							this.commandLookup,
							key,
							action.getDate(),
							actor,
							action.getActorData() );
				}
				return new FormActionPropertiesRunner( this.schedule,
						this.commandLookup,
						key,
						action.getDate(),
						runner,
						actor,
						action.getActorData() );
			}
			return key + " was not found!";
		}
		if ("delete".equals( command.getKey() )) {
			final String key = Base.getString( command.getAttributes(), "key", null );
			this.schedule.scheduleCancelGuid( key );
			return null;
		}
		return super.getCommandResult( command, arguments );
	}
	
	@Override
	public ControlCommandset getCommands() {
		final ControlCommandset result = Control.createOptions();
		if (this.schedule.isEmpty()) {
			result.add( ContainerEntrySchedule.CMD_CREATE );
		} else {
			result.add( ContainerEntrySchedule.CMD_CREATE );
			result.add( ContainerEntrySchedule.CMD_CLEAR );
		}
		return result;
	}
	
	@Override
	public ControlCommandset getContentCommands(final String key) {
		final ControlCommandset result = Control.createOptions();
		result.add( Control
				.createCommand( "edit",
						MultivariantString.getString( "Properties", Collections.singletonMap( "ru", "Свойства" ) ) )
				.setCommandPermission( "publish" ).setCommandIcon( "command-edit" ).setAttribute( "key", key ) );
		result.add( Control
				.createCommand( "delete",
						MultivariantString.getString( "Delete", Collections.singletonMap( "ru", "Удалить" ) ) )
				.setCommandPermission( "publish" ).setCommandIcon( "command-delete" ).setAttribute( "key", key ) );
		return result;
	}
}
