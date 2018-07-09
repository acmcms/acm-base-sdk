/*
 * Created on 16.04.2004
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ru.myx.xstore.forms;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseChange;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.StorageImpl;
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
import ru.myx.ae3.help.Convert;
import ru.myx.ae3.help.Create;
import ru.myx.jdbc.lock.Locker;
import ru.myx.xstore.basics.Helper;

/**
 * @author myx
 * 		
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FormEntryInternalStructure extends AbstractForm<FormEntryInternalStructure> {
	
	private static final ControlFieldset<?> FIELDSET = ControlFieldset.createFieldset("internal_structure")
			.addField(ControlFieldFactory.createFieldMap("data", MultivariantString.getString("Data", Collections.singletonMap("ru", "Данные")), null));
			
	private final StorageImpl plugin;
	
	private BaseChange change;
	
	private final Locker lock;
	
	private final boolean published;
	
	private final boolean versioning;
	
	private final Type<?> type;
	
	private final ControlFieldset<?> fieldset;
	
	private static final ControlCommand<?> CMD_CLOSE = Control.createCommand("close", " * ").setCommandIcon("command-close");
	
	private static final ControlCommand<?> CMD_SAVE = Control.createCommand("save", " OK ").setCommandPermission("modify").setCommandIcon("command-save").setGlobal(true);
	
	private static final ControlCommand<?> CMD_APPLY = Control.createCommand("apply", MultivariantString.getString("Apply", Collections.singletonMap("ru", "Применить")))
			.setCommandPermission("modify").setCommandIcon("command-apply").setGlobal(true);
			
	private static final ControlCommand<?> CMD_SAVE_PUBLISHED = Control.createCommand("save", " OK ").setCommandPermission("publish").setCommandIcon("command-save")
			.setGlobal(true);
			
	private static final ControlCommand<?> CMD_APPLY_PUBLISHED = Control.createCommand("apply", MultivariantString.getString("Apply", Collections.singletonMap("ru", "Применить")))
			.setCommandPermission("publish").setCommandIcon("command-apply").setGlobal(true);
			
	/**
	 * @param plugin
	 * @param change
	 * @param lock
	 */
	public FormEntryInternalStructure(final StorageImpl plugin, final BaseChange change, final Locker lock) {
		this.plugin = plugin;
		this.change = change;
		this.lock = lock;
		if (lock != null) {
			this.setAttributeIntern("on_close", FormEntryInternalStructure.CMD_CLOSE);
		}
		this.setAttributeIntern("id", "internal_structure");
		this.setAttributeIntern("title", MultivariantString.getString("Entry internal structure", Collections.singletonMap("ru", "Структура данных объекта")));
		this.published = change.getState() > 0;
		this.setAttributeIntern("path", change.getLocationControl());
		this.type = Context.getServer(Exec.currentProcess()).getTypes().getType(change.getTypeName());
		final BaseObject data = new BaseNativeObject("data", change.getData());
		this.setData(data);
		this.versioning = plugin.areObjectVersionsSupported() && change.getVersioning();
		if (this.versioning) {
			final ControlFieldset<?> versioningFieldset = ControlFieldset.createFieldset().addField(FormEntryVersions.getVersionControlField(change));
			this.fieldset = ControlFieldset.createFieldset(versioningFieldset, FormEntryInternalStructure.FIELDSET);
		} else {
			this.fieldset = FormEntryInternalStructure.FIELDSET;
		}
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		
		if (command == FormEntryInternalStructure.CMD_CLOSE) {
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		}
		if (command == FormEntryInternalStructure.CMD_SAVE || command == FormEntryInternalStructure.CMD_SAVE_PUBLISHED || command == FormEntryInternalStructure.CMD_APPLY
				|| command == FormEntryInternalStructure.CMD_APPLY_PUBLISHED) {
			final BaseChange change = this.change;
			final BaseObject changeData = change.getData();
			if (this.versioning) {
				final Set<String> systemFields = Create.tempSet();
				systemFields.addAll(Helper.getFieldsetEdit(null).innerFields());
				systemFields.addAll(Helper.getFieldsetDefault().innerFields());
				final BaseObject versionData = new BaseNativeObject();
				for (final Iterator<String> iterator = Base.keys(changeData); iterator.hasNext();) {
					final String key = iterator.next();
					if (!(key.length() > 0 && key.charAt(0) == '$') && !systemFields.contains(key)) {
						versionData.baseDefine(key, changeData.baseGet(key, BaseObject.UNDEFINED));
					}
				}
				change.setVersionData(versionData);
				final BaseObject versioning = this.getData().baseGet("$versioning", BaseObject.UNDEFINED);
				assert versioning != null : "NULL java value";
				final String comment = Base.getString(versioning, "$comment", "-");
				final int activation = Convert.MapEntry.toInt(versioning, "$activate", FormEntryVersions.VA_DRAFT);
				switch (activation) {
					case FormEntryVersions.VA_DRAFT : {
						change.setVersionComment(comment);
					}
						break;
					case FormEntryVersions.VA_VISA : {
						change.setVersionComment(comment);
					}
						break;
					case FormEntryVersions.VA_ACTIVATE : {
						change.setVersionComment(comment);
						change.setCommitActive();
						this.type.scriptSubmitModify(this.plugin.getStorage().getByGuidClean(change.getGuid(), change.getTypeName()), change, changeData);
					}
						break;
					case FormEntryVersions.VA_DISABLE : {
						change.setVersioning(false);
						change.setCommitActive();
						this.type.scriptSubmitModify(this.plugin.getStorage().getByGuidClean(change.getGuid(), change.getTypeName()), change, changeData);
					}
						break;
					default : {
						throw new IllegalArgumentException("Unknown activation type!");
					}
				}
			} else {
				this.type.scriptSubmitModify(this.plugin.getStorage().getByGuidClean(change.getGuid(), change.getTypeName()), change, changeData);
			}
			change.commit();
			if (command == FormEntryInternalStructure.CMD_APPLY || command == FormEntryInternalStructure.CMD_APPLY_PUBLISHED) {
				if (this.lock != null) {
					this.lock.lockUpdate();
				}
				final BaseEntry<?> entry = this.plugin.getInterface().getByGuid(this.change.getGuid());
				assert entry != null : "Should not be NULL";
				this.change = entry.createChange();
				return this;
			}
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		}
		throw new IllegalArgumentException("Unknown command: " + command.getKey());
	}
	
	@Override
	public ControlCommandset getCommands() {
		
		final ControlCommandset result = Control.createOptions();
		if (this.published) {
			result.add(FormEntryInternalStructure.CMD_SAVE_PUBLISHED);
			result.add(FormEntryInternalStructure.CMD_APPLY_PUBLISHED);
		} else {
			result.add(FormEntryInternalStructure.CMD_SAVE);
			result.add(FormEntryInternalStructure.CMD_APPLY);
		}
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		
		return this.fieldset;
	}
}
