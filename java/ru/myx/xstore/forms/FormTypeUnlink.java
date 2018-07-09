/*
 * Created on 26.01.2006
 */
package ru.myx.xstore.forms;

import ru.myx.ae1.control.Control;
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
public final class FormTypeUnlink extends AbstractForm<FormTypeUnlink> {
	private final BaseEntry<?>				entry;
	
	private final Type<?>					type;
	
	final ControlFieldset<?>				fieldset;
	
	private static final ControlCommand<?>	UNLINK	= Control.createCommand( "unlink", "Unlink" )
															.setCommandPermission( "delete" )
															.setCommandIcon( "command-delete" );
	
	/**
	 * @param type
	 * @param entry
	 */
	public FormTypeUnlink(final Type<?> type, final BaseEntry<?> entry) {
		this.entry = entry;
		this.type = type;
		this.fieldset = type.getFieldsetDelete();
		this.setAttributeIntern( "id", type.getKey() + "__unlink" );
		this.setAttributeIntern( "title", Base.getString( this.fieldset.getAttributes(), "title", "Unlink" ) );
		this.recalculate();
		final BaseObject data = new BaseNativeObject();
		type.scriptPrepareDelete( entry, data );
		this.setData( data );
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		if (command == FormTypeUnlink.UNLINK) {
			this.type.scriptSubmitDelete( this.entry, this.getData() );
			this.entry.createChange().unlink( true );
			return null;
		}
		throw new IllegalArgumentException( "Unknown command: " + command.getKey() );
	}
	
	@Override
	public ControlCommandset getCommands() {
		final ControlCommandset result = Control.createOptions();
		result.add( FormTypeUnlink.UNLINK );
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		return this.fieldset;
	}
}
