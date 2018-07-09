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
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.ControlLookupStatic;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.help.Convert;

/**
 * @author myx
 * 
 */
public final class FormTypeDeleteMore extends AbstractForm<FormTypeDeleteMore> {
	private final BaseEntry<?>				entry;
	
	private final Type<?>					type;
	
	final ControlFieldset<?>				fieldset;
	
	private static final BaseHostLookup	LOOKUP_DELETE_MORE_OPERATION	= new ControlLookupStatic()
																					.putAppend( "true",
																							MultivariantString
																									.getString( "Unlink locally (object will be deleted when there are no more links to it)",
																											Collections
																													.singletonMap( "ru",
																															"Удалить ссылку (объект будет удален полностью после удаления последней ссылки)" ) ) )
																					.putAppend( "false",
																							MultivariantString
																									.getString( "Delete an object in all locations",
																											Collections
																													.singletonMap( "ru",
																															"Удалить все ссылки на объект" ) ) );
	
	private static final ControlCommand<?>	COMMAND_DELETE					= Control
																					.createCommand( "delete",
																							MultivariantString
																									.getString( "Delete",
																											Collections
																													.singletonMap( "ru",
																															"Удалить" ) ) )
																					.setCommandPermission( "delete" )
																					.setCommandIcon( "command-delete" );
	
	/**
	 * @param type
	 * @param entry
	 */
	public FormTypeDeleteMore(final Type<?> type, final BaseEntry<?> entry) {
		this.entry = entry;
		this.type = type;
		final BaseObject form = new BaseNativeObject();
		type.scriptPrepareDelete( entry, form );
		this.fieldset = ControlFieldset
				.createFieldset()
				.addField( ControlFieldFactory
						.createFieldBoolean( "unlink",
								MultivariantString.getString( "Operation", Collections.singletonMap( "ru", "Операция" ) ),
								true ).setFieldType( "select" )
						.setAttribute( "lookup", FormTypeDeleteMore.LOOKUP_DELETE_MORE_OPERATION ) )
				.addField( ControlFieldFactory.createFieldBoolean( "soft",
						MultivariantString.getString( "Delete to recycle bin",
								Collections.singletonMap( "ru", "Удаление в корзину" ) ),
						true ) )
				.addField( ControlFieldFactory
						.createFieldMap( "form",
								MultivariantString.getString( "Additional parameters",
										Collections.singletonMap( "ru", "Дополнительные параметры" ) ),
								form ).setFieldVariant( "fieldset" )
						.setAttribute( "fieldset", type.getFieldsetDelete() ) );
		final BaseObject data = new BaseNativeObject()//
				.putAppend( "form", form )//
				.putAppend( "unlink", BaseObject.TRUE )//
				.putAppend( "soft", BaseObject.TRUE )//
		;
		this.setData( data );
		this.setAttributeIntern( "id", type.getKey() + "__delete" );
		this.setAttributeIntern( "title", Base.getString( this.fieldset.getAttributes(), "title", "Delete" ) );
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		if (command == FormTypeDeleteMore.COMMAND_DELETE) {
			final BaseObject data = this.getData();
			final BaseObject form = data.baseGet( "form", BaseObject.UNDEFINED );
			assert form != null : "NULL java object";
			this.type.scriptSubmitDelete( this.entry, form );
			final boolean unlink = Convert.MapEntry.toBoolean( data, "unlink", true );
			final boolean soft = Convert.MapEntry.toBoolean( data, "soft", true );
			if (unlink) {
				this.entry.createChange().unlink( soft );
			} else {
				this.entry.createChange().delete( soft );
			}
			return null;
		}
		throw new IllegalArgumentException( "Unknown command: " + command.getKey() );
	}
	
	@Override
	public ControlCommandset getCommands() {
		final ControlCommandset result = Control.createOptions();
		result.add( FormTypeDeleteMore.COMMAND_DELETE );
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		return this.fieldset;
	}
}
