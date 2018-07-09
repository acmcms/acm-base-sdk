/*
 * Created on 11.09.2005
 */
package ru.myx.renderer.base;

import java.util.function.Function;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.eval.CompileTargetMode;
import ru.myx.ae3.eval.Evaluate;
import ru.myx.ae3.eval.LanguageImpl;
import ru.myx.ae3.exec.OperationsA10;
import ru.myx.ae3.exec.ProgramAssembly;
import ru.myx.ae3.exec.ResultHandler;
import ru.myx.ae3.xml.Xml;

/**
 * @author myx
 *
 */
final class AcmBaseLanguageImpl implements LanguageImpl {
	
	@Override
	public final void compile(final String identity, final Function<String, String> folder, final String name, final ProgramAssembly assembly, final CompileTargetMode mode)
			throws Evaluate.CompilationException {
		
		final String source = folder.apply(name);
		final BaseObject map;
		if (source == null) {
			map = BaseObject.UNDEFINED;
		} else {
			map = Xml.toBase(identity, source, null, null, null);
		}
		assembly.addInstruction(OperationsA10.XFLOAD_P.instruction(map, null, 0, ResultHandler.FA_BNN_NXT));
	}

	@Override
	public String[] getAssociatedAliases() {
		
		return new String[]{
				//
				"BASE", //
				"ACM.BASE", //
				"XML.BASE", //
				"XML->BASE", //
				"XML->BASE_OBJECT", //
		};
	}

	@Override
	public String[] getAssociatedExtensions() {
		
		return null;
	}

	@Override
	public String[] getAssociatedMimeTypes() {
		
		return null;
	}

	@Override
	public String getKey() {
		
		return "BASE";
	}
}
