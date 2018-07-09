/*
 * Created on 26.05.2004
 */
package ru.myx.xstore.schedule;

import java.util.Collections;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.provide.ProvideRunner;
import ru.myx.ae1.schedule.Change;
import ru.myx.ae1.storage.BaseChange;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseFunctionActAbstract;
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
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
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public final class FormEntryEditSchedule extends AbstractForm<FormEntryEditSchedule> {

	private static final ControlFieldset<?> FIELDSET_LISTING = ControlFieldset.createFieldset()
			.addField(ControlFieldFactory.createFieldDate("date", MultivariantString.getString("Date", Collections.singletonMap("ru", "Дата")), 0L).setConstant())
			.addField(ControlFieldFactory.createFieldString("title", MultivariantString.getString("Description", Collections.singletonMap("ru", "Описание")), "").setConstant())
			.addField(ControlFieldFactory.createFieldString("name", MultivariantString.getString(
					"Name",
					Collections.singletonMap("ru", "Имя")), "").setConstant())
			.addField(ControlFieldFactory.createFieldOwner("owner", MultivariantString.getString(
					"Owner",
					Collections.singletonMap("ru", "Владелец"))).setConstant());
	
	private static final ControlCommand<?> CMD_SAVE = Control.createCommand("save", " OK ").setCommandPermission("publish").setCommandIcon("command-save");

	private static final ControlCommand<?> CMD_APPLY = Control.createCommand("apply", MultivariantString.getString("Apply", Collections.singletonMap("ru", "Применить")))
			.setCommandPermission("publish").setCommandIcon("command-apply");
	
	private final StorageImpl plugin;

	private final BaseEntry<?> entry;

	private final ControlFieldset<?> fieldset;

	private final ChangeListing schedule;

	/**
	 * @param plugin
	 * @param entry
	 */
	public FormEntryEditSchedule(final StorageImpl plugin, final BaseEntry<?> entry) {
		this.plugin = plugin;
		this.entry = entry;
		this.schedule = new ChangeListing(entry.getType(), (Change) plugin.getScheduling().createChange(entry.getGuid()));
		final BaseHostLookup commands;
		final ControlCommandset typeCommandList = entry.getType().getCommandsAdditional(entry, null, null, null);
		if (typeCommandList == null || typeCommandList.isEmpty()) {
			commands = ProvideRunner.getSchedulerTaskRunners(BaseEntry.class, null);
		} else {
			final ControlLookupStatic lookup = new ControlLookupStatic();
			for (final ControlCommand<?> command : typeCommandList) {
				lookup.putAppend(command.getKey(), Base.getString(command.getAttributes(), "title", command.getTitle()));
			}
			commands = ProvideRunner.getSchedulerTaskRunners(BaseEntry.class, lookup);
		}
		final ContainerEntrySchedule container = new ContainerEntrySchedule(entry, this.schedule, commands);
		this.fieldset = ControlFieldset.createFieldset();
		this.fieldset.addField(
				ControlFieldFactory.createFieldInteger("state", MultivariantString.getString("State", Collections.singletonMap("ru", "Статус")), entry.getState())
						.setFieldType("select").setAttribute("lookup", Helper.getLookupValidStates(entry)));
		this.fieldset.addField(
				Control.createFieldList("schedule", MultivariantString.getString("Schedule", Collections.singletonMap("ru", "Расписание")), null).setFieldHint(
						MultivariantString.getString(
								"User tasks ('*' in 'name' field) are ignored while object is in DRAFT state.",
								Collections.singletonMap("ru", "Пользовательские задачи ('*' в поле 'имя') будут проигнорированы если объект находится в состоянии 'черновик'.")))
						.setAttribute("content_fieldset", FormEntryEditSchedule.FIELDSET_LISTING)
						.setAttribute("content_handler", new BaseFunctionActAbstract<Object, ContainerEntrySchedule>(Object.class, ContainerEntrySchedule.class) {

							@Override
							public ContainerEntrySchedule apply(final Object arg) {

								return container;
							}
						}));
		this.setAttributeIntern("id", "entry_publishing");
		this.setAttributeIntern("title", MultivariantString.getString("Entry publishing and schedule", Collections.singletonMap("ru", "Объект: расписание и публикация")));
		this.setAttributeIntern("path", entry.getLocationControl());
		this.recalculate();
	}

	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) throws Exception {

		if (command == FormEntryEditSchedule.CMD_SAVE || command == FormEntryEditSchedule.CMD_APPLY) {
			final int state = Convert.MapEntry.toInt(this.getData(), "state", this.entry.getState());
			if (state != this.entry.getState()) {
				final BaseChange change = this.entry.createChange();
				change.setState(state);
				change.commit();
			}
			this.schedule.commit();
			return command == FormEntryEditSchedule.CMD_SAVE
				? null
				: new FormEntryEditSchedule(this.plugin, this.entry);
		}
		return super.getCommandResult(command, arguments);
	}

	@Override
	public ControlCommandset getCommands() {

		final ControlCommandset result = Control.createOptions();
		result.add(FormEntryEditSchedule.CMD_SAVE);
		result.add(FormEntryEditSchedule.CMD_APPLY);
		return result;
	}

	@Override
	public BaseObject getData() {

		final BaseObject data = super.getData();
		if (data != null) {
			data.baseDefine("schedule", Base.forArray(this.schedule.getListing()));
		}
		return data;
	}

	@Override
	public ControlFieldset<?> getFieldset() {

		return this.fieldset;
	}
}
