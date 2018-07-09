/*
 * Created on 13.04.2004
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ru.myx.xstore.forms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseChange;
import ru.myx.ae1.storage.BaseHistory;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BasePrimitive;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.jdbc.lock.Locker;

/**
 * @author myx
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FormEntryHistory extends AbstractForm<FormEntryHistory> {
	
	
	static final BaseObject STR_CURRENT_VERSION = MultivariantString.getString("Edit current version", Collections.singletonMap("ru", "Редактировать текущую версию"));
	
	private static final ControlCommand<?> CMD_CLOSE = Control.createCommand("close", " * ").setCommandIcon("command-close");
	
	private static final ControlCommand<?> CMD_EDIT = Control.createCommand("edit", MultivariantString.getString("Next...", Collections.singletonMap("ru", "Далее...")))
			.setCommandPermission("modify").setCommandIcon("command-next").setGlobal(true);
	
	private static final ControlCommand<?> CMD_EDIT_PUBLISHED = Control.createCommand("edit", MultivariantString.getString("Next...", Collections.singletonMap("ru", "Далее...")))
			.setCommandPermission("publish").setCommandIcon("command-next").setGlobal(true);
	
	private final StorageImpl plugin;
	
	private final BaseChange change;
	
	private final Locker lock;
	
	private final ControlFieldset<?> fieldset;
	
	private final boolean published;
	
	/**
	 * @param plugin
	 * @param change
	 * @param lock
	 */
	public FormEntryHistory(final StorageImpl plugin, final BaseChange change, final Locker lock) {
		this.plugin = plugin;
		this.change = change;
		this.lock = lock;
		final BaseHistory[] history = change.getHistory();
		final BaseHostLookup historyLookup = new BaseHostLookup() {
			
			
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			@Override
			public BaseObject baseGetLookupValue(final BaseObject key) {
				
				
				final String string = key.baseToJavaString();
				if (string.equals("*")) {
					return FormEntryHistory.STR_CURRENT_VERSION;
				}
				if (history != null) {
					for (final BaseHistory element : history) {
						if (string.equals(element.getGuid())) {
							return Base.forString('[' + this.sdf.format(new Date(element.getDate())) + "] " + element.getTitle());
						}
					}
				}
				return null;
			}
			
			@Override
			public boolean baseHasKeysOwn() {
				
				
				return true;
			}
			
			@Override
			public Iterator<String> baseKeysOwn() {
				
				
				final List<String> keys = new ArrayList<>();
				keys.add("*");
				if (history != null) {
					for (final BaseHistory element : history) {
						keys.add(element.getGuid());
					}
				}
				return keys.iterator();
			}
			
			@Override
			public Iterator<? extends BasePrimitive<?>> baseKeysOwnPrimitive() {
				
				
				return Base.iteratorPrimitiveSafe(this.baseKeysOwn());
			}
			
			@Override
			public String toString() {
				
				
				return "[Lookup EntryHistory]";
			}
		};
		this.fieldset = ControlFieldset.createFieldset().addField(
				ControlFieldFactory.createFieldString("history", MultivariantString.getString("Change history", Collections.singletonMap("ru", "История изменений")), "*")
						.setFieldType("select").setFieldVariant("bigselect").setAttribute("lookup", historyLookup));
		this.published = change.getState() > 0;
		this.setAttributeIntern("id", "entry_history");
		this.setAttributeIntern("title", MultivariantString.getString("Object change history", Collections.singletonMap("ru", "История изменений объекта")));
		this.setAttributeIntern("path", change.getLocationControl());
		if (lock != null) {
			this.setAttributeIntern("on_close", FormEntryHistory.CMD_CLOSE);
		}
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		
		
		if (command == FormEntryHistory.CMD_CLOSE) {
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		} else if (command == FormEntryHistory.CMD_EDIT || command == FormEntryHistory.CMD_EDIT_PUBLISHED) {
			final String historyId = Base.getString(this.getData(), "history", "*").trim();
			final BaseChange change = "*".equals(historyId)
				? this.change
				: this.change.getHistorySnapshot(historyId);
			return new FormEntryEdit(this.plugin, change, this.lock);
		} else {
			throw new IllegalArgumentException("Unknown command: " + command.getKey());
		}
	}
	
	@Override
	public ControlCommandset getCommands() {
		
		
		return this.published
			? Control.createOptionsSingleton(FormEntryHistory.CMD_EDIT_PUBLISHED)
			: Control.createOptionsSingleton(FormEntryHistory.CMD_EDIT);
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		
		
		return this.fieldset;
	}
}
