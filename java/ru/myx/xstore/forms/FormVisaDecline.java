/*
 * Created on 11.10.2004
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ru.myx.xstore.forms;

import java.util.Collections;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.messaging.Message;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
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
class FormVisaDecline extends AbstractForm<FormVisaDecline> {
	
	private static final BaseObject		STR_COMMENT			= MultivariantString
																		.getString( "Comment", Collections
																				.singletonMap( "ru", "Комментарий" ) );
	
	private static final ControlFieldset<?>	DECLINE_FIELDSET	= ControlFieldset.createFieldset().addField( ControlFieldFactory
																		.createFieldString( "comment",
																				FormVisaDecline.STR_COMMENT,
																				"" ).setFieldType( "text" ) );
	
	private static final BaseObject		STR_TITLE			= MultivariantString.getString( "Visa: decline",
																		Collections.singletonMap( "ru",
																				"Визирование: отклонение" ) );
	
	private final Message					message;
	
	private final Locker					lock;
	
	private static final ControlCommand<?>	CMD_CLOSE			= Control.createCommand( "close", " * " )
																		.setCommandIcon( "command-close" );
	
	private static final ControlCommand<?>	CMD_DECLINE			= Control
																		.createCommand( "decline",
																				MultivariantString
																						.getString( "Decline",
																								Collections
																										.singletonMap( "ru",
																												"Отклонить" ) ) )
																		.setCommandPermission( "publish" )
																		.setCommandIcon( "command-delete" );
	
	FormVisaDecline(final Message message, final Locker lock) {
		this.message = message;
		this.lock = lock;
		
		this.setAttributeIntern( "id", "visa_decline" );
		this.setAttributeIntern( "title", FormVisaDecline.STR_TITLE );
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject parameters) {
		if (command == FormVisaDecline.CMD_CLOSE) {
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		} else if (command == FormVisaDecline.CMD_DECLINE) {
			this.message.createReply( "STORAGE_VISA_DECLINED", this.getData() ).commit();
			this.message.deleteAll();
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		} else {
			throw new IllegalArgumentException( "Unknown command: " + command.getKey() );
		}
	}
	
	@Override
	public ControlCommandset getCommands() {
		return Control.createOptionsSingleton( FormVisaDecline.CMD_DECLINE );
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		return FormVisaDecline.DECLINE_FIELDSET;
	}
	
}
