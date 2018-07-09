/*
 * Created on 08.06.2004
 */
package ru.myx.xstore.forms;

import java.util.Collections;
import java.util.Set;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseChange;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.help.Convert;
import ru.myx.jdbc.lock.Locker;

/**
 * @author myx
 * 		
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
final class FormEntryChangeTemplateCleanup extends AbstractForm<FormEntryChangeTemplateCleanup> {
	
	private static final ControlFieldset<?> FIELDSET_SHORT = ControlFieldset.createFieldset().addField(
			ControlFieldFactory
					.createFieldBoolean(
							"delete",
							MultivariantString.getString("Remove extra fields from an object", Collections.singletonMap("ru", "Удалить лишние поля объекта")),
							true)
					.setFieldHint(
							MultivariantString.getString(
									"Object contains some number of fields not declared by an object type choosen",
									Collections.singletonMap("ru", "Объект содержит некоторое число полей не декларированных выбранным типом объекта"))));
									
	private static final ControlFieldset<?> FIELDSET_FULL = ControlFieldset.createFieldset().addField(
			ControlFieldFactory
					.createFieldBoolean(
							"delete",
							MultivariantString.getString("Remove extra fields from an object", Collections.singletonMap("ru", "Удалить лишние поля объекта")),
							true)
					.setFieldHint(
							MultivariantString.getString(
									"Object contains some number of fields not declared by an object type choosen",
									Collections.singletonMap("ru", "Объект содержит некоторое число полей не декларированных выбранным типом объекта"))))
			.addField(
					ControlFieldFactory.createFieldMap("fields", MultivariantString.getString("List of extra fields", Collections.singletonMap("ru", "Список лишних полей")), null)
							.setConstant());
							
	private final StorageImpl plugin;
	
	private final BaseChange change;
	
	private final Locker lock;
	
	private final boolean published;
	
	private final Set<String> extraFields;
	
	private final String template;
	
	private boolean more = false;
	
	private static final ControlCommand<?> CMD_CLOSE = Control.createCommand("close", " * ").setCommandIcon("command-close");
	
	private static final ControlCommand<?> CMD_NEXT = Control.createCommand("next", MultivariantString.getString("Next...", Collections.singletonMap("ru", "Далее...")))
			.setCommandPermission("modify").setCommandIcon("command-next");
			
	private static final ControlCommand<?> CMD_NEXT_PUBLISHED = Control.createCommand("next", MultivariantString.getString("Next...", Collections.singletonMap("ru", "Далее...")))
			.setCommandPermission("publish").setCommandIcon("command-next");
			
	private static final ControlCommand<?> CMD_FULL = Control.createCommand("full", MultivariantString.getString("Details...", Collections.singletonMap("ru", "Подробнее...")))
			.setCommandPermission("view").setCommandIcon("command-edit-more");
			
	FormEntryChangeTemplateCleanup(final StorageImpl plugin, final BaseChange change, final Locker lock, final Set<String> extraFields, final String template) {
		this.plugin = plugin;
		this.change = change;
		this.lock = lock;
		this.published = change.getState() > 0;
		this.extraFields = extraFields;
		this.template = template;
		
		this.setAttributeIntern("id", "choose_template");
		this.setAttributeIntern("title", MultivariantString.getString("Choose entry type: cleanup", Collections.singletonMap("ru", "Смена типа объекта: чистка")));
		
		if (lock != null) {
			this.setAttributeIntern("on_close", FormEntryChangeTemplateCleanup.CMD_CLOSE);
		}
		
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		
		if (command == FormEntryChangeTemplateCleanup.CMD_CLOSE) {
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		} else if (command == FormEntryChangeTemplateCleanup.CMD_FULL) {
			final BaseObject fields = new BaseNativeObject();
			for (final String key : this.extraFields) {
				fields.baseDefine(key, this.change.getData().baseGet(key, BaseObject.UNDEFINED));
			}
			final BaseObject data = new BaseNativeObject();
			data.baseDefineImportAllEnumerable(this.getData());
			data.baseDefine("fields", fields);
			this.setData(data);
			this.more = true;
			if (this.lock != null) {
				this.lock.lockUpdate();
			}
			return this;
		} else if (command == FormEntryChangeTemplateCleanup.CMD_NEXT || command == FormEntryChangeTemplateCleanup.CMD_NEXT_PUBLISHED) {
			final boolean delete = Convert.MapEntry.toBoolean(this.getData(), "delete", true);
			if (delete) {
				for (final String key : this.extraFields) {
					this.change.getData().baseDelete(key);
				}
				this.change.setTypeName(this.template);
			}
			return new FormEntryEdit(this.plugin, this.change, this.lock);
		} else {
			throw new IllegalArgumentException("Unknown command: " + command.getKey());
		}
	}
	
	@Override
	public ControlCommandset getCommands() {
		
		final ControlCommandset result = Control.createOptions();
		if (this.more) {
			result.add(this.published
				? FormEntryChangeTemplateCleanup.CMD_NEXT_PUBLISHED
				: FormEntryChangeTemplateCleanup.CMD_NEXT);
		} else {
			result.add(FormEntryChangeTemplateCleanup.CMD_FULL);
			result.add(this.published
				? FormEntryChangeTemplateCleanup.CMD_NEXT_PUBLISHED
				: FormEntryChangeTemplateCleanup.CMD_NEXT);
		}
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		
		return this.more
			? FormEntryChangeTemplateCleanup.FIELDSET_FULL
			: FormEntryChangeTemplateCleanup.FIELDSET_SHORT;
	}
}
