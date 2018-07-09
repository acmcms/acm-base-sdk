/*
 * Created on 10.10.2004
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ru.myx.xstore.forms;

import java.util.Collections;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.know.Server;
import ru.myx.ae1.messaging.Message;
import ru.myx.ae1.storage.BaseChange;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.BaseVersion;
import ru.myx.ae1.types.Type;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.exec.Exec;
import ru.myx.jdbc.lock.Locker;
import ru.myx.xstore.basics.Helper;

/**
 * @author myx
 * 		
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class FormVisaVersion extends AbstractForm<FormVisaVersion> {
	
	private static final BaseObject STR_OWNER_FIELD = MultivariantString.getString("Owner", Collections.singletonMap("ru", "Пользователь"));
	
	private static final BaseObject STR_VERSION_FIELD = MultivariantString.getString("Comment", Collections.singletonMap("ru", "Комментарий"));
	
	private static final BaseObject STR_DATE_FIELD = MultivariantString.getString("Date", Collections.singletonMap("ru", "Дата"));
	
	private static final ControlFieldset<?> FIELDSET_VISA = ControlFieldset.createFieldset()
			.addField(ControlFieldFactory.createFieldOwner("$owner", FormVisaVersion.STR_OWNER_FIELD).setConstant())
			.addField(ControlFieldFactory.createFieldString("$comment", FormVisaVersion.STR_VERSION_FIELD, "").setConstant())
			.addField(ControlFieldFactory.createFieldDate("$modified", FormVisaVersion.STR_DATE_FIELD, 0L).setConstant());
			
	private static final BaseObject STR_TITLE = MultivariantString.getString("Version visa", Collections.singletonMap("ru", "Визирование версии"));
	
	private final BaseEntry<?> entry;
	
	private final BaseChange change;
	
	private final Type<?> type;
	
	private final Message message;
	
	private final Locker lock;
	
	private final ControlFieldset<?> fieldset;
	
	private static final ControlCommand<?> CMD_CLOSE = Control.createCommand("close", " * ").setCommandIcon("command-close");
	
	private static final ControlCommand<?> CMD_EDIT = Control.createCommand("edit", MultivariantString.getString("Edit", Collections.singletonMap("ru", "Редактировать")))
			.setCommandPermission("publish").setCommandIcon("command-edit");
			
	private static final ControlCommand<?> CMD_VISA = Control.createCommand("visa", MultivariantString.getString("Accept", Collections.singletonMap("ru", "Одобрить")))
			.setCommandPermission("publish").setCommandIcon("command-state-publish");
			
	private static final ControlCommand<?> CMD_DECLINE = Control.createCommand("decline", MultivariantString.getString("Decline", Collections.singletonMap("ru", "Отклонить")))
			.setCommandPermission("publish").setCommandIcon("command-delete");
			
	private static ControlFieldset<?> getFieldsetIntern(final Server server, final String template) {
		
		if (template.length() == 0) {
			return Helper.getFieldsetEdit(null);
		}
		return Helper.getFieldsetEdit(server.getTypes().getType(template));
	}
	
	/**
	 * @param entry
	 * @param message
	 * @param lock
	 */
	public FormVisaVersion(final BaseEntry<?> entry, final Message message, final Locker lock) {
		this.entry = entry;
		this.change = entry.createChange();
		final Server server = Context.getServer(Exec.currentProcess());
		this.fieldset = ControlFieldset
				.createFieldset(FormVisaVersion.FIELDSET_VISA, ControlFieldset.createFieldsetConstant(FormVisaVersion.getFieldsetIntern(server, entry.getTypeName())));
		this.fieldset.setAttribute("constant", true);
		this.type = server.getTypes().getType(this.change.getTypeName());
		this.message = message;
		this.lock = lock;
		final BaseObject data = new BaseNativeObject()//
				.putAppend("$owner", entry.getData().baseGet("$owner", BaseObject.UNDEFINED))//
				.putAppend("$modified", Base.forDateMillis(entry.getModified()))//
				;
		{
			final BaseVersion[] versions = entry.getVersions();
			if (versions != null) {
				for (int i = versions.length - 1; i >= 0; --i) {
					if (this.change.getVersionId().equals(versions[i].getGuid())) {
						data.baseDefine("$comment", versions[i].getComment());
						data.baseDefine("$owner", versions[i].getOwner());
						data.baseDefine("$modified", Base.forDateMillis(versions[i].getDate()));
						break;
					}
				}
			}
		}
		data.baseDefineImportAllEnumerable(this.change.getData());
		data.baseDefine("$key", this.change.getKey());
		data.baseDefine("$title", this.change.getTitle());
		data.baseDefine("$state", this.change.getState());
		data.baseDefine("$folder", this.change.isFolder());
		data.baseDefine("$type", this.change.getTypeName());
		data.baseDefine("$created", Base.forDateMillis(this.change.getCreated()));
		final BaseObject formData = this.change.getVersionData();
		if (formData == null) {
			if (this.type != null) {
				this.type.scriptPrepareModify(entry, this.change, data);
			}
		} else {
			data.baseDefineImportAllEnumerable(formData);
		}
		this.setData(data);
		this.setAttributeIntern("id", "visa");
		this.setAttributeIntern("title", FormVisaVersion.STR_TITLE);
		this.setAttributeIntern("path", entry.getLocationControl());
		if (lock != null) {
			this.setAttributeIntern("on_close", FormVisaVersion.CMD_CLOSE);
		}
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		
		if (command == FormVisaVersion.CMD_EDIT) {
			if (this.lock != null) {
				this.lock.lockUpdate();
			}
			this.message.deleteAll();
			return new FormEntryEdit(this.entry.getStorageImpl(), this.change, this.lock);
		} else if (command == FormVisaVersion.CMD_VISA) {
			this.change.setCommitActive();
			this.change.commit();
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			this.message.createReply("STORAGE_VISA_ACCEPTED", null).commit();
			this.message.deleteAll();
			return null;
		} else if (command == FormVisaVersion.CMD_DECLINE) {
			return new FormVisaDecline(this.message, this.lock);
		} else if (command == FormVisaVersion.CMD_CLOSE) {
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		} else {
			throw new IllegalArgumentException("Unknown command: " + command.getKey());
		}
	}
	
	@Override
	public ControlCommandset getCommands() {
		
		final ControlCommandset result = Control.createOptions();
		result.add(FormVisaVersion.CMD_EDIT);
		result.add(FormVisaVersion.CMD_VISA);
		result.add(FormVisaVersion.CMD_DECLINE);
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		
		return this.fieldset;
	}
}
