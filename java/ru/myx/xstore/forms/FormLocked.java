/*
 * Created on 21.08.2004
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ru.myx.xstore.forms;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.ControlForm;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.jdbc.lock.Locker;

/**
 * @author myx
 * 
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class FormLocked extends AbstractForm<FormLocked> {
	private static final BaseObject		STR_TITLE	= MultivariantString.getString( "Object locked!",
																Collections.singletonMap( "ru", "Объект занят!" ) );
	
	private static final ControlFieldset<?>	FIELDSET	= ControlFieldset
																.createFieldset()
																.addField( ControlFieldFactory
																		.createFieldOwner( "owner",
																				MultivariantString.getString( "User",
																						Collections.singletonMap( "ru",
																								"Пользователь" ) ) )
																		.setConstant() )
																.addField( ControlFieldFactory
																		.createFieldString( "date",
																				MultivariantString.getString( "Date",
																						Collections.singletonMap( "ru",
																								"Дата" ) ),
																				"" ).setConstant() )
																.addField( ControlFieldFactory
																		.createFieldString( "expires",
																				MultivariantString
																						.getString( "Expires",
																								Collections
																										.singletonMap( "ru",
																												"Истекает" ) ),
																				"" ).setConstant() );
	
	private static final ControlCommand<?>	CMD_IGNORE	= Control
																.createCommand( "ignore",
																		MultivariantString.getString( "Ignore",
																				Collections.singletonMap( "ru",
																						"Игнорировать" ) ) )
																.setCommandIcon( "command-next" );
	
	private final ControlForm<?>			form;
	
	/**
	 * @param lock
	 * @param form
	 */
	public FormLocked(final Locker lock, final ControlForm<?> form) {
		this.form = form;
		
		final SimpleDateFormat formatter = new SimpleDateFormat( "yyyy.MM.dd HH:mm:ss" );
		final BaseObject data = new BaseNativeObject()//
				.putAppend( "owner", lock.getLockId() )//
				.putAppend( "date", formatter.format( new Date( lock.getLockDate() ) ) )//
				.putAppend( "expires", formatter.format( new Date( lock.getLockExpiration() ) ) )//
		;
		
		this.setData( data );
		
		this.setAttributeIntern( "id", "lock_failed" );
		this.setAttributeIntern( "title", FormLocked.STR_TITLE );
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		if (command == FormLocked.CMD_IGNORE) {
			return this.form;
		}
		throw new IllegalArgumentException( "Unknown command: " + command.getKey() );
	}
	
	@Override
	public ControlCommandset getCommands() {
		return Control.createOptionsSingleton( FormLocked.CMD_IGNORE );
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		return FormLocked.FIELDSET;
	}
	
}
