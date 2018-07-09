/*
 * Created on 26.01.2006
 */
package ru.myx.xstore.forms;

import java.util.Collections;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae3.base.BaseArray;
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
public class FormEntryUnlinkConfirmationMultiple extends AbstractForm<FormEntryUnlinkConfirmationMultiple> {
	
	private static final ControlCommand<?> UNLINK = Control.createCommand("unlink", MultivariantString.getString("Unlink", Collections.singletonMap("ru", "Удалить")))
			.setCommandPermission("delete").setCommandIcon("command-delete");
			
	private final StorageImpl parent;
	
	private final String sectionId;
	
	private final BaseArray keys;
	
	private static final ControlFieldset<?> FIELDSET_DELETE = ControlFieldset.createFieldset("confirmation")
			.addField(ControlFieldFactory.createFieldBoolean("confirmation", MultivariantString.getString("yes, i do.", Collections.singletonMap("ru", "однозначно")), false));
			
	/**
	 * @param parent
	 * @param sectionId
	 * @param keys
	 */
	public FormEntryUnlinkConfirmationMultiple(final StorageImpl parent, final String sectionId, final BaseArray keys) {
		this.parent = parent;
		this.sectionId = sectionId;
		this.keys = keys;
		this.setAttributeIntern("id", "confirmation");
		this.setAttributeIntern(
				"title",
				MultivariantString.getString(
						"Do you really want to delete these " + keys.length() + " link(s)?",
						Collections.singletonMap("ru", "Вы действительно хотите удалить эти " + keys.length() + " ссыл(ку/ок)?")));
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		
		if (command == FormEntryUnlinkConfirmationMultiple.UNLINK) {
			if (Convert.MapEntry.toBoolean(this.getData(), "confirmation", false)) {
				final int length = this.keys.length();
				for (int i = 0; i < length; ++i) {
					final String key = this.keys.baseGet(i, BaseObject.UNDEFINED).baseToJavaString();
					final BaseEntry<?> entry = this.parent.getStorage().getByGuid(this.sectionId).getChildByName(key);
					if (entry != null) {
						entry.createChange().unlink(true);
					}
				}
				return null;
			}
			return "Action cancelled.";
		}
		throw new IllegalArgumentException("Unknown command: " + command.getKey());
	}
	
	@Override
	public ControlCommandset getCommands() {
		
		final ControlCommandset result = Control.createOptions();
		result.add(FormEntryUnlinkConfirmationMultiple.UNLINK);
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		
		return FormEntryUnlinkConfirmationMultiple.FIELDSET_DELETE;
	}
}
