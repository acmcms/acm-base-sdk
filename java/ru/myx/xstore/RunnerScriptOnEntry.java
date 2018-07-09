package ru.myx.xstore;

/*
 * Created on 27.05.2004
 */
import java.util.Collections;

import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.provide.TaskRunner;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.exec.ExecProcess;

/**
 * @author myx
 *
 */
public final class RunnerScriptOnEntry implements TaskRunner {
	
	
	private static final ControlFieldset<?> settingsDefinition = ControlFieldset.createFieldset().addField(
			ControlFieldFactory.createFieldTemplate("template", MultivariantString.getString("Script", Collections.singletonMap("ru", "Скрипт")), "<%EXEC:ZZZ%>").setFieldHint(
					MultivariantString
							.getString("'this' variable contains current storage object\r\n'Storage' variable contains current storage interface", Collections.singletonMap(
									"ru",
									"В переменной 'this' содержится текущий объект хранилища\r\nВ переменной 'Storage' содержится текущий интерфейс хранилища"))));

	private static final Object TITLE = MultivariantString.getString("Script execution ACM.TPL", Collections.singletonMap("ru", "Выполнение скрипта ACM.TPL"));

	@Override
	public String describe(final BaseObject settings) {
		
		
		return this.getTitle();
	}

	@Override
	public ControlFieldset<?> getFieldset() {
		
		
		return RunnerScriptOnEntry.settingsDefinition;
	}

	@Override
	public Class<?> getParameterClass() {
		
		
		return BaseEntry.class;
	}

	@Override
	public String getTitle() {
		
		
		return RunnerScriptOnEntry.TITLE.toString();
	}

	@Override
	public Object run(final ExecProcess ctx, final BaseObject settings) throws Throwable {
		
		
		final String template = Base.getString(settings, "template", "");
		Context.getServer(ctx).createRenderer("RUNNER/SCRIPT/ENTRY", template)//
				.callVE0(ctx, BaseObject.UNDEFINED);
		return null;
	}
}
