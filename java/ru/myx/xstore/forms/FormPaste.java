/*
 * Created on 21.06.2004
 */
package ru.myx.xstore.forms;

import java.util.Collections;
import java.util.List;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseChange;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.BaseRecycled;
import ru.myx.ae1.storage.BaseSchedule;
import ru.myx.ae1.storage.BaseSync;
import ru.myx.ae1.storage.ListByValue;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.base.BaseList;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.ControlBasic;
import ru.myx.ae3.control.ControlLookupStatic;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.help.Convert;
import ru.myx.xstore.basics.Clipboard;
import ru.myx.xstore.basics.Helper;

/**
 * @author myx
 * 		
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FormPaste extends AbstractForm<FormPaste> {
	
	private static final int OP_MOVE = 0;
	
	private static final int OP_COPY = 1;
	
	private static final int OP_LINK = 2;
	
	private static final int OP_LINK_SYNC = 3;
	
	private static final int OP_COPY_ROOTS = 4;
	
	private static final int OP_LINK_ROOTS = 5;
	
	private static final int OP_SYNC_HERE = 6;
	
	private static final int OP_SYNC_THERE = 7;
	
	private static final int OP_SYNC_BOTH = 8;
	
	private static final BaseHostLookup LOOKUP_OPERATION_TYPE_SIMPLE = new ControlLookupStatic()
			.putAppend(
					String.valueOf(FormPaste.OP_MOVE),
					MultivariantString.getString("Move object(s) to a current folder", Collections.singletonMap("ru", "Переместить объект(ы) в текущую папку")))
			.putAppend(
					String.valueOf(FormPaste.OP_COPY),
					MultivariantString.getString(
							"Copy object(s) and related hierarchy to a current folder",
							Collections.singletonMap("ru", "Скопировать объект(ы) со всей иерархией в текущую папку")))
			.putAppend(
					String.valueOf(FormPaste.OP_COPY_ROOTS),
					MultivariantString.getString(
							"Copy object(s) without related hierarchy to a current folder",
							Collections.singletonMap("ru", "Скопировать объект(ы) без иерархии в текущую папку")));
							
	private static final BaseHostLookup LOOKUP_OPERATION_TYPE_LINK = new ControlLookupStatic()
			.putAppend(String.valueOf(FormPaste.OP_MOVE), MultivariantString.getString("Move object(s)", Collections.singletonMap("ru", "Переместить объект(ы)")))
			.putAppend(
					String.valueOf(FormPaste.OP_COPY),
					MultivariantString.getString("Copy object(s) and related hierarchy", Collections.singletonMap("ru", "Скопировать объект(ы) со всей иерархией")))
			.putAppend(
					String.valueOf(FormPaste.OP_LINK),
					MultivariantString.getString("Link object(s) and related hierarchy", Collections.singletonMap("ru", "Слинковать объект(ы) со всей иерархией")))
			.putAppend(
					String.valueOf(FormPaste.OP_COPY_ROOTS),
					MultivariantString.getString("Copy object(s) without related hierarchy", Collections.singletonMap("ru", "Скопировать объект(ы) без иерархии")))
			.putAppend(
					String.valueOf(FormPaste.OP_LINK_ROOTS),
					MultivariantString.getString("Link object(s) without related hierarchy", Collections.singletonMap("ru", "Слинковать объект(ы) без иерархии")));
					
	private static final BaseHostLookup LOOKUP_OPERATION_TYPE_LINK_SYNC = new ControlLookupStatic()
			.putAppend(String.valueOf(FormPaste.OP_MOVE), MultivariantString.getString("Move object(s)", Collections.singletonMap("ru", "Переместить объект(ы)")))
			.putAppend(
					String.valueOf(FormPaste.OP_COPY),
					MultivariantString.getString("Copy object(s) and related hierarchy", Collections.singletonMap("ru", "Скопировать объект(ы) со всей иерархией")))
			.putAppend(
					String.valueOf(FormPaste.OP_LINK),
					MultivariantString.getString("Link object(s) and related hierarchy", Collections.singletonMap("ru", "Слинковать объект(ы) со всей иерархией")))
			.putAppend(
					String.valueOf(FormPaste.OP_LINK_SYNC),
					MultivariantString.getString(
							"Link object(s) with hierarchy and with syncronization established",
							Collections.singletonMap("ru", "Слинковать объект(ы) со всей иерархией и установить синхронизацию")))
			.putAppend(
					String.valueOf(FormPaste.OP_COPY_ROOTS),
					MultivariantString.getString("Copy object(s) without related hierarchy", Collections.singletonMap("ru", "Скопировать объект(ы) без иерархии")))
			.putAppend(
					String.valueOf(FormPaste.OP_LINK_ROOTS),
					MultivariantString.getString(
							"Link object(s) without hierarchy and synchronization established",
							Collections.singletonMap("ru", "Слинковать объект(ы) без иерархии и установить синхронизацию")))
			.putAppend(
					String.valueOf(FormPaste.OP_SYNC_HERE),
					MultivariantString.getString(
							"Estabilish object creation synchronization to current folder (import)",
							Collections.singletonMap("ru", "Установить импортирующую синхронизацию из выбранных объектов в текущую папку")))
			.putAppend(
					String.valueOf(FormPaste.OP_SYNC_THERE),
					MultivariantString.getString(
							"Estabilish object creation synchronization from current folder (export)",
							Collections.singletonMap("ru", "Установить экпортирующую синхронизацию из текущей папки в выбранные объекты")))
			.putAppend(
					String.valueOf(FormPaste.OP_SYNC_BOTH),
					MultivariantString.getString(
							"Estabilish object creation synchronization with current folder",
							Collections.singletonMap("ru", "Установить симметричную синхронизацию между выбранными объектами и текущей папкой")));
							
	private static final ControlFieldset<?> FIELDSET_RESTORE_LISTING = ControlFieldset.createFieldset()
			.addField(ControlFieldFactory.createFieldString("title", MultivariantString.getString("Title", Collections.singletonMap("ru", "Заголовок")), ""))
			.addField(ControlFieldFactory.createFieldDate("date", MultivariantString.getString("Deleted", Collections.singletonMap("ru", "Удален")), 0L))
			.addField(ControlFieldFactory.createFieldString("folder", MultivariantString.getString("Folder", Collections.singletonMap("ru", "Папка")), ""))
			.addField(ControlFieldFactory.createFieldOwner("owner", MultivariantString.getString("User", Collections.singletonMap("ru", "Пользователь"))));
			
	private final StorageImpl parent;
	
	private final Clipboard clipboard;
	
	private final String targetId;
	
	private final ControlFieldset<?> fieldset;
	
	private static final ControlCommand<?> CMD_OK = Control.createCommand("paste", " OK ").setCommandPermission("modify").setCommandIcon("command-paste");
	
	private static final ControlCommand<?> CMD_RESTORE = Control.createCommand("restore", " OK ").setCommandPermission("modify").setCommandIcon("command-paste");
	
	/**
	 * @param parent
	 * @param clipboard
	 * @param targetId
	 */
	public FormPaste(final StorageImpl parent, final Clipboard clipboard, final String targetId) {
		this.parent = parent;
		this.clipboard = clipboard;
		this.targetId = targetId;
		
		this.setAttributeIntern("id", "paste");
		if (clipboard.restore) {
			this.fieldset = ControlFieldset.createFieldset().addField(
					Control.createFieldList("list", MultivariantString.getString("Listing", Collections.singletonMap("ru", "Список")), null).setConstant()
							.setAttribute("content_fieldset", FormPaste.FIELDSET_RESTORE_LISTING));
							
			this.setAttributeIntern(
					"title",
					MultivariantString.getString("Restore object(s), confirmation", Collections.singletonMap("ru", "Восстановление объектов, подтверждение")));
					
			final BaseList<ControlBasic<?>> listing = BaseObject.createArray();
			for (final BaseRecycled record : clipboard.recycled) {
				final String title = record.getTitle();
				final BaseObject data = new BaseNativeObject()//
						.putAppend("title", title)//
						.putAppend("date", Base.forDateMillis(record.getDate()))//
						;
				final String folderId = record.getFolder();
				final BaseEntry<?> folder = parent.getStorage().getByGuid(folderId);
				data.baseDefine("folder", folder == null
					? "n/a"
					: folder.getLocationControl() + " - " + folder.getTitle());
				data.baseDefine("owner", record.getOwner());
				listing.add(Control.createBasic(record.getGuid(), title, data));
			}
			this.setData(new BaseNativeObject("list", listing));
		} else {
			this.fieldset = ControlFieldset.createFieldset()
					.addField(
							Control.createFieldList("list", MultivariantString.getString("Listing", Collections.singletonMap("ru", "Список")), null).setConstant()
									.setAttribute("content_fieldset", Helper.getContentListingFieldset(null)))
					.addField(
							ControlFieldFactory
									.createFieldInteger("operation", MultivariantString.getString("Operation type", Collections.singletonMap("ru", "Тип операции")), clipboard.move
										? FormPaste.OP_MOVE
										: FormPaste.OP_COPY)
									.setFieldType("select").setAttribute("lookup", parent.areLinksSupported()
										? parent.areSynchronizationsSupported()
											? FormPaste.LOOKUP_OPERATION_TYPE_LINK_SYNC
											: FormPaste.LOOKUP_OPERATION_TYPE_LINK
										: FormPaste.LOOKUP_OPERATION_TYPE_SIMPLE));
										
			this.setAttributeIntern("title", clipboard.move
				? MultivariantString.getString("Move object(s), confirmation", Collections.singletonMap("ru", "Перемещение объектов, подтверждение"))
				: MultivariantString.getString("Copy object(s), confirmation", Collections.singletonMap("ru", "Копирование объектов, подтверждение")));
			this.setData(
					new BaseNativeObject(
							"list",
							Base.forArray(new ListByValue<ControlBasic<?>>(clipboard.objects.toArray(new String[clipboard.objects.size()]), parent.getStorage()))));
		}
		this.recalculate();
	}
	
	private void doCopy() {
		
		final BaseEntry<?> current = this.parent.getStorage().getByGuid(this.targetId);
		if (current != null) {
			final BaseChange change = current.createChange();
			for (final String key : this.clipboard.objects) {
				this.internCopyRecursive(current, change, key);
			}
			change.commit();
		}
	}
	
	private void doCopyRoots() {
		
		final BaseEntry<?> current = this.parent.getStorage().getByGuid(this.targetId);
		if (current != null) {
			final BaseChange change = current.createChange();
			for (final String key : this.clipboard.objects) {
				this.internCopyOnce(current, change, key);
			}
			change.commit();
		}
	}
	
	private void doLink(final boolean synchronization) {
		
		final BaseEntry<?> current = this.parent.getStorage().getByGuid(this.targetId);
		if (current != null) {
			final BaseChange change = current.createChange();
			for (final String key : this.clipboard.objects) {
				this.internLinkRecursive(change, key, this.targetId, synchronization);
			}
			change.commit();
		}
	}
	
	private void doLinkRoots() {
		
		final BaseEntry<?> current = this.parent.getStorage().getByGuid(this.targetId);
		if (current != null) {
			final BaseChange change = current.createChange();
			for (final String key : this.clipboard.objects) {
				this.internLinkOnce(change, key, this.targetId);
			}
			change.commit();
		}
	}
	
	private void doMove() {
		
		final BaseEntry<?> target = this.parent.getStorage().getByGuid(this.targetId);
		if (target == null) {
			throw new RuntimeException("Target is unreacheable, id=" + this.targetId);
		}
		final BaseChange xact = target.createChange();
		for (final String key : this.clipboard.objects) {
			final BaseEntry<?> current = this.parent.getStorage().getByGuid(key);
			if (current != null) {
				final BaseChange change = xact.createChange(current);
				change.setParentGuid(this.targetId);
				change.commit();
			}
		}
		xact.commit();
		Clipboard.setClipboard(this.parent, new Clipboard(false, this.clipboard.objects));
	}
	
	private void doSync(final boolean syncExport, final boolean syncImport) {
		
		final BaseChange change = this.parent.getStorage().getByGuid(this.targetId).createChange();
		final BaseSync sync = change.getSynchronization();
		for (final String key : this.clipboard.objects) {
			if (syncExport) {
				sync.synchronizeExport(key);
			}
			if (syncImport) {
				sync.synchronizeImport(key);
			}
		}
		sync.commit();
		change.commit();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		
		if (command == FormPaste.CMD_RESTORE) {
			for (final BaseRecycled record : this.clipboard.recycled) {
				record.doMove(this.targetId);
			}
			return null;
		}
		if (command == FormPaste.CMD_OK) {
			final int operation = Convert.MapEntry.toInt(this.getData(), "operation", this.clipboard.move
				? FormPaste.OP_MOVE
				: FormPaste.OP_COPY);
			switch (operation) {
				case OP_MOVE :
					this.doMove();
					break;
				case OP_COPY :
					this.doCopy();
					break;
				case OP_LINK :
					this.doLink(false);
					break;
				case OP_LINK_SYNC :
					this.doLink(true);
					break;
				case OP_COPY_ROOTS :
					this.doCopyRoots();
					break;
				case OP_LINK_ROOTS :
					this.doLinkRoots();
					break;
				case OP_SYNC_HERE :
					this.doSync(false, true);
					break;
				case OP_SYNC_THERE :
					this.doSync(true, false);
					break;
				case OP_SYNC_BOTH :
					this.doSync(true, true);
					break;
				default :
					throw new IllegalArgumentException("Unknown operation: " + operation);
			}
			return null;
		}
		throw new IllegalArgumentException("Unknown command: " + command.getKey());
	}
	
	@Override
	public ControlCommandset getCommands() {
		
		return Control.createOptionsSingleton(this.clipboard.restore
			? FormPaste.CMD_RESTORE
			: FormPaste.CMD_OK);
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		
		return this.fieldset;
	}
	
	private void internCopyOnce(final BaseEntry<?> root, final BaseChange target, final String key) {
		
		final BaseEntry<?> current = this.parent.getStorage().getByGuid(key);
		if (current != null) {
			final BaseChange newEntry = target.createChild();
			
			if (this.parent.areSchedulesSupported()) {
				final BaseSchedule schedule = current.getSchedule();
				if (schedule != null && !schedule.isEmpty()) {
					final BaseSchedule newSchedule = newEntry.getSchedule();
					schedule.scheduleFill(newSchedule, true);
					newSchedule.commit();
				}
			}
			
			final BaseObject currentData = current.getData();
			final BaseObject newData = newEntry.getData();
			
			newData.baseDefineImportAllEnumerable(currentData);
			
			final String newKey;
			if (root == null) {
				newKey = current.getKey();
			} else {
				String candidate = current.getKey();
				for (;;) {
					final BaseEntry<?> child = root.getChildByName(candidate);
					if (child == null) {
						newKey = candidate;
						break;
					}
					candidate = "copyof_" + candidate;
				}
			}
			
			newEntry.setTitle(current.getTitle());
			newEntry.setKey(newKey);
			newEntry.setFolder(current.isFolder());
			newEntry.setState(current.getState());
			newEntry.setTypeName(current.getTypeName());
			newEntry.commit();
		}
	}
	
	private void internCopyRecursive(final BaseEntry<?> root, final BaseChange target, final String key) {
		
		final BaseEntry<?> current = this.parent.getStorage().getByGuid(key);
		if (current != null) {
			final BaseChange newEntry = target.createChild();
			
			if (this.parent.areSchedulesSupported()) {
				final BaseSchedule schedule = current.getSchedule();
				if (schedule != null && !schedule.isEmpty()) {
					final BaseSchedule newSchedule = newEntry.getSchedule();
					schedule.scheduleFill(newSchedule, true);
					newSchedule.commit();
				}
			}
			
			final BaseObject currentData = current.getData();
			final BaseObject newData = newEntry.getData();
			
			newData.baseDefineImportAllEnumerable(currentData);
			
			final String newKey;
			if (root == null) {
				newKey = current.getKey();
			} else {
				String candidate = current.getKey();
				for (;;) {
					final BaseEntry<?> child = root.getChildByName(candidate);
					if (child == null) {
						newKey = candidate;
						break;
					}
					candidate = "copyof_" + candidate;
				}
			}
			
			newEntry.setTitle(current.getTitle());
			newEntry.setKey(newKey);
			newEntry.setFolder(current.isFolder());
			newEntry.setState(current.getState());
			newEntry.setTypeName(current.getTypeName());
			newEntry.setCreated(current.getCreated());
			
			final List<ControlBasic<?>> list = current.getChildren(0, null);
			if (list != null && !list.isEmpty()) {
				for (int i = list.size() - 1; i >= 0; --i) {
					final BaseEntry<?> entry = (BaseEntry<?>) list.get(i);
					if (entry != null) {
						this.internCopyRecursive(null, newEntry, entry.getGuid());
					}
				}
			}
			
			newEntry.commit();
		}
	}
	
	private void internLinkOnce(final BaseChange target, final String key, final String targetId) {
		
		final BaseEntry<?> current = this.parent.getStorage().getByGuid(key);
		if (current != null) {
			final BaseChange newEntry = target.createChild();
			
			if (this.parent.areSchedulesSupported()) {
				final BaseSchedule schedule = current.getSchedule();
				if (schedule != null && !schedule.isEmpty()) {
					final BaseSchedule newSchedule = newEntry.getSchedule();
					schedule.scheduleFill(newSchedule, true);
					newSchedule.commit();
				}
			}
			
			newEntry.setCreateLinkedWith(current);
			newEntry.setKey(current.getKey());
			newEntry.setFolder(current.isFolder());
			newEntry.setState(current.getState());
			newEntry.setParentGuid(targetId);
			newEntry.commit();
		}
	}
	
	private void internLinkRecursive(final BaseChange target, final String key, final String targetId, final boolean synchronization) {
		
		final BaseEntry<?> current = this.parent.getStorage().getByGuid(key);
		if (current != null) {
			final BaseChange newEntry = target.createChild();
			final String newKey = newEntry.getGuid();
			
			if (this.parent.areSchedulesSupported()) {
				final BaseSchedule schedule = current.getSchedule();
				if (schedule != null && !schedule.isEmpty()) {
					final BaseSchedule newSchedule = newEntry.getSchedule();
					schedule.scheduleFill(newSchedule, true);
					newSchedule.commit();
				}
			}
			
			if (synchronization) {
				final BaseSync sync = newEntry.getSynchronization();
				sync.synchronizeExport(key);
				sync.synchronizeImport(key);
				sync.commit();
			}
			
			newEntry.setCreateLinkedWith(current);
			newEntry.setKey(current.getKey());
			newEntry.setFolder(current.isFolder());
			newEntry.setState(current.getState());
			newEntry.setParentGuid(targetId);
			
			final List<ControlBasic<?>> list = current.getChildren(0, null);
			if (list != null && !list.isEmpty()) {
				for (int i = list.size() - 1; i >= 0; --i) {
					final BaseEntry<?> entry = (BaseEntry<?>) list.get(i);
					if (entry != null) {
						this.internLinkRecursive(newEntry, entry.getGuid(), newKey, synchronization);
					}
				}
			}
			
			newEntry.commit();
		}
	}
}
