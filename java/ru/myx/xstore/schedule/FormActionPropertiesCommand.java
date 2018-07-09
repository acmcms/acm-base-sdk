/*
 * Created on 27.05.2004
 */
package ru.myx.xstore.schedule;

import java.util.Collections;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.schedule.Action;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.types.Type;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.base.BaseNativeObject;
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
final class FormActionPropertiesCommand extends AbstractForm<FormActionPropertiesCommand> {
	private final BaseEntry<?>				entry;
	
	private final Type<?>					type;
	
	private final ChangeListing				schedule;
	
	private final String					scheduleId;
	
	private final long						scheduleDate;
	
	private final String					scheduleActor;
	
	private final ControlFieldset<?>		fieldset;
	
	private static final ControlCommand<?>	CMD_CREATE	= Control.createCommand( "create", " OK " )
																.setCommandPermission( "publish" )
																.setCommandIcon( "command-save" );
	
	FormActionPropertiesCommand(final BaseEntry<?> entry,
			final ChangeListing schedule,
			final BaseHostLookup commandLookup,
			final String scheduleId,
			final long scheduleDate,
			final String scheduleActor,
			final BaseObject parameters) {
		this.entry = entry;
		this.type = entry.getType();
		this.schedule = schedule;
		this.scheduleId = scheduleId;
		this.scheduleDate = scheduleDate;
		this.scheduleActor = scheduleActor;
		this.fieldset = ControlFieldset
				.createFieldset()
				.addField( ControlFieldFactory.createFieldDate( "scheduleDate",
						MultivariantString.getString( "Execution date",
								Collections.singletonMap( "ru", "Дата выполнения" ) ),
						scheduleDate ) )
				.addField( ControlFieldFactory
						.createFieldString( "scheduleActor",
								MultivariantString.getString( "Task type",
										Collections.singletonMap( "ru", "Тип исполнителя" ) ),
								scheduleActor,
								1,
								64 ).setConstant().setFieldType( "select" ).setFieldVariant( "bigselect" )
						.setAttribute( "lookup", commandLookup ) );
		final ControlFieldset<?> runnerFieldset = this.type.getCommandFieldset( scheduleActor );
		if (runnerFieldset != null && Base.hasKeys( runnerFieldset )) {
			final BaseObject parametersNew = new BaseNativeObject( parameters );
			this.type.scriptCommandFormPrepare( scheduleActor, entry, parametersNew );
			this.setData( new BaseNativeObject( "parameters", parametersNew ) );
			
			this.fieldset.addField( ControlFieldFactory
					.createFieldMap( "parameters",
							MultivariantString.getString( "Additional parameters",
									Collections.singletonMap( "ru", "Параметры команды" ) ),
							parameters ).setFieldVariant( "fieldset" ).setAttribute( "fieldset", runnerFieldset ) );
		}
		
		this.setAttributeIntern( "id", "entry_publishing_add" );
		this.setAttributeIntern( "title",
				MultivariantString.getString( "Schedule: action properties",
						Collections.singletonMap( "ru", "Планирование: свойства задания" ) ) );
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) throws Exception {
		if (command == FormActionPropertiesCommand.CMD_CREATE) {
			final long scheduleDate = Convert.MapEntry.toLong( this.getData(), "scheduleDate", this.scheduleDate );
			final BaseObject parameters = this.getData().baseGet( "parameters", BaseObject.UNDEFINED );
			this.type.scriptCommandFormSubmit( this.scheduleActor, this.entry, parameters );
			final Action<?> result = new ActionBasic( this.type,
					this.scheduleId,
					scheduleDate,
					"*",
					this.scheduleActor,
					parameters );
			this.schedule.schedule( result );
			return null;
		}
		return super.getCommandResult( command, arguments );
	}
	
	@Override
	public ControlCommandset getCommands() {
		return Control.createOptionsSingleton( FormActionPropertiesCommand.CMD_CREATE );
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		return this.fieldset;
	}
}
