/*
 * Created on 13.04.2004
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ru.myx.xstore.forms;

// import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseChange;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.BaseSync;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae1.types.Type;
import ru.myx.ae1.types.TypeRegistry;
import ru.myx.ae3.act.Context;
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
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.help.Convert;
import ru.myx.ae3.help.Create;
import ru.myx.xstore.basics.Helper;

/**
 * @author myx
 * 		
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public final class FormEntryCreateChooseTemplate extends AbstractForm<FormEntryCreateChooseTemplate> {
	
	private static final BaseObject STR_OBJECT_TYPE = MultivariantString.getString("Object type", Collections.singletonMap("ru", "Тип объекта"));
	
	private static final BaseObject STR_OPERATION_TYPE = MultivariantString.getString("Operation type", Collections.singletonMap("ru", "Тип операции"));
	
	private static final BaseHostLookup LOOKUP_OPERATION_TYPE = new ControlLookupStatic()
			.putAppend("true", MultivariantString.getString("Create locally in current folder only", Collections.singletonMap("ru", "Создать локально только в текущей папке")))
			.putAppend(
					"false",
					MultivariantString.getString(
							"Create globally in all folders synchronized with current folder ",
							Collections.singletonMap("ru", "Создать глобально во всех папках синхронизированных с текущей")));
							
	private static final BaseObject STR_VERSIONING = MultivariantString.getString("Version control", Collections.singletonMap("ru", "Контроль версий"));
	
	private static final BaseHostLookup LOOKUP_VERSION_MODES = new ControlLookupStatic()
			.putAppend(
					"-",
					MultivariantString.getString("Default mode - as designed in type definition", Collections.singletonMap("ru", "По умолчанию - как задумано в описании типа")))
			.putAppend("true", MultivariantString.getString("Version control: activated", Collections.singletonMap("ru", "Контроль версий: включен")))
			.putAppend("false", MultivariantString.getString("Version control: deactivated", Collections.singletonMap("ru", "Контроль версий: отключен")));
			
	private static final BaseObject STR_PATH = MultivariantString.getString("Path", Collections.singletonMap("ru", "Путь"));
	
	private static final BaseObject STR_LISTING = MultivariantString
			.getString("Links to the same folder and synchronized folders", Collections.singletonMap("ru", "Ссылки на ту же папку и синхронизированные папки"));
			
	private final StorageImpl plugin;
	
	private final String sectionId;
	
	private final BaseObject entryData;
	
	private final ControlFieldset<?> fieldset;
	
	private static final ControlCommand<?> CMD_NEXT = Control.createCommand("next", MultivariantString.getString("Next...", Collections.singletonMap("ru", "Далее...")))
			.setCommandPermission("create").setCommandIcon("command-next");
			
	private static final ControlCommand<?> CMD_INFO = Control.createCommand("info", MultivariantString.getString("Info...", Collections.singletonMap("ru", "Информация...")))
			.setCommandPermission("create").setCommandIcon("command-info");
			
	/**
	 * @param plugin
	 * @param sectionId
	 * @param defaultTemplate
	 * @param entryData
	 */
	public FormEntryCreateChooseTemplate(final StorageImpl plugin, final String sectionId, final String defaultTemplate, final BaseObject entryData) {
		this.plugin = plugin;
		this.sectionId = sectionId;
		this.entryData = entryData;
		final BaseEntry<?> section = plugin.getStorage().getByGuid(sectionId);
		this.fieldset = ControlFieldset.createFieldset("choose_template");
		this.fieldset.addField(
				ControlFieldFactory.createFieldString("template", FormEntryCreateChooseTemplate.STR_OBJECT_TYPE, defaultTemplate).setFieldType("select")
						.setFieldVariant("bigselect").setAttribute("lookup", Helper.getLookupValidChildrenTypes(section)));
		if (plugin.areLinksSupported()) {
			{
				final Set<String> guids = Create.tempSet();
				final List<ControlBasic<?>> listing = new ArrayList<>();
				{
					if (plugin.areSynchronizationsSupported()) {
						final BaseSync sync = section.getSynchronization();
						if (sync != null) {
							final String[] export = sync.getExportSynchronizations();
							if (export != null) {
								for (final String guid : export) {
									final BaseEntry<?> candidate = plugin.getInterface().getByGuid(guid);
									if (candidate != null && !guids.contains(candidate.getGuid())) {
										listing.add(candidate);
										guids.add(candidate.getGuid());
									}
								}
							}
						}
					}
				}
				if (!guids.isEmpty()) {
					this.fieldset.addField(
							ControlFieldFactory.createFieldBoolean("local", FormEntryCreateChooseTemplate.STR_OPERATION_TYPE, false).setFieldType("select")
									.setAttribute("lookup", FormEntryCreateChooseTemplate.LOOKUP_OPERATION_TYPE));
					final ControlFieldset<?> listingFieldset = ControlFieldset.createFieldset()
							.addField(ControlFieldFactory.createFieldString("$path", FormEntryCreateChooseTemplate.STR_PATH, "", 1, 255))
							.addFields(Helper.getContentListingFieldset(null));
					this.fieldset.addField(
							Control.createFieldList("listing", FormEntryCreateChooseTemplate.STR_LISTING, null).setAttribute("content_fieldset", listingFieldset)
									.setAttribute("content_handler", new EntryListingContainerProvider(plugin, null)));
					final BaseObject data = new BaseNativeObject()//
							.putAppend(
									"listing", //
									Base.forArray(new EntryListingFilter(
											Helper.getContentListingFieldset(null), //
											listing,
											section,
											null,
											null) //
					) //
					)//
					;
					this.setData(data);
				}
			}
		}
		if (plugin.areObjectVersionsSupported()) {
			this.fieldset.addField(
					ControlFieldFactory.createFieldString("versioning", FormEntryCreateChooseTemplate.STR_VERSIONING, "-").setFieldType("select")
							.setAttribute("lookup", FormEntryCreateChooseTemplate.LOOKUP_VERSION_MODES));
		}
		this.setAttributeIntern("id", "choose_template");
		this.setAttributeIntern("title", MultivariantString.getString("Choose entry type", Collections.singletonMap("ru", "Выбор типа объекта")));
		this.setAttributeIntern("path", plugin.getStorage().getByGuid(sectionId).getLocationControl());
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		
		final TypeRegistry types = Context.getServer(Exec.currentProcess()).getTypes();
		if (command == FormEntryCreateChooseTemplate.CMD_INFO) {
			final String typeName = Base.getString(this.getData(), "template", types.getTypeNameDefault());
			final Type<?> type = types.getType(typeName);
			return new FormTypeInfo(type, this, FormEntryCreateChooseTemplate.CMD_NEXT);
		}
		if (command == FormEntryCreateChooseTemplate.CMD_NEXT) {
			final String typeName = Base.getString(this.getData(), "template", types.getTypeNameDefault());
			final Type<?> type = types.getType(typeName);
			final boolean local = Convert.MapEntry.toBoolean(this.getData(), "local", true);
			final boolean versioning = Convert.MapEntry.toBoolean(this.getData(), "versioning", type.getDefaultVersioning());
			final BaseEntry<?> folder = this.plugin.getStorage().getByGuid(this.sectionId);
			final BaseChange change = folder.createChild();
			if (this.entryData != null) {
				change.getData().baseDefineImportAllEnumerable(this.entryData);
			}
			return new FormEntryCreate(this.plugin, change, typeName, local, versioning);
		}
		{
			throw new IllegalArgumentException("Unknown command: " + command.getKey());
		}
	}
	
	@Override
	public ControlCommandset getCommands() {
		
		final ControlCommandset result = Control.createOptions();
		result.add(FormEntryCreateChooseTemplate.CMD_NEXT);
		result.add(FormEntryCreateChooseTemplate.CMD_INFO);
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		
		return this.fieldset;
	}
}
