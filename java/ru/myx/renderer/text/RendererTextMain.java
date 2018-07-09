package ru.myx.renderer.text;

import ru.myx.ae3.eval.Evaluate;
import ru.myx.ae3.produce.Produce;

/*
 * Created on 07.10.2003
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
public final class RendererTextMain {
	
	/**
	 * @param args
	 */
	public static final void main(final String[] args) {
		
		Produce.registerFactory(new AcmTextPluginFactory());
		Evaluate.registerLanguage(new AcmTextLanguageImpl());
	}
}
