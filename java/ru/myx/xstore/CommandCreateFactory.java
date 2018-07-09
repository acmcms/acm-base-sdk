package ru.myx.xstore;

import java.util.Collections;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseChange;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.PluginRegistry;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae1.types.Type;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractContainer;
import ru.myx.ae3.control.ControlContainer;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.produce.ObjectFactory;
import ru.myx.xstore.forms.FormEntryCreate;

/*
 * Created on 14.11.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
/**
 * @author myx
 *
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public final class CommandCreateFactory implements ObjectFactory<Object, ControlContainer<?>> {
	
	static final class CreateContainer extends AbstractContainer<CreateContainer> {
		
		private final StorageImpl parent;

		private final String sectionId;

		private final String sectionTitle;

		private final String template;

		CreateContainer(final StorageImpl parent, final String sectionId, final String sectionTitle, final String template) {
			this.parent = parent;
			this.sectionId = sectionId;
			this.sectionTitle = sectionTitle;
			this.template = template;
		}

		@Override
		public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
			
			final BaseEntry<?> folder = this.parent.getStorage().getByGuid(this.sectionId);
			final BaseChange change = folder.createChild();
			return new FormEntryCreate(this.parent, change, this.template, false, false);
		}

		@Override
		public ControlCommandset getCommands() {
			
			final Type<?> type = Context.getServer(Exec.currentProcess()).getTypes().getType(this.template);
			final Object typeName = type == null
				? this.template
				: type.getTitle();
			return Control.createOptionsSingleton(
					Control.createCommand(
							"quick",
							MultivariantString.getString(
									"<b>Create </b>" + typeName + "<b> in </b>" + this.sectionTitle + "...",
									Collections.singletonMap("ru", "<b>Создать </b>" + typeName + "<b> в разделе </b>" + this.sectionTitle + "...")))
							.setCommandPermission("control, edit").setCommandIcon("command-next").setAttribute(
									"description",
									MultivariantString.getString(
											"Create new entry based on the choosen template.",
											Collections.singletonMap("ru", "Создание нового объекта определенного типа."))));
		}
	}

	private static final Class<?>[] TARGETS = {
			ControlContainer.class
	};

	private static final String[] VARIETY = {
			"XDS_COMMAND_CREATE_OBJECT"
	};

	@Override
	public final boolean accepts(final String variant, final BaseObject attributes, final Class<?> source) {
		
		return true;
	}

	@Override
	public final ControlContainer<?> produce(final String variant, final BaseObject attributes, final Object source) {
		
		final String storageName = Base.getString(attributes, "storage", "").trim();
		if (storageName.length() == 0) {
			return null;
		}
		final StorageImpl parent = PluginRegistry.getPlugin(Context.getServer(Exec.currentProcess()), storageName);
		if (parent == null) {
			return null;
		}
		final String sectionId = Base.getString(attributes, "sectionId", "").trim();
		if (sectionId.length() == 0) {
			return null;
		}
		final BaseEntry<?> section = parent.getStorage().getByGuid(sectionId);
		if (section == null) {
			return null;
		}
		final String parentTitle;
		final String parentId = section.getParentGuid();
		if (parentId.length() == 1 && parentId.charAt(0) == '*' || parentId.startsWith("$$")) {
			parentTitle = "";
		} else {
			final BaseEntry<?> parentSection = parent.getStorage().getByGuid(parentId);
			if (parentSection == null) {
				parentTitle = "??? / ";
			} else {
				parentTitle = parentSection.getTitle() + " / ";
			}
		}
		return new CreateContainer(parent, sectionId, parentTitle + section.getTitle(), Base.getString(attributes, "template", ""));
	}

	@Override
	public final Class<?>[] sources() {
		
		return null;
	}

	@Override
	public final Class<?>[] targets() {
		
		return CommandCreateFactory.TARGETS;
	}

	@Override
	public final String[] variety() {
		
		return CommandCreateFactory.VARIETY;
	}
}
