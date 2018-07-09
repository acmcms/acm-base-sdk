package ru.myx.ae1runners;

/*
 * Created on 21.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
import java.util.Collections;

import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.provide.TaskRunner;
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
// !!! добавить выбор скрипта!
public final class RunnerScript implements TaskRunner {
	
	
	private static final ControlFieldset<?> settingsDefinition = ControlFieldset.createFieldset()//
			.addField(//
					ControlFieldFactory
							.createFieldTemplate(//
									"template",
									MultivariantString.getString("Script", Collections.singletonMap("ru", "Скрипт")),
									"<%EXEC:ZZZ%>")//
							.setFieldHint(//
									MultivariantString.getString("ACM.TPL Script is default", Collections.singletonMap("ru", "Скрипт по умолчанию: ACM.TPL"))//
					)//
	);

	private static final Object TITLE = MultivariantString.getString("Script / ACM.TPL Script", Collections.singletonMap("ru", "Скрипт / Скрипт ACM.TPL"));

	@Override
	public String describe(final BaseObject settings) {
		
		
		return this.getTitle();
	}

	@Override
	public ControlFieldset<?> getFieldset() {
		
		
		return RunnerScript.settingsDefinition;
	}

	@Override
	public Class<?> getParameterClass() {
		
		
		return null;
	}

	@Override
	public String getTitle() {
		
		
		return RunnerScript.TITLE.toString();
	}

	@Override
	public Object run(final ExecProcess ctx, final BaseObject settings) throws Throwable {
		
		
		final String template = Base.getString(settings, "template", "");
		Context.getServer(ctx).createRenderer("RUNNER/SCRIPT", template)//
				.callVE0(ctx, BaseObject.UNDEFINED);
		return null;
	}
}
