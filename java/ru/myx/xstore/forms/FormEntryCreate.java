/*
 * Created on 13.04.2004
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ru.myx.xstore.forms;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.know.Server;
import ru.myx.ae1.storage.BaseChange;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae1.types.Type;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.help.Convert;
import ru.myx.jdbc.lock.Locker;
import ru.myx.xstore.basics.Helper;

/**
 * @author myx
 * 		
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public final class FormEntryCreate extends AbstractForm<FormEntryCreate> {
	
	private static final String[] SYSTEM_FIELDS = {
			"$key", "$title", "$folder", "$state", "$created",
	};
	
	private static final ControlCommand<?> CMD_PREV = Control.createCommand("prev", MultivariantString.getString("Prev...", Collections.singletonMap("ru", "Назад...")))
			.setCommandPermission("create").setCommandIcon("command-prev");
			
	private static final ControlCommand<?> CMD_CREATE = Control.createCommand("create", MultivariantString.getString("Create", Collections.singletonMap("ru", "Создать")))
			.setCommandPermission("create").setCommandIcon("command-create");
			
	private static final ControlCommand<?> CMD_CREATE_EDIT = Control
			.createCommand("create_edit", MultivariantString.getString("Create & Edit", Collections.singletonMap("ru", "Создать и редактировать...")))
			.setCommandPermission("create").setCommandIcon("command-create-edit");
			
	private static final ControlCommand<?> CMD_CREATE_MORE = Control
			.createCommand(
					"create_more", //
					MultivariantString.getString(
							"Create more...", //
							Collections.singletonMap(
									"ru", //
									"Создать еще..."))) //
			.setCommandPermission("create") //
			.setCommandIcon("command-create-more") //
			;
			
	private static ControlFieldset<?> getFieldsetIntern(final Server server, final String template) {
		
		return template.length() == 0
			? Helper.getFieldsetCreate(null)
			: Helper.getFieldsetCreate(server.getTypes().getType(template));
	}
	
	private final StorageImpl plugin;
	
	private BaseChange change;
	
	private final boolean local;
	
	private final boolean versioning;
	
	private final String typeName;
	
	private final ControlFieldset<?> fieldset;
	
	/**
	 * @param plugin
	 * @param change
	 * @param typeName
	 * @param local
	 * @param versioning
	 */
	public FormEntryCreate(final StorageImpl plugin, final BaseChange change, final String typeName, final boolean local, final boolean versioning) {
		this.plugin = plugin;
		this.change = change;
		
		this.local = local;
		this.typeName = typeName;
		this.versioning = versioning && plugin.areObjectVersionsSupported();
		
		final Server server = Context.getServer(Exec.currentProcess());
		if (this.versioning) {
			final ControlFieldset<?> versioningFieldset = ControlFieldset.createFieldset().addField(FormEntryVersions.getVersionControlField(change));
			this.fieldset = ControlFieldset.createFieldset(versioningFieldset, FormEntryCreate.getFieldsetIntern(server, this.typeName));
		} else {
			this.fieldset = FormEntryCreate.getFieldsetIntern(server, this.typeName);
		}
		this.setAttributeIntern("id", "create_entry");
		this.setAttributeIntern("title", MultivariantString.getString("Create an entry", Collections.singletonMap("ru", "Создание объекта")));
		this.setAttributeIntern("path", plugin.getStorage().getByGuid(change.getParentGuid()).getLocationControl());
		
		this.recalculate();
		
		final Type<?> type = server.getTypes().getType(this.typeName);
		this.setupChange(change, type);
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		
		final BaseObject data = this.getData();
		if (command == FormEntryCreate.CMD_CREATE || command == FormEntryCreate.CMD_CREATE_EDIT || command == FormEntryCreate.CMD_CREATE_MORE) {
			final Type<?> type = Context.getServer(Exec.currentProcess()).getTypes().getType(this.typeName);
			final Set<String> systemFields = new TreeSet<>();
			{
				for (final String fieldName : FormEntryCreate.SYSTEM_FIELDS) {
					systemFields.add(fieldName);
				}
				final ControlFieldset<?> typePropertiesCreate = Helper.getFieldsetCreateClean(type);
				if (typePropertiesCreate != null) {
					systemFields.addAll(typePropertiesCreate.innerFields());
				}
				final ControlFieldset<?> typeFieldset = type.getFieldsetLoad();
				if (typeFieldset != null) {
					systemFields.addAll(typeFieldset.innerFields());
				}
			}
			final String id = this.change.getGuid();
			for (final Iterator<String> iterator = Base.keys(data); iterator.hasNext();) {
				final String key = iterator.next();
				if (systemFields.contains(key)) {
					this.change.getData().baseDefine(key, data.baseGet(key, BaseObject.UNDEFINED));
				}
			}
			this.change.setTypeName(this.typeName);
			if (this.versioning) {
				final BaseObject versionData = new BaseNativeObject();
				for (final Iterator<String> iterator = Base.keys(data); iterator.hasNext();) {
					final String key = iterator.next();
					if (!(key.length() > 0 && key.charAt(0) == '$') && !systemFields.contains(key)) {
						versionData.baseDefine(key, data.baseGet(key, BaseObject.UNDEFINED));
					}
				}
				this.change.setVersionData(versionData);
				final BaseObject versioning = data.baseGet("$versioning", BaseObject.UNDEFINED);
				assert versioning != null : "NULL java value";
				final String comment = Base.getString(versioning, "$comment", "-");
				final int activation = Convert.MapEntry.toInt(versioning, "$activate", FormEntryVersions.VA_DRAFT);
				switch (activation) {
					case FormEntryVersions.VA_DRAFT : {
						this.change.setVersionComment(comment);
						this.change.commit();
					}
						break;
					case FormEntryVersions.VA_VISA : {
						this.change.setVersionComment(comment);
						this.change.commit();
						FormEntryVersions.createVisaMessage(this.change);
					}
						break;
					case FormEntryVersions.VA_ACTIVATE : {
						this.change.setVersionComment(comment);
						this.change.setCommitActive();
						type.scriptSubmitModify(this.plugin.getStorage().getByGuidClean(this.change.getGuid(), this.typeName), this.change, data);
						this.change.commit();
					}
						break;
					case FormEntryVersions.VA_DISABLE : {
						this.change.setVersioning(false);
						this.change.setCommitActive();
						type.scriptSubmitModify(this.plugin.getStorage().getByGuidClean(this.change.getGuid(), this.typeName), this.change, data);
						this.change.commit();
					}
						break;
					default : {
						throw new IllegalArgumentException("Unknown activation type!");
					}
				}
			} else {
				type.scriptSubmitCreate(this.change, data);
				this.change.commit();
			}
			Context.getServer(Exec.currentProcess()).logQuickTaskUsage(
					"XDS_COMMAND_CREATE_OBJECT", //
					new BaseNativeObject() //
							.putAppend("storage", this.plugin.getMnemonicName()) //
							.putAppend("sectionId", this.change.getParentGuid()) //
							.putAppend("template", this.typeName) //
			);
			if (command == FormEntryCreate.CMD_CREATE) {
				return null;
			}
			if (command == FormEntryCreate.CMD_CREATE_EDIT) {
				try {
					final Locker lock = this.plugin.getLocker().createLock(id, 0);
					return new FormEntryEdit(this.plugin, this.plugin.getStorage().getByGuid(id).createChange(), lock);
				} catch (final Exception e) {
					return new FormEntryEdit(this.plugin, this.plugin.getStorage().getByGuid(id).createChange(), null);
				}
			}
			if (command == FormEntryCreate.CMD_CREATE_MORE) {
				this.change = this.plugin.getStorage().getByGuid(this.change.getParentGuid()).createChild();
				this.setupChange(this.change, type);
				return this;
			}
			{
				return null;
			}
		} else if (command == FormEntryCreate.CMD_PREV) {
			return new FormEntryCreateChooseTemplate(this.plugin, this.change.getParentGuid(), this.typeName, data);
		} else {
			throw new IllegalArgumentException("Unknown command: " + command.getKey());
		}
	}
	
	@Override
	public ControlCommandset getCommands() {
		
		final ControlCommandset result = Control.createOptions();
		result.add(FormEntryCreate.CMD_CREATE);
		result.add(FormEntryCreate.CMD_CREATE_EDIT);
		result.add(FormEntryCreate.CMD_CREATE_MORE);
		result.add(FormEntryCreate.CMD_PREV);
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		
		return this.fieldset;
	}
	
	/**
	 * @param change
	 * @param versioning
	 * @param type
	 */
	private void setupChange(final BaseChange change, final Type<?> type) {
		
		change.setCreateLocal(this.local);
		
		change.setTypeName(this.typeName);
		
		final BaseObject changeData = change.getData();
		if (Base.getInt(changeData, "$state", -1) < 0) {
			change.setState(type.getDefaultState());
		}
		if (Base.getInt(changeData, "$folder", -1) < 0) {
			change.setFolder(type.getDefaultFolder());
		}
		final BaseObject data = new BaseNativeObject();
		data.baseDefineImportAllEnumerable(changeData);
		type.scriptPrepareCreate(change, data);
		this.setData(data);
		
		change.setVersioning(this.versioning);
	}
}
