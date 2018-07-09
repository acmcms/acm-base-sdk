/*
 * Created on 26.01.2006
 */
package ru.myx.xstore.forms;

import java.util.Collections;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseEntry;
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
 */
public class FormEntryUnlinkConfirmation extends AbstractForm<FormEntryUnlinkConfirmation> {
	private static final ControlCommand<?>	UNLINK			= Control.createCommand( "unlink", "Unlink" )
																	.setCommandPermission( "delete" )
																	.setCommandIcon( "command-delete" );
	
	private final BaseEntry<?>				entry;
	
	private static final ControlFieldset<?>	FIELDSET_DELETE	= ControlFieldset
																	.createFieldset( "confirmation" )
																	.addField( ControlFieldFactory.createFieldBoolean( "confirmation",
																			MultivariantString.getString( "yes, i do.",
																					Collections.singletonMap( "ru",
																							"однозначно" ) ),
																			false ) );
	
	/**
	 * @param entry
	 */
	public FormEntryUnlinkConfirmation(final BaseEntry<?> entry) {
		this.entry = entry;
		this.setAttributeIntern( "id", "confirmation" );
		this.setAttributeIntern( "title",
				MultivariantString.getString( "Do you really want to unlink this entry?",
						Collections.singletonMap( "ru", "Вы действительно хотите удалить эту ссылку на объект?" ) ) );
		this.setAttributeIntern( "path", entry.getLocationControl() );
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		if (command == FormEntryUnlinkConfirmation.UNLINK) {
			if (Convert.MapEntry.toBoolean( this.getData(), "confirmation", false )) {
				this.entry.createChange().unlink( true );
				return null;
			}
			return "Action cancelled.";
		}
		throw new IllegalArgumentException( "Unknown command: " + command.getKey() );
	}
	
	@Override
	public ControlCommandset getCommands() {
		final ControlCommandset result = Control.createOptions();
		result.add( FormEntryUnlinkConfirmation.UNLINK );
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		return FormEntryUnlinkConfirmation.FIELDSET_DELETE;
	}
}
