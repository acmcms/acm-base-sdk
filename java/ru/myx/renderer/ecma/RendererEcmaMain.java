package ru.myx.renderer.ecma;

import ru.myx.ae3.eval.Evaluate;
import ru.myx.ae3.produce.Produce;

/**
 * 
 * TODO: WARNING: not used until put in separate module
 * 
 * 
 * @author myx
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public final class RendererEcmaMain {
	
	/**
	 * @param args
	 */
	public static final void main(final String[] args) {
		Produce.registerFactory( new AcmEcmaPluginFactory() );
		/**
		 * !!! Temporary initialized in Evaluate
		 */
		if (Evaluate.getLanguageImpl( "ECMA" ) == null) {
			Evaluate.registerLanguage( AcmEcmaLanguageImpl.INSTANCE );
		}
	}
}
