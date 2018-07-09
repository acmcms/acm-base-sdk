package ru.myx.xstore.basics;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ru.myx.ae1.access.Access;
import ru.myx.ae1.control.AbstractNode;
import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.ControlEntry;
import ru.myx.ae1.control.ControlNode;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseChange;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.ModuleInterface;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae1.types.Type;
import ru.myx.ae3.access.AccessPermissions;
import ru.myx.ae3.answer.ReplyAnswer;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseArray;
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.ControlBasic;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.help.Convert;
import ru.myx.ae3.help.Create;
import ru.myx.ae3.i3.Handler;
import ru.myx.ae3.serve.ServeRequest;
import ru.myx.jdbc.lock.Locker;
import ru.myx.xstore.forms.FormEntryChangeTemplate;
import ru.myx.xstore.forms.FormEntryCreateChooseTemplate;
import ru.myx.xstore.forms.FormEntryDeleteConfirmation;
import ru.myx.xstore.forms.FormEntryDeleteConfirmationMultiple;
import ru.myx.xstore.forms.FormEntryDeleteMore;
import ru.myx.xstore.forms.FormEntryDeleteMoreMultiple;
import ru.myx.xstore.forms.FormEntryEdit;
import ru.myx.xstore.forms.FormEntryHistory;
import ru.myx.xstore.forms.FormEntryInternalStructure;
import ru.myx.xstore.forms.FormEntryUnlinkConfirmation;
import ru.myx.xstore.forms.FormEntryUnlinkConfirmationMultiple;
import ru.myx.xstore.forms.FormEntryVersions;
import ru.myx.xstore.forms.FormLocked;
import ru.myx.xstore.forms.FormPaste;
import ru.myx.xstore.forms.FormSearch;
import ru.myx.xstore.forms.FormTypeDelete;
import ru.myx.xstore.forms.FormTypeDeleteMore;
import ru.myx.xstore.forms.FormTypeUnlink;
import ru.myx.xstore.schedule.FormEntryEditSchedule;

/**
 * Title: Xml Document Management for WSM3 Description: Copyright: Copyright (c)
 * 
 * 2001
 * 
 * @author Alexander I. Kharitchev
 * @version 1.0
 */
public final class ControlNodeImpl extends AbstractNode {
	
	// ////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////
	private static final int FILTER_LOCK_COUNT = 256;
	
	private static final int FILTER_LOCK_MASK = ControlNodeImpl.FILTER_LOCK_COUNT - 1;
	
	private static final Object[] FILTER_LOCKS = new Object[ControlNodeImpl.FILTER_LOCK_COUNT];
	
	private static final ControlCommand<?> FORM_EDIT = Control.createCommand("form_edit", MultivariantString.getString("Properties", Collections.singletonMap("ru", "Свойства")))
			.setCommandPermission("view").setCommandIcon("command-edit");
			
	private static final ControlCommand<?> FORM_SCHEDULE = Control
			.createCommand("form_schedule", MultivariantString.getString("Schedule", Collections.singletonMap("ru", "Планирование"))).setCommandPermission("publish")
			.setCommandIcon("command-edit-publishing");
			
	private static final ControlCommand<?> FORM_STRUCTURE = Control
			.createCommand("form_internal", MultivariantString.getString("Internal structure", Collections.singletonMap("ru", "Структура данных"))).setCommandPermission("view")
			.setCommandIcon("command-edit-internal");
			
	private static final ControlCommand<?> CMD_CREATE = Control.createCommand("create", MultivariantString.getString("Create...", Collections.singletonMap("ru", "Создать...")))
			.setCommandPermission("create").setCommandIcon("command-create");
			
	private static final ControlCommand<?> CMD_CHANGE_TEMPLATE = Control
			.createCommand("change_template", MultivariantString.getString("Change type...", Collections.singletonMap("ru", "Сменить тип..."))).setCommandPermission("modify")
			.setCommandIcon("command-edit-change");
			
	private static final ControlCommand<?> CMD_CHANGE_TEMPLATE_PUBLISHED = Control
			.createCommand("change_template", MultivariantString.getString("Change type...", Collections.singletonMap("ru", "Сменить тип..."))).setCommandPermission("publish")
			.setCommandIcon("command-edit-change");
			
	private static final ControlCommand<?> CMD_VERSIONS = Control
			.createCommand("versions", MultivariantString.getString("Versions...", Collections.singletonMap("ru", "Версии..."))).setCommandPermission("modify")
			.setCommandIcon("command-edit-versions");
			
	private static final ControlCommand<?> CMD_VERSIONS_PUBLISHED = Control
			.createCommand("versions", MultivariantString.getString("Versions...", Collections.singletonMap("ru", "Версии..."))).setCommandPermission("publish")
			.setCommandIcon("command-edit-versions");
			
	private static final ControlCommand<?> CMD_HISTORY = Control.createCommand("history", MultivariantString.getString("History...", Collections.singletonMap("ru", "История...")))
			.setCommandPermission("modify").setCommandIcon("command-edit-history");
			
	private static final ControlCommand<?> CMD_HISTORY_PUBLISHED = Control
			.createCommand("history", MultivariantString.getString("History...", Collections.singletonMap("ru", "История..."))).setCommandPermission("publish")
			.setCommandIcon("command-edit-history");
			
	private static final ControlCommand<?> CMD_BROWSE = Control.createCommand("browse", MultivariantString.getString("Preview", Collections.singletonMap("ru", "Просмотр")))
			.setCommandIcon("command-browse");
			
	private static final ControlCommand<?> CMD_BROWSE_DRAFT = Control.createCommand("browse", MultivariantString.getString("Preview", Collections.singletonMap("ru", "Просмотр")))
			.setCommandPermission("execute_draft").setCommandIcon("command-browse");
			
	private static final ControlCommand<?> CMD_TOUCH = Control.createCommand("touch", MultivariantString.getString("Recalculate", Collections.singletonMap("ru", "Пересчитать")))
			.setCommandPermission("modify").setCommandIcon("command-reload");
			
	private static final ControlCommand<?> CMD_SEARCH = Control.createCommand("search", MultivariantString.getString("Search...", Collections.singletonMap("ru", "Поиск...")))
			.setCommandPermission("modify").setCommandIcon("command-search");
			
	private static final ControlCommand<?> CMD_PASTE = Control.createCommand("paste", MultivariantString.getString("Paste", Collections.singletonMap("ru", "Вставить")))
			.setCommandPermission("modify").setCommandIcon("command-paste");
			
	private static final ControlCommand<?> CMD_PASTE_PUBLISHED = Control.createCommand("paste", MultivariantString.getString("Paste", Collections.singletonMap("ru", "Вставить")))
			.setCommandPermission("publish").setCommandIcon("command-paste");
			
	private static final ControlCommand<?> CMD_CLEAR_ALL = Control
			.createCommand("clearlost", MultivariantString.getString("Clear all", Collections.singletonMap("ru", "Удалить все"))).setCommandPermission("publish")
			.setCommandIcon("command-delete");
			
	static {
		for (int i = ControlNodeImpl.FILTER_LOCK_MASK; i >= 0; --i) {
			ControlNodeImpl.FILTER_LOCKS[i] = new Object();
		}
	}
	
	private static final void unlinkRecursive(final BaseEntry<?> entry, final Set<String> guids, final boolean soft) {
		
		if (entry == null || !guids.add(entry.getGuid())) {
			return;
		}
		final List<ControlBasic<?>> children = entry.getChildren();
		if (children != null) {
			for (final ControlBasic<?> current : children) {
				ControlNodeImpl.unlinkRecursive((BaseEntry<?>) current, guids, soft);
			}
		}
		entry.createChange().unlink(soft);
	}
	
	private final StorageImpl parent;
	
	private final String innerId;
	
	private final boolean root;
	
	private final ControlNode<?> trashcanNode;
	
	private QueryHandler handler = null;
	
	/**
	 * @param parent
	 * @param innerId
	 */
	public ControlNodeImpl(final StorageImpl parent, final String innerId) {
		this(parent, innerId, false);
	}
	
	/**
	 * @param parent
	 * @param innerId
	 * @param root
	 */
	public ControlNodeImpl(final StorageImpl parent, final String innerId, final boolean root) {
		this.parent = parent;
		this.innerId = innerId;
		this.root = root;
		this.trashcanNode = root && parent.areSoftDeletionsSupported()
			? new ControlNodeRecycler(parent)
			: null;
	}
	
	@Override
	public AccessPermissions getCommandPermissions() {
		
		return Access.createPermissionsLocal()
				.addPermission(
						"execute_draft",
						MultivariantString.getString("Read/Execute draft object on site", Collections.singletonMap("ru", "Чтение/Выполнение неопубликованного объекта на сайте")))
				.addPermission("execute", MultivariantString.getString("Read/Execute object on site", Collections.singletonMap("ru", "Чтение/Выполнение объекта на сайте")), false)
				.addPermission("view", MultivariantString.getString("View objects structure and settings", Collections.singletonMap("ru", "Просмотр свойств и структуры объектов")))
				.addPermission("create", MultivariantString.getString("Create objects", Collections.singletonMap("ru", "Создавать объекты")))
				.addPermission("modify", MultivariantString.getString("Modify objects and this object", Collections.singletonMap("ru", "Изменять объекты и текущий объект")))
				.addPermission("delete", MultivariantString.getString("Delete objects", Collections.singletonMap("ru", "Удалять объекты")))
				.addPermission(
						"publish",
						MultivariantString.getString("Scheduling / Publishing / Unpublishing", Collections.singletonMap("ru", "Планирование / Публикация / Снятие публикации")))
				.addPreset(new String[]{
						"execute"
		}, MultivariantString.getString("Public visitor", Collections.singletonMap("ru", "Публичный посетитель"))).addPreset(new String[]{
				"execute", "execute_draft", "create", "modify", "delete"
		}, MultivariantString.getString("Content writer", Collections.singletonMap("ru", "Писатель")));
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) throws Exception {
		
		if (command == ControlNodeImpl.FORM_EDIT) {
			final BaseEntry<?> entry = this.getEntry();
			final BaseChange change = entry.createChange();
			change.setCommitLogged();
			final Locker lock = this.parent.areLocksSupported()
				? this.parent.getLocker().createLock(this.innerId, 0)
				: null;
			if (lock == null || lock.isOwned()) {
				return new FormEntryEdit(this.parent, change, lock);
			}
			return new FormLocked(lock, new FormEntryEdit(this.parent, change, null));
		}
		if (command == ControlNodeImpl.FORM_SCHEDULE) {
			final BaseEntry<?> entry = this.getEntry();
			return new FormEntryEditSchedule(this.parent, entry);
		}
		if (command == ControlNodeImpl.FORM_STRUCTURE) {
			final BaseEntry<?> entry = this.getEntry();
			final BaseChange change = entry.createChange();
			change.setCommitLogged();
			final Locker lock = this.parent.areLocksSupported()
				? this.parent.getLocker().createLock(this.innerId, 0)
				: null;
			if (lock == null || lock.isOwned()) {
				return new FormEntryInternalStructure(this.parent, change, lock);
			}
			return new FormLocked(lock, new FormEntryInternalStructure(this.parent, change, null));
		}
		if (command == ControlNodeImpl.CMD_CLEAR_ALL) {
			final BaseEntry<?> entry = this.getEntry();
			final Set<String> guids = Create.tempSet();
			final List<ControlBasic<?>> children = entry.getChildren();
			if (children != null) {
				for (final ControlBasic<?> current : children) {
					ControlNodeImpl.unlinkRecursive((BaseEntry<?>) current, guids, false);
				}
			}
			return MultivariantString.getString("Contents cleared", Collections.singletonMap("ru", "Содержимое очищено"));
		}
		if (command == ControlNodeImpl.CMD_CREATE) {
			return new FormEntryCreateChooseTemplate(this.parent, this.innerId, null, null);
		}
		if (command == ControlNodeImpl.CMD_BROWSE || command == ControlNodeImpl.CMD_BROWSE_DRAFT) {
			final BaseEntry<?> entry = this.getEntry();
			try {
				return new URL(entry.getLocationAbsolute());
			} catch (final MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
		if (command == ControlNodeImpl.CMD_CHANGE_TEMPLATE || command == ControlNodeImpl.CMD_CHANGE_TEMPLATE_PUBLISHED) {
			final BaseEntry<?> entry = this.getEntry();
			final BaseChange change = entry.createChange();
			change.setCommitLogged();
			final Locker lock = this.parent.areLocksSupported()
				? this.parent.getLocker().createLock(this.innerId, 0)
				: null;
			if (lock == null || lock.isOwned()) {
				return new FormEntryChangeTemplate(this.parent, change, lock);
			}
			return new FormLocked(lock, new FormEntryChangeTemplate(this.parent, change, null));
		}
		if (command == ControlNodeImpl.CMD_HISTORY || command == ControlNodeImpl.CMD_HISTORY_PUBLISHED) {
			final BaseEntry<?> entry = this.getEntry();
			final BaseChange change = entry.createChange();
			change.setCommitLogged();
			final Locker lock = this.parent.areLocksSupported()
				? this.parent.getLocker().createLock(this.innerId, 0)
				: null;
			if (lock == null || lock.isOwned()) {
				return new FormEntryHistory(this.parent, change, lock);
			}
			return new FormLocked(lock, new FormEntryHistory(this.parent, change, null));
		}
		if (command == ControlNodeImpl.CMD_VERSIONS || command == ControlNodeImpl.CMD_VERSIONS_PUBLISHED) {
			final BaseEntry<?> entry = this.getEntry();
			final BaseChange change = entry.createChange();
			change.setCommitLogged();
			final Locker lock = this.parent.areLocksSupported()
				? this.parent.getLocker().createLock(this.innerId, 0)
				: null;
			if (lock == null || lock.isOwned()) {
				return new FormEntryVersions(this.parent, change, lock);
			}
			return new FormLocked(lock, new FormEntryVersions(this.parent, change, null));
		}
		if (command == ControlNodeImpl.CMD_PASTE || command == ControlNodeImpl.CMD_PASTE_PUBLISHED) {
			final Clipboard clipboard = Clipboard.getClipboard(this.parent);
			for (String id = this.innerId; id != null;) {
				if (clipboard.objects.contains(id)) {
					throw new IllegalArgumentException("Cannot paste object into itself!");
				}
				final BaseEntry<?> current = this.parent.getStorage().getByGuid(id);
				if (current == null) {
					break;
				}
				id = current.getParentGuid();
			}
			return new FormPaste(this.parent, clipboard, this.innerId);
		}
		if (command == ControlNodeImpl.CMD_TOUCH) {
			final BaseEntry<?> entry = this.getEntry();
			if (entry == null) {
				return "Nothing to update.";
			}
			final BaseChange change = entry.createChange();
			change.touch();
			change.commit();
			return null;
		}
		if (command == ControlNodeImpl.CMD_SEARCH) {
			final BaseEntry<?> entry = this.getEntry();
			return new FormSearch(entry);
		}
		if ("unlink".equals(command.getKey())) {
			final BaseEntry<?> parent = this.getEntry();
			final BaseEntry<?> entry = parent.getChildByName(Base.getString(command.getAttributes(), "key", ""));
			final Type<?> type = entry.getType();
			if (type != null && type.hasDeletionForm()) {
				return new FormTypeUnlink(type, entry);
			}
			return new FormEntryUnlinkConfirmation(entry);
		}
		if ("delete".equals(command.getKey())) {
			final BaseEntry<?> parent = this.getEntry();
			final BaseEntry<?> entry = parent.getChildByName(Base.getString(command.getAttributes(), "key", ""));
			final Type<?> type = entry.getType();
			if (type != null && type.hasDeletionForm()) {
				return new FormTypeDelete(type, entry);
			}
			return new FormEntryDeleteConfirmation(entry);
		}
		if ("delete_more".equals(command.getKey())) {
			final BaseEntry<?> parent = this.getEntry();
			final BaseEntry<?> entry = parent.getChildByName(Base.getString(command.getAttributes(), "key", ""));
			final Type<?> type = entry.getType();
			if (type != null && type.hasDeletionForm()) {
				return new FormTypeDeleteMore(type, entry);
			}
			return new FormEntryDeleteMore(entry);
		}
		if ("cut".equals(command.getKey())) {
			final BaseEntry<?> parent = this.getEntry();
			final BaseEntry<?> entry = parent.getChildByName(Base.getString(command.getAttributes(), "key", ""));
			if (entry == null) {
				Clipboard.setClipboard(this.parent, Clipboard.EMPTY);
			} else {
				Clipboard.setClipboard(this.parent, new Clipboard(true, Collections.singleton(entry.getGuid())));
			}
			return null;
		}
		if ("copy".equals(command.getKey())) {
			final BaseEntry<?> parent = this.getEntry();
			final BaseEntry<?> entry = parent.getChildByName(Base.getString(command.getAttributes(), "key", ""));
			if (entry == null) {
				Clipboard.setClipboard(this.parent, Clipboard.EMPTY);
			} else {
				Clipboard.setClipboard(this.parent, new Clipboard(false, Collections.singleton(entry.getGuid())));
			}
			return null;
		}
		if ("unlink_multi".equals(command.getKey())) {
			final BaseArray keys = Convert.MapEntry.toCollection(command.getAttributes(), "keys", null);
			if (keys != null && !keys.isEmpty()) {
				if (keys.length() == 1) {
					final BaseEntry<?> parent = this.getEntry();
					final BaseEntry<?> entry = parent.getChildByName(keys.baseGet(0, BaseObject.UNDEFINED).baseToJavaString());
					final Type<?> type = entry.getType();
					if (type != null && type.hasDeletionForm()) {
						return new FormTypeUnlink(type, entry);
					}
					return new FormEntryUnlinkConfirmation(entry);
				}
				return new FormEntryUnlinkConfirmationMultiple(this.parent, this.innerId, keys);
			}
			return null;
		}
		if ("delete_multi".equals(command.getKey())) {
			final BaseArray keys = Convert.MapEntry.toCollection(command.getAttributes(), "keys", null);
			if (keys != null && !keys.isEmpty()) {
				if (keys.length() == 1) {
					final BaseEntry<?> parent = this.getEntry();
					final BaseEntry<?> entry = parent.getChildByName(keys.baseGet(0, BaseObject.UNDEFINED).baseToJavaString());
					final Type<?> type = entry.getType();
					if (type != null && type.hasDeletionForm()) {
						return new FormTypeDelete(type, entry);
					}
					return new FormEntryDeleteConfirmation(entry);
				}
				return new FormEntryDeleteConfirmationMultiple(this.parent, this.innerId, keys);
			}
			return null;
		}
		if ("delete_more_multi".equals(command.getKey())) {
			final BaseArray keys = Convert.MapEntry.toCollection(command.getAttributes(), "keys", null);
			if (keys != null && !keys.isEmpty()) {
				if (keys.length() == 1) {
					final BaseEntry<?> parent = this.getEntry();
					final String key = keys.baseGet(0, BaseObject.UNDEFINED).baseToJavaString();
					final BaseEntry<?> entry = parent.getChildByName(key);
					if (entry == null) {
						throw new IllegalArgumentException("No content entry for given key: " + key);
					}
					final Type<?> type = entry.getType();
					if (type != null && type.hasDeletionForm()) {
						return new FormTypeDeleteMore(type, entry);
					}
					return new FormEntryDeleteMore(entry);
				}
				return new FormEntryDeleteMoreMultiple(this.parent, this.innerId, keys);
			}
			return null;
		}
		if ("cut_multi".equals(command.getKey())) {
			final BaseArray keys = Convert.MapEntry.toCollection(command.getAttributes(), "keys", null);
			if (keys != null && !keys.isEmpty()) {
				final Set<String> objects = new TreeSet<>();
				final int length = keys.length();
				for (int i = 0; i < length; ++i) {
					final String key = keys.baseGet(i, BaseObject.UNDEFINED).baseToJavaString();
					final BaseEntry<?> parent = this.getEntry();
					final BaseEntry<?> entry = parent.getChildByName(key);
					if (entry != null) {
						objects.add(entry.getGuid());
					}
				}
				Clipboard.setClipboard(this.parent, new Clipboard(true, objects));
			} else {
				Clipboard.setClipboard(this.parent, Clipboard.EMPTY);
			}
			return null;
		}
		if ("copy_multi".equals(command.getKey())) {
			final BaseArray keys = Convert.MapEntry.toCollection(command.getAttributes(), "keys", null);
			if (keys != null && !keys.isEmpty()) {
				final Set<String> objects = new TreeSet<>();
				final int length = keys.length();
				for (int i = 0; i < length; ++i) {
					final String key = keys.baseGet(i, BaseObject.UNDEFINED).baseToJavaString();
					final BaseEntry<?> parent = this.getEntry();
					final BaseEntry<?> entry = parent.getChildByName(key);
					if (entry != null) {
						objects.add(entry.getGuid());
					}
				}
				Clipboard.setClipboard(this.parent, new Clipboard(false, objects));
			} else {
				Clipboard.setClipboard(this.parent, Clipboard.EMPTY);
			}
			return null;
		}
		{
			final ControlCommand<?> storageCommand = (ControlCommand<?>) command.getAttributes().baseGet("storage_cmmn_cmd_original", null);
			if (storageCommand != null) {
				return this.parent.getCommandResult(storageCommand, arguments);
			}
			final ControlCommand<?> storageContentCommand = (ControlCommand<?>) command.getAttributes().baseGet("storage_cntnt_cmd_original", null);
			if (storageContentCommand != null) {
				return this.parent.getCommandResult(storageContentCommand, arguments);
			}
			final ControlCommand<?> typeCommand = (ControlCommand<?>) command.getAttributes().baseGet("inner_cmd_original", null);
			if (typeCommand != null) {
				final BaseEntry<?> entry = this.getEntry();
				return entry.getCommandResult(typeCommand, arguments);
			}
			throw new IllegalArgumentException("Unknown command: " + command.getKey());
		}
	}
	
	@Override
	public ControlCommandset getCommands() {
		
		final ControlCommandset result = Control.createOptions();
		final BaseEntry<?> entry = this.getEntry();
		final boolean lostAndFound = entry.getKey().equals("lost_found") && this.parent.getStorage().getRootIdentifier().equals(entry.getParentGuid());
		if (lostAndFound) {
			final List<ControlBasic<?>> children = entry.getChildren();
			if (children != null && !children.isEmpty()) {
				result.add(ControlNodeImpl.CMD_CLEAR_ALL);
			}
		}
		{
			final BaseHostLookup creatable = Helper.getLookupValidChildrenTypes(entry);
			if (creatable != null) {
				if (Base.keys(creatable).hasNext()) {
					result.add(ControlNodeImpl.CMD_CREATE);
				}
			}
		}
		if (!lostAndFound) {
			if (entry.getState() > 0) {
				result.add(ControlNodeImpl.CMD_CHANGE_TEMPLATE_PUBLISHED);
			} else {
				result.add(ControlNodeImpl.CMD_CHANGE_TEMPLATE);
			}
			if (this.parent.areObjectVersionsSupported()) {
				if (entry.getState() > 0) {
					result.add(ControlNodeImpl.CMD_VERSIONS_PUBLISHED);
				} else {
					result.add(ControlNodeImpl.CMD_VERSIONS);
				}
			}
			if (this.parent.areObjectHistoriesSupported()) {
				if (entry.getState() > 0) {
					result.add(ControlNodeImpl.CMD_HISTORY_PUBLISHED);
				} else {
					result.add(ControlNodeImpl.CMD_HISTORY);
				}
			}
			if (entry.getState() > ModuleInterface.STATE_DRAFT) {
				result.add(ControlNodeImpl.CMD_BROWSE);
			} else {
				result.add(ControlNodeImpl.CMD_BROWSE_DRAFT);
			}
			result.add(ControlNodeImpl.CMD_TOUCH);
		}
		result.add(ControlNodeImpl.CMD_SEARCH);
		if (!Clipboard.getClipboard(this.parent).isEmpty()) {
			if (entry.getState() > ModuleInterface.STATE_DRAFT) {
				result.add(ControlNodeImpl.CMD_PASTE_PUBLISHED);
			} else {
				result.add(ControlNodeImpl.CMD_PASTE);
			}
		}
		if (!lostAndFound) {
			final ControlCommandset typeOptions = entry.getCommands();
			if (typeOptions != null && !typeOptions.isEmpty()) {
				for (final ControlCommand<?> current : typeOptions) {
					result.add(
							Control.createCommand("", "").setAttributes(current.getAttributes()).setAttribute("id", "inner_" + current.getKey())
									.setAttribute("inner_cmd_original", current));
				}
			}
		}
		{
			final ControlCommandset storageOptions = this.parent.getContentCommands(this.innerId);
			if (storageOptions != null && !storageOptions.isEmpty()) {
				for (final ControlCommand<?> current : storageOptions) {
					result.add(
							Control.createCommand("", "").setAttributes(current.getAttributes()).setAttribute("id", "cntnt_" + current.getKey())
									.setAttribute("storage_cntnt_cmd_original", current));
				}
			}
		}
		if (this.root) {
			final ControlCommandset storageOptions = this.parent.getCommands();
			if (storageOptions != null && !storageOptions.isEmpty()) {
				for (final ControlCommand<?> current : storageOptions) {
					result.add(
							Control.createCommand("", "").setAttributes(current.getAttributes()).setAttribute("id", "cmmn_" + current.getKey())
									.setAttribute("storage_cmmn_cmd_original", current));
				}
			}
		}
		return result;
	}
	
	@Override
	public ControlCommandset getContentCommands(final String key) {
		
		final BaseEntry<?> entry = this.getEntry().getChildByName(key);
		if (entry == null) {
			return null;
		}
		final ControlCommandset result = Control.createOptions();
		final boolean published = entry.getState() > ModuleInterface.STATE_DRAFT;
		if (this.parent.areLinksSupported()) {
			result.add(Control.createCommand("unlink", MultivariantString.getString("Unlink", Collections.singletonMap("ru", "Удалить ссылку"))).setCommandPermission(published
				? "publish"
				: "delete").setCommandIcon("command-delete").setAttribute("key", key));
		}
		if (this.parent.areSoftDeletionsSupported()) {
			result.add(
					Control.createCommand("delete_more", MultivariantString.getString("Delete...", Collections.singletonMap("ru", "Удаление...")))
							.setCommandPermission(entry.getState() > 0
								? "publish"
								: "delete")
							.setCommandIcon("command-delete").setGlobal(true).setAttribute("key", key));
		} else {
			result.add(Control.createCommand("delete", MultivariantString.getString("Delete", Collections.singletonMap("ru", "Удалить"))).setCommandPermission(entry.getState() > 0
				? "publish"
				: "delete").setCommandIcon("command-delete").setGlobal(true).setAttribute("key", key));
		}
		result.add(Control.createCommand("cut", MultivariantString.getString("Cut", Collections.singletonMap("ru", "Вырезать"))).setCommandPermission(published
			? "publish"
			: "delete").setCommandIcon("command-cut").setAttribute("key", key));
		result.add(Control.createCommand("copy", MultivariantString.getString("Copy", Collections.singletonMap("ru", "Копировать"))).setCommandPermission(published
			? "publish"
			: "modify").setCommandIcon("command-copy").setAttribute("key", key));
		return result;
	}
	
	@Override
	public ControlEntry<?> getContentEntry(final String key) {
		
		return this.getEntry().getChildByName(key);
	}
	
	@Override
	public ControlFieldset<?> getContentFieldset() {
		
		final BaseEntry<?> entry = this.getEntry();
		return Helper.getContentListingFieldset(entry == null
			? null
			: entry.getType());
	}
	
	@Override
	public ControlCommandset getContentMultipleCommands(final BaseArray keys) {
		
		int max_state = 0;
		if (keys != null) {
			final int length = keys.length();
			for (int i = 0; i < length; ++i) {
				final String key = keys.baseGet(i, BaseObject.UNDEFINED).baseToJavaString();
				final BaseEntry<?> entry = this.getEntry().getChildByName(key);
				if (entry != null) {
					if (entry.getState() > max_state) {
						max_state = entry.getState();
						if (max_state > ModuleInterface.STATE_DRAFT) {
							break;
						}
					}
				}
			}
		}
		final ControlCommandset result = Control.createOptions();
		if (this.parent.areLinksSupported()) {
			result.add(
					Control.createCommand("unlink_multi", MultivariantString.getString("Unlink", Collections.singletonMap("ru", "Удалить ссылки")))
							.setCommandPermission(max_state == ModuleInterface.STATE_DRAFT
								? "delete"
								: "publish")
							.setCommandIcon("command-delete").setAttribute("keys", keys));
		}
		if (this.parent.areSoftDeletionsSupported()) {
			result.add(
					Control.createCommand("delete_more_multi", MultivariantString.getString("Delete...", Collections.singletonMap("ru", "Удаление...")))
							.setCommandPermission(max_state == ModuleInterface.STATE_DRAFT
								? "delete"
								: "publish")
							.setCommandIcon("command-delete").setAttribute("keys", keys));
		} else {
			result.add(
					Control.createCommand("delete_multi", MultivariantString.getString("Delete objects", Collections.singletonMap("ru", "Удалить объекты")))
							.setCommandPermission(max_state == ModuleInterface.STATE_DRAFT
								? "delete"
								: "publish")
							.setCommandIcon("command-delete").setAttribute("keys", keys));
		}
		result.add(
				Control.createCommand("cut_multi", MultivariantString.getString("Cut", Collections.singletonMap("ru", "Вырезать")))
						.setCommandPermission(max_state == ModuleInterface.STATE_DRAFT
							? "delete"
							: "publish")
						.setCommandIcon("command-cut").setAttribute("keys", keys));
		result.add(
				Control.createCommand("copy_multi", MultivariantString.getString("Copy", Collections.singletonMap("ru", "Копировать")))
						.setCommandPermission(max_state == ModuleInterface.STATE_DRAFT
							? "modify"
							: "publish")
						.setCommandIcon("command-copy").setAttribute("keys", keys));
		return result;
	}
	
	@Override
	public List<ControlBasic<?>> getContents() {
		
		final BaseEntry<?> entry = this.getEntry();
		if (entry == null) {
			return null;
		}
		return entry.getChildren();
	}
	
	@Override
	public BaseObject getData() {
		
		final BaseEntry<?> entry = this.getEntry();
		return entry == null
			? null
			: entry.getData();
	}
	
	/**
	 * @return storage entry for this node
	 */
	@Override
	public final BaseEntry<?> getEntry() {
		
		return this.parent.getStorage().getByGuid(this.innerId);
	}
	
	@Override
	public ControlCommandset getForms() {
		
		final BaseEntry<?> entry = this.getEntry();
		if (entry != null) {
			final boolean lostAndFound = entry.getKey().equals("lost_found") && this.parent.getStorage().getRootIdentifier().equals(entry.getParentGuid());
			if (lostAndFound) {
				return null;
			}
		}
		final ControlCommandset result = Control.createOptions();
		result.add(ControlNodeImpl.FORM_EDIT);
		if (this.parent.getScheduling() != null) {
			result.add(ControlNodeImpl.FORM_SCHEDULE);
		}
		result.add(ControlNodeImpl.FORM_STRUCTURE);
		return result;
	}
	
	/**
	 * @return
	 */
	@Override
	public final String getGuid() {
		
		return this.innerId;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getIcon() {
		
		return this.root
			? "container-storage"
			: this.getEntry().getIcon();
	}
	
	@Override
	public String getKey() {
		
		if (this.root) {
			return this.parent.getMnemonicName();
		}
		final BaseEntry<?> entry = this.getEntry();
		return entry == null
			? null
			: entry.getKey();
	}
	
	@Override
	public String getLocationControl() {
		
		final BaseEntry<?> entry = this.getEntry();
		return entry == null
			? null
			: entry.getLocationControl();
	}
	
	@Override
	public Collection<String> getLocationControlAll() {
		
		final BaseEntry<?> entry = this.getEntry();
		if (entry == null) {
			return null;
		}
		return entry.getLocationControlAll();
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTitle() {
		
		final BaseEntry<?> entry = this.getEntry();
		if (entry == null) {
			return this.root
				? this.parent.getMnemonicName()
				: "Untitled - no entry {" + this.innerId + "}!";
		}
		return entry.getTitle();
	}
	
	@Override
	protected ControlNode<?> internGetChildByName(final String name) {
		
		if (this.trashcanNode != null && this.trashcanNode.getKey().equals(name)) {
			return this.trashcanNode;
		}
		final BaseEntry<?> entry = this.getEntry();
		if (entry == null) {
			return null;
		}
		final BaseEntry<?> child = entry.getChildByName(name);
		if (child == null) {
			return null;
		}
		return new ControlNodeImpl(this.parent, child.getGuid());
	}
	
	@Override
	protected ControlNode<?>[] internGetChildren() {
		
		final BaseEntry<?> entry = this.getEntry();
		if (entry != null) {
			final List<ControlBasic<?>> ids = entry.getFolders();
			final List<ControlNode<?>> result = new ArrayList<>();
			if (this.trashcanNode != null) {
				result.add(this.trashcanNode);
			}
			if (ids != null && !ids.isEmpty()) {
				for (final ControlBasic<?> basic : ids) {
					final BaseEntry<?> current = (BaseEntry<?>) basic;
					if (current != null) {
						result.add(new ControlNodeImpl(this.parent, current.getGuid()));
					}
				}
			}
			return result.toArray(new ControlNode<?>[result.size()]);
		}
		return null;
	}
	
	@Override
	protected ControlNode<?>[] internGetChildrenExternal() {
		
		return this.trashcanNode == null
			? null
			: new ControlNode<?>[]{
					this.trashcanNode
		};
	}
	
	@Override
	protected boolean internHasChildren() {
		
		final BaseEntry<?> entry = this.getEntry();
		if (entry != null) {
			final List<?> ids = entry.getFolders();
			return ids != null && !ids.isEmpty();
		}
		return false;
	}
	
	@Override
	public final ReplyAnswer onQuery(final ServeRequest query) {
		
		return new QueryHandler(this.parent, this.innerId, false).onQuery(query);
	}
	
	@Override
	public String restoreFactoryIdentity() {
		
		return "storage";
	}
	
	@Override
	public String restoreFactoryParameter() {
		
		return this.parent.getMnemonicName() + ',' + this.innerId;
	}
	
	@Override
	public final Handler substituteHandler() {
		
		if (this.handler == null) {
			final QueryHandler handler = new QueryHandler(this.parent, this.innerId, true);
			this.handler = handler;
		}
		return this.handler;
	}
	
	@Override
	public String toString() {
		
		return "[object " + this.baseClass() + "(" + this.toStringDetails() + ")]";
	}
	
	@Override
	protected String toStringDetails() {
		
		try {
			final BaseEntry<?> entry = this.getEntry();
			return "id=" + this.innerId + ", location=" + entry.getLocationControl() + ", typeName=" + entry.getTypeName() + ", type=" + entry.getType();
		} catch (final Throwable t) {
			return "id=" + this.innerId;
		}
	}
}
