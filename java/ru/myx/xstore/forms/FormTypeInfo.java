/*
 * Created on 21.09.2004
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ru.myx.xstore.forms;

import java.util.Collection;
import java.util.Collections;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.types.Type;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.ControlForm;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.xstore.basics.Helper;

/**
 * @author myx
 * 
 *         Window - Preferences - Java - Code Style - Code Templates
 */
class FormTypeInfo extends AbstractForm<FormTypeInfo> {
	private static final BaseObject		STR_TITLE	= MultivariantString.getString( "Type information",
																Collections.singletonMap( "ru", "Информация о типе" ) );
	
	private static final ControlFieldset<?>	FIELDSET	= ControlFieldset
																.createFieldset()
																.addField( ControlFieldFactory
																		.createFieldString( "typeName",
																				"Type name",
																				null ).setConstant() )
																.addField( ControlFieldFactory
																		.createFieldString( "typeTitle",
																				"Type name",
																				null ).setConstant() )
																.addField( ControlFieldFactory
																		.createFieldString( "typeIcon",
																				"Type icon",
																				null ).setConstant() )
																.addField( ControlFieldFactory
																		.createFieldString( "typeImpl",
																				"Type class",
																				null ).setConstant() )
																.addField( ControlFieldFactory
																		.createFieldSet( "typeChildren",
																				"Allowed children",
																				null ).setConstant() )
																.addField( ControlFieldFactory
																		.createFieldSet( "typeParents",
																				"Allowed parents",
																				null ).setConstant() )
																.addField( ControlFieldFactory
																		.createFieldString( "typeDefVer",
																				"Versioning",
																				null ).setConstant() )
																.addField( Control.createFieldList( "typeListing",
																		"Listing fields",
																		null ).setConstant() )
																.addField( ControlFieldFactory
																		.createFieldSet( "typeEvalut",
																				"Evaluate fields",
																				null ).setConstant() )
																.addField( ControlFieldFactory
																		.createFieldSet( "typePublic",
																				"Public fields",
																				null ).setConstant() )
																.addField( ControlFieldFactory
																		.createFieldSet( "typeReplace", "Aliases", null )
																		.setConstant() );
	
	private static final ControlCommand<?>	CMD_RETURN	= Control.createCommand( "return",
																MultivariantString.getString( "Back...",
																		Collections.singletonMap( "ru", "Назад..." ) ) )
																.setCommandIcon( "command-prev" );
	
	private static final BaseObject getArrayOrNothing(final Collection<?> collection) {
		return collection == null
				? BaseObject.UNDEFINED
				: Base.forArray( collection.toArray() );
	}
	
	private final ControlForm<?>	form;
	
	private final ControlCommand<?>	command;
	
	/**
	 * @param type
	 * @param form
	 * @param command
	 */
	FormTypeInfo(final Type<?> type, final ControlForm<?> form, final ControlCommand<?> command) {
		this.form = form;
		this.command = command;
		this.setAttributeIntern( "id", "type_info" );
		this.setAttributeIntern( "title", FormTypeInfo.STR_TITLE );
		final BaseObject onClose = form.getAttributes().baseGet( "on_close", BaseObject.UNDEFINED );
		assert onClose != null : "NULL java value";
		if (onClose != BaseObject.UNDEFINED) {
			this.setAttributeIntern( "on_close", onClose );
		}
		this.recalculate();
		final BaseObject data = new BaseNativeObject()//
				.putAppend( "typeName", type.getKey() )//
				.putAppend( "typeTitle", type.getTitle() )//
				.putAppend( "typeIcon", type.getIcon() )//
				.putAppend( "typeImpl", type.getClass().getName() )//
				.putAppend( "typeDefVer", type.getDefaultVersioning() )//
				.putAppend( "typeListing", Helper.getContentListingFieldset( type ) )//
				.putAppend( "typeChildren", FormTypeInfo.getArrayOrNothing( type.getValidChildrenTypeNames() ) )//
				.putAppend( "typeParents", FormTypeInfo.getArrayOrNothing( type.getValidParentsTypeNames() ) )//
				.putAppend( "typeEvalut", FormTypeInfo.getArrayOrNothing( type.getFieldsEvaluable() ) )//
				.putAppend( "typePublic", FormTypeInfo.getArrayOrNothing( type.getFieldsPublic() ) )//
				.putAppend( "typeReplace", FormTypeInfo.getArrayOrNothing( type.getReplacements() ) )//
		;
		this.setData( data );
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) throws Exception {
		if (command == FormTypeInfo.CMD_RETURN) {
			return this.form;
		}
		return this.form.getCommandResult( command, arguments );
	}
	
	@Override
	public ControlCommandset getCommands() {
		final ControlCommandset result = Control.createOptions();
		result.add( this.command );
		result.add( FormTypeInfo.CMD_RETURN );
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		return FormTypeInfo.FIELDSET;
	}
}
