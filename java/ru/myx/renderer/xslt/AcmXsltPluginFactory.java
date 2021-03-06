package ru.myx.renderer.xslt;

import ru.myx.ae1.AcmPluginFactory;
import ru.myx.ae1.PluginInstance;
import ru.myx.ae3.base.BaseObject;

/*
 * Created on 07.10.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
final class AcmXsltPluginFactory implements AcmPluginFactory {
	
	private static final String[] VARIETY = {
			"ACMMOD:ACM.XSLT", "ACMMOD:XSLT", "ACMMOD:XSL"
	};

	@Override
	public final PluginInstance produce(final String variant, final BaseObject attributes, final Object source) {
		
		return new DummyPlugin();
	}

	@Override
	public final String[] variety() {
		
		return AcmXsltPluginFactory.VARIETY;
	}
}
