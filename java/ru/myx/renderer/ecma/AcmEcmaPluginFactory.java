package ru.myx.renderer.ecma;

import ru.myx.ae1.AcmPluginFactory;
import ru.myx.ae1.PluginInstance;
import ru.myx.ae3.base.BaseObject;

/*
 * Created on 07.10.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
/**
 * @author myx
 *
 */
public final class AcmEcmaPluginFactory implements AcmPluginFactory {
	
	private static final String[] VARIETY = {
			"ACMMOD:ACM.ECMA", "ACM.ECMA"
	};

	@Override
	public final PluginInstance produce(final String variant, final BaseObject attributes, final Object source) {
		
		return new DummyPlugin();
	}

	@Override
	public final String[] variety() {
		
		return AcmEcmaPluginFactory.VARIETY;
	}
}
