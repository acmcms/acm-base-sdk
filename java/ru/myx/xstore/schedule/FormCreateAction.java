/*
 * Created on 27.05.2004
 */
package ru.myx.xstore.schedule;

import java.util.Collections;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.provide.ProvideRunner;
import ru.myx.ae1.provide.TaskRunner;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae3.Engine;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.help.Convert;

/**
 * @author myx
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
final class FormCreateAction extends AbstractForm<FormCreateAction> {
	private static final ControlCommand<?>	CMD_CREATE	= Control.createCommand( "create", " OK " )
																.setCommandPermission( "publish" )
																.setCommandIcon( "command-save" );
	
	private final BaseEntry<?>				entry;
	
	private final ChangeListing				schedule;
	
	private final BaseHostLookup				commandLookup;
	
	private final ControlFieldset<?>		fieldset;
	
	FormCreateAction(final BaseEntry<?> entry, final ChangeListing schedule, final BaseHostLookup commandLookup) {
		this.entry = entry;
		this.schedule = schedule;
		this.commandLookup = commandLookup;
		this.fieldset = ControlFieldset
				.createFieldset()
				.addField( ControlFieldFactory.createFieldDate( "scheduleDate",
						MultivariantString.getString( "Execution date",
								Collections.singletonMap( "ru", "Дата выполнения" ) ),
						Engine.fastTime() ) )
				.addField( ControlFieldFactory
						.createFieldString( "scheduleActor",
								MultivariantString.getString( "Task type",
										Collections.singletonMap( "ru", "Тип исполнителя" ) ),
								"",
								1,
								64 ).setFieldType( "select" ).setFieldVariant( "bigselect" )
						.setAttribute( "lookup", commandLookup ) );
		
		this.setAttributeIntern( "id", "entry_publishing_add" );
		this.setAttributeIntern( "title",
				MultivariantString.getString( "Schedule: add an action",
						Collections.singletonMap( "ru", "Планирование: добавление задания" ) ) );
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) throws Exception {
		if (command == FormCreateAction.CMD_CREATE) {
			final long scheduleDate = Convert.MapEntry.toLong( this.getData(), "scheduleDate", 0L );
			final String scheduleActor = Base.getString( this.getData(), "scheduleActor", "" );
			final TaskRunner runner = ProvideRunner.forName( scheduleActor );
			if (runner == null) {
				return new FormActionPropertiesCommand( this.entry,
						this.schedule,
						this.commandLookup,
						Engine.createGuid(),
						scheduleDate,
						scheduleActor,
						BaseObject.UNDEFINED );
			}
			final ControlFieldset<?> runnerFieldset = runner.getFieldset();
			if (runnerFieldset == null || runnerFieldset.isEmpty()) {
				this.schedule.schedule( new ActionBasic( this.entry.getType(),
						Engine.createGuid(),
						scheduleDate,
						"*",
						scheduleActor,
						BaseObject.UNDEFINED ) );
				return null;
			}
			return new FormActionPropertiesRunner( this.schedule,
					this.commandLookup,
					Engine.createGuid(),
					scheduleDate,
					runner,
					scheduleActor,
					BaseObject.UNDEFINED );
		}
		return super.getCommandResult( command, arguments );
	}
	
	@Override
	public ControlCommandset getCommands() {
		return Control.createOptionsSingleton( FormCreateAction.CMD_CREATE );
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		return this.fieldset;
	}
}
