/*
 * Created on 24.08.2004
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ru.myx.xstore.forms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ru.myx.ae1.access.AuthLevels;
import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.messaging.Message;
import ru.myx.ae1.messaging.MessageBlank;
import ru.myx.ae1.messaging.Messaging;
import ru.myx.ae1.messaging.MessagingManager;
import ru.myx.ae1.storage.BaseChange;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.BaseVersion;
import ru.myx.ae1.storage.PluginRegistry;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BasePrimitive;
import ru.myx.ae3.base.BaseString;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.ControlForm;
import ru.myx.ae3.control.ControlLookupStatic;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.field.ControlField;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.exec.ExecProcess;
import ru.myx.ae3.help.Convert;
import ru.myx.ae3.report.Report;
import ru.myx.jdbc.lock.Locker;

/**
 * @author myx
 * 
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class FormEntryVersions extends AbstractForm<FormEntryVersions> {
	
	
	private static final BaseObject STR_VERSION_COMMENT = MultivariantString.getString("Version comment", Collections.singletonMap("ru", "Комментарий"));
	
	private static final BaseObject STR_VERSION_OPERATION = MultivariantString.getString("Operation", Collections.singletonMap("ru", "Операция"));
	
	private static final BaseObject STR_VERSION_ACTIVATE_DRAFT = MultivariantString.getString("Save as draft", Collections.singletonMap("ru", "Сохранить как черновик"));
	
	private static final BaseObject STR_VERSION_ACTIVATE_VISA = MultivariantString
			.getString("Initiate visa activation for this version", Collections.singletonMap("ru", "Инициировать процесс визирования"));
	
	private static final BaseObject STR_VERSION_ACTIVATE_NOW = MultivariantString
			.getString("Set this change as an active version", Collections.singletonMap("ru", "Сделать сохраненные изменения активной версией"));
	
	private static final BaseObject STR_VERSION_DISABLE = MultivariantString
			.getString("Turn versionning off and set this content active", Collections.singletonMap("ru", "Отключить версионность и активировать изменения"));
	
	static final int VA_DRAFT = 0;
	
	static final int VA_VISA = 1;
	
	static final int VA_ACTIVATE = 2;
	
	static final int VA_DISABLE = 3;
	
	private static final BaseHostLookup LOOKUP_ACTIVATION_MDLESS = new ControlLookupStatic()
			.putAppend(String.valueOf(FormEntryVersions.VA_DRAFT), FormEntryVersions.STR_VERSION_ACTIVATE_DRAFT);
	
	private static final BaseHostLookup LOOKUP_ACTIVATION_MDMORE = new ControlLookupStatic()
			.putAppend(String.valueOf(FormEntryVersions.VA_DRAFT), FormEntryVersions.STR_VERSION_ACTIVATE_DRAFT)
			.putAppend(String.valueOf(FormEntryVersions.VA_ACTIVATE), FormEntryVersions.STR_VERSION_ACTIVATE_NOW)
			.putAppend(String.valueOf(FormEntryVersions.VA_DISABLE), FormEntryVersions.STR_VERSION_DISABLE);
	
	private static final BaseHostLookup LOOKUP_ACTIVATION_MELESS = new ControlLookupStatic()
			.putAppend(String.valueOf(FormEntryVersions.VA_DRAFT), FormEntryVersions.STR_VERSION_ACTIVATE_DRAFT)
			.putAppend(String.valueOf(FormEntryVersions.VA_VISA), FormEntryVersions.STR_VERSION_ACTIVATE_VISA);
	
	private static final BaseHostLookup LOOKUP_ACTIVATION_MEMORE = new ControlLookupStatic()
			.putAppend(String.valueOf(FormEntryVersions.VA_DRAFT), FormEntryVersions.STR_VERSION_ACTIVATE_DRAFT)
			.putAppend(String.valueOf(FormEntryVersions.VA_VISA), FormEntryVersions.STR_VERSION_ACTIVATE_VISA)
			.putAppend(String.valueOf(FormEntryVersions.VA_ACTIVATE), FormEntryVersions.STR_VERSION_ACTIVATE_NOW)
			.putAppend(String.valueOf(FormEntryVersions.VA_DISABLE), FormEntryVersions.STR_VERSION_DISABLE);
	
	private static final ControlFieldset<?> FIELDSET_VERSIONING_MDLESS = ControlFieldset.createFieldset()
			.addField(ControlFieldFactory.createFieldString("$comment", FormEntryVersions.STR_VERSION_COMMENT, "-")).addField(
					ControlFieldFactory.createFieldInteger("$activate", FormEntryVersions.STR_VERSION_OPERATION, FormEntryVersions.VA_DRAFT).setFieldType("select")
							.setAttribute("lookup", FormEntryVersions.LOOKUP_ACTIVATION_MDLESS));
	
	private static final ControlFieldset<?> FIELDSET_VERSIONING_MDMORE = ControlFieldset.createFieldset()
			.addField(ControlFieldFactory.createFieldString("$comment", FormEntryVersions.STR_VERSION_COMMENT, "-")).addField(
					ControlFieldFactory.createFieldInteger("$activate", FormEntryVersions.STR_VERSION_OPERATION, FormEntryVersions.VA_ACTIVATE).setFieldType("select")
							.setAttribute("lookup", FormEntryVersions.LOOKUP_ACTIVATION_MDMORE));
	
	private static final ControlFieldset<?> FIELDSET_VERSIONING_MELESS = ControlFieldset.createFieldset()
			.addField(ControlFieldFactory.createFieldString("$comment", FormEntryVersions.STR_VERSION_COMMENT, "-")).addField(
					ControlFieldFactory.createFieldInteger("$activate", FormEntryVersions.STR_VERSION_OPERATION, FormEntryVersions.VA_VISA).setFieldType("select")
							.setAttribute("lookup", FormEntryVersions.LOOKUP_ACTIVATION_MELESS));
	
	private static final ControlFieldset<?> FIELDSET_VERSIONING_MEMORE = ControlFieldset.createFieldset()
			.addField(ControlFieldFactory.createFieldString("$comment", FormEntryVersions.STR_VERSION_COMMENT, "-")).addField(
					ControlFieldFactory.createFieldInteger("$activate", FormEntryVersions.STR_VERSION_OPERATION, FormEntryVersions.VA_ACTIVATE).setFieldType("select")
							.setAttribute("lookup", FormEntryVersions.LOOKUP_ACTIVATION_MEMORE));
	
	static final BaseObject STR_VERSION_CONTROL_FIELD = MultivariantString.getString("Versioning settings", Collections.singletonMap("ru", "Параметры версионности"));
	
	static final BaseObject STR_CURRENT_VERSION = MultivariantString.getString("Current object content", Collections.singletonMap("ru", "Текущее содержание объекта"));
	
	static final BaseObject STR_ACTIVE_VERSION = MultivariantString.getString("Active version", Collections.singletonMap("ru", "Активную версию"));
	
	static final BaseObject STR_LAST_VERSION = MultivariantString.getString("Latest version", Collections.singletonMap("ru", "Последнюю версию"));
	
	private static final ControlCommand<?> CMD_CLOSE = Control.createCommand("close", " * ").setCommandIcon("command-close");
	
	private static final ControlCommand<?> CMD_VERSIONING = Control.createCommand("versioning", MultivariantString.getString("Next...", Collections.singletonMap("ru", "Далее...")))
			.setCommandPermission("modify").setCommandIcon("command-next");
	
	private static final ControlCommand<?> CMD_EDIT = Control.createCommand("edit", MultivariantString.getString("Next...", Collections.singletonMap("ru", "Далее...")))
			.setCommandPermission("modify").setCommandIcon("command-next").setGlobal(true);
	
	private static final ControlCommand<?> CMD_EDIT_PUBLISHED = Control.createCommand("edit", MultivariantString.getString("Next...", Collections.singletonMap("ru", "Далее...")))
			.setCommandPermission("publish").setCommandIcon("command-next").setGlobal(true);
	
	private static final ControlCommand<?> CMD_ACTIVATE = Control
			.createCommand("activate", MultivariantString.getString("Activate", Collections.singletonMap("ru", "Активировать"))).setCommandPermission("modify")
			.setCommandIcon("command-activate").setGlobal(true);
	
	private static final ControlCommand<?> CMD_ACTIVATE_PUBLISHED = Control
			.createCommand("activate", MultivariantString.getString("Activate", Collections.singletonMap("ru", "Активировать"))).setCommandPermission("publish")
			.setCommandIcon("command-activate").setGlobal(true);
	
	private static final Object checkSource(final StorageImpl parent, final String guid) {
		
		
		final BaseEntry<?> entry = parent.getStorage().getByGuid(guid);
		if (entry == null) {
			if (parent.areAliasesSupported()) {
				final BaseEntry<?> entryAlias = parent.getStorage().getByAlias(guid, true);
				if (entryAlias == null) {
					return null;
				}
				return entryAlias;
			}
			return null;
		}
		return entry;
	}
	
	private static final ControlForm<?> createVisaForm(final BaseEntry<?> entry, final Message message) {
		
		
		final StorageImpl storage = entry.getStorageImpl();
		final Locker lock = storage.areLocksSupported()
			? storage.getLocker().createLock(entry.getGuid(), 0)
			: null;
		if (lock == null || lock.isOwned()) {
			return new FormVisaVersion(entry, message, lock);
		}
		return new FormLocked(lock, new FormVisaVersion(entry, message, null));
	}
	
	static final void createVisaMessage(final BaseChange change) {
		
		
		final ExecProcess process = Exec.currentProcess();
		final MessagingManager manager = Context.getServer(process).getMessagingManager();
		if (manager == null) {
			Report.warning(
					"DS/VISA",
					"No messaging found while initiating visa process: path=" + change.getLocationControl() + ", guid=" + change.getGuid() + ", version=" + change.getVersionId()
							+ ", user=" + Context.getUserId(process));
		} else {
			final BaseObject parameters = new BaseNativeObject()//
					.putAppend("storage", change.getStorageImpl().getMnemonicName())//
					.putAppend("guid", change.getGuid())//
					.putAppend("version", change.getVersionId())//
			;
			final MessageBlank message = manager.createBlankMessage("STORAGE_VISA_VERSION", parameters);
			if (message == null) {
				Report.warning(
						"DS/VISA",
						"Cannot create message while initiating visa process: path=" + change.getLocationControl() + ", guid=" + change.getGuid() + ", version="
								+ change.getVersionId() + ", user=" + Context.getUserId(process));
			} else {
				message.addRecipientAccess(change.getLocationControl(), "publish");
				message.setSendInteractive(true);
				try {
					message.commit();
				} catch (final RuntimeException e) {
					Report.exception(
							"DS/VISA",
							"Exception while initiating visa process: path=" + change.getLocationControl() + ", guid=" + change.getGuid() + ", version=" + change.getVersionId()
									+ ", user=" + Context.getUserId(process),
							e);
					throw e;
				} catch (final Exception e) {
					Report.exception(
							"DS/VISA",
							"Exception while initiating visa process: path=" + change.getLocationControl() + ", guid=" + change.getGuid() + ", version=" + change.getVersionId()
									+ ", user=" + Context.getUserId(process),
							e);
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	/**
	 * @param message
	 * @param parameters
	 * @return form
	 */
	public static final ControlForm<?> createVisaMessageForm(final Message message, final BaseObject parameters) {
		
		
		final String storage = Base.getString(parameters, "storage", "");
		final String guid = Base.getString(parameters, "guid", "");
		final String version = Base.getString(parameters, "version", "");
		{
			final StorageImpl parent = PluginRegistry.getPlugin(Context.getServer(Exec.currentProcess()), storage);
			if (parent != null) {
				final Object check1 = FormEntryVersions.checkSource(parent, guid);
				if (check1 != null) {
					final BaseEntry<?> entry = (BaseEntry<?>) check1;
					return FormEntryVersions.createVisaForm(entry.getVersion(version), message);
				}
			}
		}
		{
			final StorageImpl[] plugins = PluginRegistry.allPlugins();
			if (plugins != null && plugins.length > 0) {
				for (int i = plugins.length - 1; i >= 0; --i) {
					final StorageImpl parent = plugins[i];
					final Object check1 = FormEntryVersions.checkSource(parent, guid);
					if (check1 != null) {
						final BaseEntry<?> entry = (BaseEntry<?>) check1;
						return FormEntryVersions.createVisaForm(entry.getVersion(version), message);
					}
				}
			}
		}
		return null;
	}
	
	static final ControlField getVersionControlField(final BaseChange change) {
		
		
		final boolean publishing = change.getState() == 0
				|| Context.getServer(Exec.currentProcess()).getAccessManager().securityCheck(AuthLevels.AL_AUTHORIZED_HIGH, change.getLocationControl(), "publish") != null;
		final boolean messaging = Context.getServer(Exec.currentProcess()).getMessagingManager() != Messaging.DEFAULT_MANAGER;
		final BaseObject map = new BaseNativeObject()//
				.putAppend("$comment", "-")//
		;
		return ControlFieldFactory.createFieldMap("$versioning", FormEntryVersions.STR_VERSION_CONTROL_FIELD, map).setFieldVariant("fieldset").setAttribute("fieldset", publishing
			? messaging
				? FormEntryVersions.FIELDSET_VERSIONING_MEMORE
				: FormEntryVersions.FIELDSET_VERSIONING_MDMORE
			: messaging
				? FormEntryVersions.FIELDSET_VERSIONING_MELESS
				: FormEntryVersions.FIELDSET_VERSIONING_MDLESS);
	}
	
	private final StorageImpl plugin;
	
	private final BaseChange change;
	
	private final Locker lock;
	
	private final boolean published;
	
	private final ControlFieldset<?> fieldset;
	
	/**
	 * @param parent
	 * @param change
	 * @param lock
	 */
	public FormEntryVersions(final StorageImpl parent, final BaseChange change, final Locker lock) {
		this.plugin = parent;
		this.change = change;
		this.lock = lock;
		this.published = change.getState() > 0;
		if (change.getVersioning()) {
			final BaseVersion[] versions = change.getVersions();
			final String activeVersion;
			final String lastVersion;
			final BaseHostLookup versionLookup;
			if (versions != null) {
				final String activeVersionId = change.getVersionId();
				String lastVersionId = null;
				long lastDate = 0L;
				boolean hasActiveVersion = false;
				for (int i = versions.length - 1; i >= 0; --i) {
					final BaseVersion version = versions[i];
					if (!hasActiveVersion && activeVersionId.equals(version.getGuid())) {
						hasActiveVersion = true;
					}
					if (version.getDate() > lastDate) {
						lastDate = version.getDate();
						lastVersionId = version.getGuid();
					}
				}
				lastVersion = lastVersionId;
				activeVersion = hasActiveVersion
					? activeVersionId
					: null;
				versionLookup = new BaseHostLookup() {
					
					
					final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					
					@Override
					public BaseObject baseGetLookupValue(final BaseObject key) {
						
						
						final BaseObject temp = this.fill(new BaseNativeObject());
						return temp.baseGet(key.baseToString(), BaseObject.UNDEFINED);
					}
					
					@Override
					public boolean baseHasKeysOwn() {
						
						
						return true;
					}
					
					@Override
					public Iterator<String> baseKeysOwn() {
						
						
						final List<String> keys = new ArrayList<>();
						keys.add("*");
						if (activeVersion != null) {
							keys.add("*" + activeVersion);
						}
						if (lastVersion != null && !lastVersion.equals(activeVersion)) {
							keys.add("*" + lastVersion);
						}
						keys.add("");
						this.keySet(keys, "*");
						return keys.iterator();
					}
					
					@Override
					public Iterator<? extends BasePrimitive<?>> baseKeysOwnPrimitive() {
						
						
						return Base.iteratorPrimitiveSafe(this.baseKeysOwn());
					}
					
					@Override
					public String toString() {
						
						
						return "[Lookup EntryVersions]";
					}
					
					private BaseObject fill(final BaseObject acceptor) {
						
						
						acceptor.baseDefine("*", FormEntryVersions.STR_CURRENT_VERSION);
						if (activeVersion != null) {
							acceptor.baseDefine("*" + activeVersion, FormEntryVersions.STR_ACTIVE_VERSION);
						}
						if (lastVersion != null && !lastVersion.equals(activeVersion)) {
							acceptor.baseDefine("*" + lastVersion, FormEntryVersions.STR_LAST_VERSION);
						}
						acceptor.baseDefine("", BaseString.EMPTY);
						this.fill(acceptor, "", "*");
						return acceptor;
					}
					
					private final void fill(final BaseObject acceptor, final String prefix, final String parentGuid) {
						
						
						for (final BaseVersion version : versions) {
							if (parentGuid.equals(version.getParentGuid())) {
								acceptor.baseDefine(
										version.getGuid(),
										Base.forString(prefix + '[' + this.sdf.format(new Date(version.getDate())) + "] " + version.getComment() + " / " + version.getTitle()));
								this.fill(acceptor, prefix + "· ", version.getGuid());
							}
						}
					}
					
					private final void keySet(final List<String> acceptor, final String parentGuid) {
						
						
						for (final BaseVersion version : versions) {
							if (parentGuid.equals(version.getParentGuid())) {
								acceptor.add(version.getGuid());
								this.keySet(acceptor, version.getGuid());
							}
						}
					}
				};
			} else {
				lastVersion = null;
				activeVersion = null;
				versionLookup = null;
			}
			this.fieldset = ControlFieldset.createFieldset().addField(
					ControlFieldFactory.createFieldString("version", MultivariantString.getString("Versions", Collections.singletonMap("ru", "Версии")), "*").setFieldType("select")
							.setFieldVariant("bigselect").setAttribute("lookup", versionLookup));
			this.setAttributeIntern("id", "select_version");
			this.setAttributeIntern("title", MultivariantString.getString("Version selection", Collections.singletonMap("ru", "Выбор версии")));
		} else {
			this.fieldset = ControlFieldset.createFieldset().addField(
					ControlFieldFactory.createFieldBoolean(
							"versioning",
							MultivariantString.getString("Activate version control for this object", Collections.singletonMap("ru", "Включить контроль версий для этого объекта")),
							true));
			this.setAttributeIntern("id", "activate_versioning");
			this.setAttributeIntern("title", MultivariantString.getString("Version control setup", Collections.singletonMap("ru", "Настройка контроля версий")));
		}
		this.setAttributeIntern("path", change.getLocationControl());
		if (lock != null) {
			this.setAttributeIntern("on_close", FormEntryVersions.CMD_CLOSE);
		}
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		
		
		if (command == FormEntryVersions.CMD_CLOSE) {
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		} else if (command == FormEntryVersions.CMD_VERSIONING) {
			final boolean versioning = Convert.MapEntry.toBoolean(this.getData(), "versioning", false);
			this.change.setVersioning(versioning);
			return new FormEntryEdit(this.plugin, this.change, this.lock);
		} else if (command == FormEntryVersions.CMD_EDIT || command == FormEntryVersions.CMD_EDIT_PUBLISHED) {
			final String versionId = Base.getString(this.getData(), "version", "*").trim();
			final BaseChange change = "*".equals(versionId)
				? this.change
				: this.change.getVersion(versionId.startsWith("*")
					? versionId.substring(1)
					: versionId);
			return new FormEntryEdit(this.plugin, change, this.lock);
		} else if (command == FormEntryVersions.CMD_ACTIVATE || command == FormEntryVersions.CMD_ACTIVATE_PUBLISHED) {
			final String versionId = Base.getString(this.getData(), "version", "*").trim();
			if (!"*".equals(versionId)) {
				final BaseChange change = this.change.getVersion(versionId.startsWith("*")
					? versionId.substring(1)
					: versionId);
				change.setCommitActive();
				change.commit();
			}
			return null;
		} else {
			throw new IllegalArgumentException("Unknown command: " + command.getKey());
		}
	}
	
	@Override
	public ControlCommandset getCommands() {
		
		
		final ControlCommandset result = Control.createOptions();
		if (this.change.getVersioning()) {
			result.add(this.published
				? FormEntryVersions.CMD_EDIT_PUBLISHED
				: FormEntryVersions.CMD_EDIT);
			result.add(this.published
				? FormEntryVersions.CMD_ACTIVATE_PUBLISHED
				: FormEntryVersions.CMD_ACTIVATE);
		} else {
			result.add(FormEntryVersions.CMD_VERSIONING);
		}
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		
		
		return this.fieldset;
	}
}
