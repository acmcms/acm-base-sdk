/*
 * Created on 26.01.2006
 */
package ru.myx.xstore.forms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae3.base.Base;
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
import ru.myx.xstore.basics.Helper;

/**
 * @author myx
 * 		
 */
public class FormEntryDeleteMore extends AbstractForm<FormEntryDeleteMore> {
	
	private final BaseEntry<?> entry;
	
	private final ControlFieldset<?> fieldset;
	
	private static final Object STR_PATH = MultivariantString.getString("Path", Collections.singletonMap("ru", "Путь"));
	
	private static final Object STR_LISTING = MultivariantString.getString("Links to the same object", Collections.singletonMap("ru", "Ссылки на тотже объект"));
	
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
			
	private static final ControlCommand<?> COMMAND_DELETE = Control.createCommand("delete", MultivariantString.getString("Delete", Collections.singletonMap("ru", "Удалить")))
			.setCommandPermission("delete").setCommandIcon("command-delete").setGlobal(true);
			
	/**
	 * @param entry
	 */
	public FormEntryDeleteMore(final BaseEntry<?> entry) {
		this.entry = entry;
		this.setAttributeIntern("id", "more");
		this.setAttributeIntern(
				"title",
				MultivariantString.getString("Do you really want to delete this entry?", Collections.singletonMap("ru", "Вы действительно хотите удалить этот объект?")));
		this.setAttributeIntern("path", entry.getLocationControl());
		this.recalculate();
		final ControlFieldset<?> listingFieldset = ControlFieldset.createFieldset()
				.addField(ControlFieldFactory.createFieldString("$path", FormEntryDeleteMore.STR_PATH, "", 1, 255)).addFields(Helper.getContentListingFieldset(null));
		this.fieldset = ControlFieldset.createFieldset();
		this.fieldset.addField(
				ControlFieldFactory.createFieldInteger("operationIndex", MultivariantString.getString("Operation", Collections.singletonMap("ru", "Операция")), 2)
						.setFieldType("select").setFieldVariant("bigselect").setAttribute("lookup", FormEntryDeleteMore.LOOKUP_DELETE_MORE_OPERATION));
		this.fieldset.addField(
				ControlFieldFactory.createFieldBoolean("soft", MultivariantString.getString("Delete to recycle bin", Collections.singletonMap("ru", "Удалить в корзину")), true));
		this.fieldset.addField(
				Control.createFieldList("listing", FormEntryDeleteMore.STR_LISTING, null).setAttribute("content_fieldset", listingFieldset)
						.setAttribute("content_handler", new EntryListingContainerProvider(entry.getStorageImpl(), null)));
		final BaseObject data = new BaseNativeObject()//
				.putAppend("operationIndex", 2)//
				.putAppend("soft", true)//
				;
		final Collection<ControlBasic<?>> found = entry.getStorageImpl().getInterface().searchForIdentity(entry.getLinkedIdentity(), true);
		final List<ControlBasic<?>> listing = new ArrayList<>();
		if (found != null && !found.isEmpty()) {
			for (final ControlBasic<?> basic : found) {
				if (basic != null) {
					if (basic instanceof BaseEntry<?>) {
						final BaseEntry<?> candidate = (BaseEntry<?>) basic;
						if (candidate.getLinkedIdentity().equals(entry.getLinkedIdentity())) {
							listing.add(candidate);
						}
					}
				}
			}
		}
		data.baseDefine("listing", Base.forArray(new EntryListingFilter(Helper.getContentListingFieldset(null), listing, entry, null, null)));
		this.setData(data);
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		
		if (command == FormEntryDeleteMore.COMMAND_DELETE) {
			final boolean soft = Convert.MapEntry.toBoolean(this.getData(), "soft", true);
			final int operationIndex = Convert.MapEntry.toInt(this.getData(), "operationIndex", 2);
			if (operationIndex == 1 || operationIndex == 3) {
				int unlinked = 0;
				final Collection<ControlBasic<?>> found = this.entry.getStorageImpl().getInterface().searchForIdentity(this.entry.getLinkedIdentity(), true);
				if (found != null && !found.isEmpty()) {
					for (final ControlBasic<?> basic : found) {
						if (basic instanceof BaseEntry<?>) {
							final BaseEntry<?> candidate = (BaseEntry<?>) basic;
							if (candidate == this.entry || candidate.getGuid().equals(this.entry.getGuid())
									|| !candidate.getLinkedIdentity().equals(this.entry.getLinkedIdentity())) {
								continue;
							}
							candidate.createChange().unlink();
							unlinked++;
						}
					}
				}
				if (operationIndex == 3) {
					this.entry.createChange().resync();
				}
				return MultivariantString.getString(unlinked + " links unlinked", Collections.singletonMap("ru", "Удалено ссылок: " + unlinked));
			}
			final boolean unlink = operationIndex == 2;
			if (unlink) {
				this.entry.createChange().unlink(soft);
			} else {
				this.entry.createChange().delete(soft);
			}
			return null;
		}
		throw new IllegalArgumentException("Unknown command: " + command.getKey());
	}
	
	@Override
	public ControlCommandset getCommands() {
		
		final ControlCommandset result = Control.createOptions();
		result.add(FormEntryDeleteMore.COMMAND_DELETE);
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		
		return this.fieldset;
	}
}
