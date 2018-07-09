/*
 * Created on 26.01.2006
 */
package ru.myx.xstore.forms;

import java.util.Collection;
import java.util.Collections;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae3.base.BaseArray;
import ru.myx.ae3.base.BaseHostLookup;
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

/**
 * @author myx
 * 		
 */
public class FormEntryDeleteMoreMultiple extends AbstractForm<FormEntryDeleteMoreMultiple> {
	
	private final StorageImpl parent;
	
	private final String sectionId;
	
	private final BaseArray keys;
	
	private static final BaseHostLookup LOOKUP_DELETE_MORE_OPERATION = new ControlLookupStatic()
			.putAppend(
					"2",
					MultivariantString.getString(
							"Unlink locally (object will be deleted when there are no more links to it)",
							Collections.singletonMap("ru", "Удалить ссылку (объект будет удален после удаления последней ссылки)")))
			.putAppend("1", MultivariantString.getString("Delete other links to the same object", Collections.singletonMap("ru", "Удалить другие ссылки на этот же объект")))
			.putAppend(
					"3",
					MultivariantString.getString(
							"Delete other links to the same object and request re-synchronization",
							Collections.singletonMap("ru", "Удалить другие ссылки на этот же объект и запросить пересинхронизацию")))
			.putAppend("0", MultivariantString.getString("Delete an object in all locations", Collections.singletonMap("ru", "Удалить объект во всём хранилище")));
			
	private static final ControlFieldset<?> FIELDSET_DELETE_MORE = ControlFieldset.createFieldset()
			.addField(
					ControlFieldFactory.createFieldInteger("operationIndex", MultivariantString.getString("Operation", Collections.singletonMap("ru", "Операция")), 2)
							.setFieldType("select").setFieldVariant("bigselect").setAttribute("lookup", FormEntryDeleteMoreMultiple.LOOKUP_DELETE_MORE_OPERATION))
			.addField(
					ControlFieldFactory
							.createFieldBoolean("soft", MultivariantString.getString("Delete to recycle bin", Collections.singletonMap("ru", "Удалить в корзину")), true));
							
	private static final ControlCommand<?> COMMAND_DELETE = Control.createCommand("delete", MultivariantString.getString("Delete", Collections.singletonMap("ru", "Удалить")))
			.setCommandPermission("delete").setCommandIcon("command-delete").setGlobal(true);
			
	/**
	 * @param parent
	 * @param sectionId
	 * @param keys
	 */
	public FormEntryDeleteMoreMultiple(final StorageImpl parent, final String sectionId, final BaseArray keys) {
		this.parent = parent;
		this.sectionId = sectionId;
		this.keys = keys;
		this.setAttributeIntern("id", "more");
		this.setAttributeIntern(
				"title",
				MultivariantString.getString(
						"Do you really want to delete these " + keys.length() + " objects?",
						Collections.singletonMap("ru", "Вы действительно хотите удалить эти " + keys.length() + " объект(а/ов)?")));
		this.recalculate();
		final BaseObject data = new BaseNativeObject()//
				.putAppend("operationIndex", 2)//
				.putAppend("soft", true)//
				;
		this.setData(data);
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		
		if (command == FormEntryDeleteMoreMultiple.COMMAND_DELETE) {
			final boolean soft = Convert.MapEntry.toBoolean(this.getData(), "soft", true);
			final int operationIndex = Convert.MapEntry.toInt(this.getData(), "operationIndex", 2);
			if (operationIndex == 1 || operationIndex == 3) {
				int unlinked = 0;
				final int length = this.keys.length();
				for (int i = 0; i < length; ++i) {
					final String key = this.keys.baseGet(i, BaseObject.UNDEFINED).baseToJavaString();
					final BaseEntry<?> entry = this.parent.getStorage().getByGuid(this.sectionId).getChildByName(key);
					if (entry != null) {
						final Collection<ControlBasic<?>> found = this.parent.getStorage().searchForIdentity(entry.getLinkedIdentity(), true);
						if (found != null && !found.isEmpty()) {
							for (final ControlBasic<?> basic : found) {
								if (basic instanceof BaseEntry<?>) {
									final BaseEntry<?> candidate = (BaseEntry<?>) basic;
									if (candidate == entry || candidate.getGuid().equals(entry.getGuid()) || !candidate.getLinkedIdentity().equals(entry.getLinkedIdentity())) {
										continue;
									}
									candidate.createChange().unlink();
									unlinked++;
								}
							}
						}
						if (operationIndex == 3) {
							entry.createChange().resync();
						}
					}
				}
				return MultivariantString.getString(unlinked + " links unlinked", Collections.singletonMap("ru", "Удалено ссылок: " + unlinked));
			}
			final boolean unlink = operationIndex == 2;
			final int length = this.keys.length();
			for (int i = 0; i < length; ++i) {
				final BaseEntry<?> entry = this.parent.getStorage().getByGuid(this.sectionId).getChildByName(this.keys.baseGet(i, BaseObject.UNDEFINED).baseToJavaString());
				if (entry != null) {
					if (unlink) {
						entry.createChange().unlink(soft);
					} else {
						entry.createChange().delete(soft);
					}
				}
			}
			return null;
		}
		throw new IllegalArgumentException("Unknown command: " + command.getKey());
	}
	
	@Override
	public ControlCommandset getCommands() {
		
		final ControlCommandset result = Control.createOptions();
		result.add(FormEntryDeleteMoreMultiple.COMMAND_DELETE);
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		
		return FormEntryDeleteMoreMultiple.FIELDSET_DELETE_MORE;
	}
}
