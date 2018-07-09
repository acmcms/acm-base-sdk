/*
 * Created on 26.01.2006
 */
package ru.myx.xstore.forms;

import java.util.Collections;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.types.Type;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.fieldset.ControlFieldset;

/**
 * @author myx
 * 
 */
public final class FormTypeDelete extends AbstractForm<FormTypeDelete> {
	private final BaseEntry<?>				entry;
	
	private final Type<?>					type;
	
	final ControlFieldset<?>				fieldset;
	
	private static final ControlCommand<?>	COMMAND_DELETE	= Control
																	.createCommand( "delete",
																			MultivariantString.getString( "Delete",
																					Collections.singletonMap( "ru",
																							"УДалить" ) ) )
																	.setCommandPermission( "delete" )
																	.setCommandIcon( "command-delete" )
																	.setGlobal( true );
	
	/**
	 * @param type
	 * @param entry
	 */
	public FormTypeDelete(final Type<?> type, final BaseEntry<?> entry) {
		this.entry = entry;
		this.type = type;
		this.fieldset = type.getFieldsetDelete();
		this.setAttributeIntern( "id", type.getKey() + "__delete" );
		this.setAttributeIntern( "title", Base.getString( this.fieldset.getAttributes(), "title", "Delete" ) );
		this.recalculate();
		final BaseObject data = new BaseNativeObject();
		type.scriptPrepareDelete( entry, data );
		this.setData( data );
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		if (command == FormTypeDelete.COMMAND_DELETE) {
			this.type.scriptSubmitDelete( this.entry, this.getData() );
			this.entry.createChange().delete();
			return null;
		}
		throw new IllegalArgumentException( "Unknown command: " + command.getKey() );
	}
	
	@Override
	public ControlCommandset getCommands() {
		final ControlCommandset result = Control.createOptions();
		result.add( FormTypeDelete.COMMAND_DELETE );
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		return this.fieldset;
	}
}
