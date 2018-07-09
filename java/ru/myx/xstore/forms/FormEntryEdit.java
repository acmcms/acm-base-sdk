/*
 * Created on 13.04.2004
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ru.myx.xstore.forms;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.know.Server;
import ru.myx.ae1.storage.BaseChange;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae1.types.Type;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.ControlBasic;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.field.ControlFieldFactory;
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
public final class FormEntryEdit extends AbstractForm<FormEntryEdit> {
	
	private final StorageImpl plugin;
	
	private BaseChange change;
	
	private final Locker lock;
	
	private final String template;
	
	private final boolean published;
	
	private final boolean versioning;
	
	private final Type<?> type;
	
	private final ControlFieldset<?> fieldset;
	
	private final boolean hasLinks;
	
	private List<BaseEntry<?>> others;
	
	private static final String[] SYSTEM_FIELDS = {
			"$key", "$title", "$folder", "$state", "$created",
	};
	
	private static final Object STR_PATH = MultivariantString.getString("Path", Collections.singletonMap("ru", "Путь"));
	
	private static final Object STR_LISTING = MultivariantString.getString("Links to the same object", Collections.singletonMap("ru", "Ссылки на тотже объект"));
	
	private static final ControlCommand<?> CMD_CLOSE = Control.createCommand("close", " * ").setCommandIcon("command-close");
	
	private static final ControlCommand<?> CMD_DIVIDE = Control.createCommand("divide", MultivariantString.getString("Segregate", Collections.singletonMap("ru", "Разлинковать")))
			.setCommandPermission("modify").setCommandIcon("command-save");
			
	private static final ControlCommand<?> CMD_SAVE = Control.createCommand("save", " OK ").setCommandPermission("modify").setCommandIcon("command-save").setGlobal(true);
	
	private static final ControlCommand<?> CMD_APPLY = Control.createCommand("apply", MultivariantString.getString("Apply", Collections.singletonMap("ru", "Применить")))
			.setCommandPermission("modify").setCommandIcon("command-apply").setGlobal(true);
			
	private static final ControlCommand<?> CMD_DIVIDE_PUBLISHED = Control
			.createCommand("divide", MultivariantString.getString("Segregate", Collections.singletonMap("ru", "Разлинковать"))).setCommandPermission("publish")
			.setCommandIcon("command-save");
			
	private static final ControlCommand<?> CMD_SAVE_PUBLISHED = Control.createCommand("save", " OK ").setCommandPermission("publish").setCommandIcon("command-save")
			.setGlobal(true);
			
	private static final ControlCommand<?> CMD_APPLY_PUBLISHED = Control.createCommand("apply", MultivariantString.getString("Apply", Collections.singletonMap("ru", "Применить")))
			.setCommandPermission("publish").setCommandIcon("command-apply").setGlobal(true);
			
	private static final ControlCommand<?> CMD_BROWSE = Control.createCommand("browse", MultivariantString.getString("Preview", Collections.singletonMap("ru", "Просмотр")))
			.setCommandIcon("command-browse");
			
	private static final ControlCommand<?> CMD_BROWSE_DRAFT = Control.createCommand("browse", MultivariantString.getString("Preview", Collections.singletonMap("ru", "Просмотр")))
			.setCommandPermission("execute_draft").setCommandIcon("command-browse");
			
	/**
	 * @param plugin
	 * @param change
	 * @param lock
	 */
	public FormEntryEdit(final StorageImpl plugin, final BaseChange change, final Locker lock) {
		this.plugin = plugin;
		this.change = change;
		this.lock = lock;
		this.template = change.getTypeName();
		final Server server = Context.getServer(Exec.currentProcess());
		this.type = server.getTypes().getType(this.template);
		final BaseObject data = new BaseNativeObject();
		data.baseDefineImportAllEnumerable(change.getData());
		data.baseDefine("$key", change.getKey());
		data.baseDefine("$title", change.getTitle());
		data.baseDefine("$state", change.getState());
		data.baseDefine("$folder", change.isFolder());
		data.baseDefine("$type", this.template);
		data.baseDefine("$created", Base.forDateMillis(change.getCreated()));
		final BaseObject formData = change.getVersionData();
		if (formData == null) {
			if (this.type != null) {
				this.type.scriptPrepareModify(plugin.getStorage().getByGuidClean(change.getGuid(), this.template), change, data);
			}
		} else {
			data.baseDefineImportAllEnumerable(formData);
		}
		this.setData(data);
		this.published = change.getState() > 0;
		this.setAttributeIntern("id", "edit_entry");
		this.setAttributeIntern("title", MultivariantString.getString("Entry properties", Collections.singletonMap("ru", "Свойства объекта")));
		this.setAttributeIntern("path", change.getLocationControl());
		if (lock != null) {
			this.setAttributeIntern("on_close", FormEntryEdit.CMD_CLOSE);
		}
		this.versioning = plugin.areObjectVersionsSupported() && change.getVersioning();
		if (this.versioning) {
			final ControlFieldset<?> versioningFieldset = ControlFieldset.createFieldset().addField(FormEntryVersions.getVersionControlField(change));
			this.fieldset = ControlFieldset.createFieldset(versioningFieldset, this.getFieldsetIntern(server, this.template));
		} else {
			this.fieldset = this.getFieldsetIntern(server, this.template);
		}
		{
			final Collection<ControlBasic<?>> found = change.getStorageImpl().getInterface().searchForIdentity(change.getLinkedIdentity(), true);
			final List<ControlBasic<?>> listing = new ArrayList<>();
			if (found != null && !found.isEmpty()) {
				for (final ControlBasic<?> basic : found) {
					if (basic != null) {
						if (basic instanceof BaseEntry<?>) {
							final BaseEntry<?> candidate = (BaseEntry<?>) basic;
							if (candidate.getLinkedIdentity().equals(change.getLinkedIdentity())) {
								listing.add(candidate);
								if (candidate.getGuid() != this.change.getGuid() && !candidate.getGuid().equals(this.change.getGuid())) {
									if (this.others == null) {
										this.others = new ArrayList<>();
									}
									this.others.add(candidate);
								}
							}
						}
					}
				}
			}
			if (found != null && found.size() > 1) {
				final ControlFieldset<?> listingFieldset = ControlFieldset.createFieldset()
						.addField(ControlFieldFactory.createFieldString("$path", FormEntryEdit.STR_PATH, "", 1, 255)).addFields(Helper.getContentListingFieldset(null));
				this.hasLinks = true;
				this.fieldset.addField(
						Control.createFieldList("__link_listing", FormEntryEdit.STR_LISTING, null).setAttribute("content_fieldset", listingFieldset)
								.setAttribute("content_handler", new EntryListingContainerProvider(change.getStorageImpl(), null)));
				data.baseDefine("__link_listing", Base.forArray(new EntryChangeListingFilter(Helper.getContentListingFieldset(null), listing, change)));
			} else {
				this.hasLinks = false;
			}
		}
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		
		if (command == FormEntryEdit.CMD_CLOSE) {
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		}
		if (command == FormEntryEdit.CMD_BROWSE || command == FormEntryEdit.CMD_BROWSE_DRAFT) {
			final BaseEntry<?> entry = this.plugin.getStorage().getByGuid(this.change.getGuid());
			if (entry != null) {
				try {
					return new URL(entry.getLocationAbsolute());
				} catch (final MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		}
		if (command == FormEntryEdit.CMD_DIVIDE || command == FormEntryEdit.CMD_DIVIDE_PUBLISHED) {
			this.change.segregate();
		}
		if (command == FormEntryEdit.CMD_SAVE || command == FormEntryEdit.CMD_DIVIDE || command == FormEntryEdit.CMD_DIVIDE_PUBLISHED || command == FormEntryEdit.CMD_SAVE_PUBLISHED
				|| command == FormEntryEdit.CMD_APPLY || command == FormEntryEdit.CMD_APPLY_PUBLISHED) {
			final Set<String> systemFields = new TreeSet<>();
			{
				for (final String fieldName : FormEntryEdit.SYSTEM_FIELDS) {
					systemFields.add(fieldName);
				}
				final ControlFieldset<?> fieldsetProperties = Helper.getFieldsetPropertiesClean(this.type);
				if (fieldsetProperties != null) {
					systemFields.addAll(fieldsetProperties.innerFields());
				}
				final ControlFieldset<?> fieldsetLoad = this.type.getFieldsetLoad();
				if (fieldsetLoad != null) {
					systemFields.addAll(fieldsetLoad.innerFields());
				}
			}
			final BaseObject data = this.getData();
			for (final Iterator<String> iterator = Base.keys(data); iterator.hasNext();) {
				final String key = iterator.next();
				if (systemFields.contains(key)) {
					this.change.getData().baseDefine(key, data.baseGet(key, BaseObject.UNDEFINED));
				}
			}
			this.change.setTypeName(this.template);
			if (this.versioning) {
				final BaseObject versionData = new BaseNativeObject();
				for (final Iterator<String> iterator = Base.keys(data); iterator.hasNext();) {
					final String key = iterator.next();
					if (!(key.length() > 0 && key.charAt(0) == '$') && !systemFields.contains(key)) {
						versionData.baseDefine(key, data.baseGet(key, BaseObject.UNDEFINED));
					}
				}
				this.change.setVersionData(versionData);
				final BaseObject versioning = this.getData().baseGet("$versioning", BaseObject.UNDEFINED);
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
						this.type.scriptSubmitModify(this.plugin.getStorage().getByGuidClean(this.change.getGuid(), this.template), this.change, this.change.getData());
						this.change.commit();
					}
						break;
					case FormEntryVersions.VA_DISABLE : {
						this.change.setVersioning(false);
						this.change.setCommitActive();
						this.type.scriptSubmitModify(this.plugin.getStorage().getByGuidClean(this.change.getGuid(), this.template), this.change, this.change.getData());
						this.change.commit();
					}
						break;
					default : {
						throw new IllegalArgumentException("Unknown activation type!");
					}
				}
			} else {
				this.type.scriptSubmitModify(this.plugin.getStorage().getByGuidClean(this.change.getGuid(), this.template), this.change, this.change.getData());
				this.change.commit();
			}
			if (command != FormEntryEdit.CMD_DIVIDE && command != FormEntryEdit.CMD_DIVIDE_PUBLISHED) {
				if (this.others != null) {
					for (final BaseEntry<?> entry : this.others) {
						final BaseChange change = entry.createChange();
						change.touch();
						change.commit();
					}
				}
			}
			if (command == FormEntryEdit.CMD_APPLY || command == FormEntryEdit.CMD_APPLY_PUBLISHED) {
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
			result.add(FormEntryEdit.CMD_SAVE_PUBLISHED);
			result.add(FormEntryEdit.CMD_APPLY_PUBLISHED);
			if (this.hasLinks) {
				result.add(FormEntryEdit.CMD_DIVIDE_PUBLISHED);
			}
			final BaseEntry<?> entry = this.plugin.getStorage().getByGuid(this.change.getGuid());
			if (entry != null) {
				final String location = entry.getLocationAbsolute();
				if (location != null) {
					final String control = entry.getLocationControl();
					if (!location.endsWith(control)) {
						result.add(FormEntryEdit.CMD_BROWSE);
					}
				}
			}
		} else {
			result.add(FormEntryEdit.CMD_SAVE);
			result.add(FormEntryEdit.CMD_APPLY);
			if (this.hasLinks) {
				result.add(FormEntryEdit.CMD_DIVIDE);
			}
			final BaseEntry<?> entry = this.plugin.getStorage().getByGuid(this.change.getGuid());
			if (entry != null) {
				final String location = entry.getLocationAbsolute();
				if (location != null) {
					final String control = entry.getLocationControl();
					if (!location.endsWith(control)) {
						result.add(FormEntryEdit.CMD_BROWSE_DRAFT);
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		
		return this.fieldset;
	}
	
	private ControlFieldset<?> getFieldsetIntern(final Server server, final String template) {
		
		if (this.type == null) {
			if (template.length() == 0) {
				return Helper.getFieldsetEdit(null);
			}
			return Helper.getFieldsetEdit(server.getTypes().getType(template));
		}
		return Helper.getFieldsetEdit(this.type);
	}
}
