/*
 * Created on 11.09.2005
 */
package ru.myx.renderer.map;

import java.util.function.Function;
import ru.myx.ae3.base.BaseNativeObject;
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
final class AcmMapLanguageImpl implements LanguageImpl {
	
	@Override
	public final void compile(final String identity, final Function<String, String> folder, final String name, final ProgramAssembly assembly, final CompileTargetMode mode)
			throws Evaluate.CompilationException {
		
		final String source = folder.apply(name);
		final BaseNativeObject map = new BaseNativeObject();
		if (source != null) {
			Xml.toMap(identity, source, null, map, null, null);
		}
		assembly.addInstruction(OperationsA10.XFLOAD_P.instruction(map, null, 0, ResultHandler.FA_BNN_NXT));
	}

	@Override
	public String[] getAssociatedAliases() {
		
		return new String[]{
				//
				"MAP", //
				"ACM.MAP", //
				"XML.MAP", //
				"XML->MAP", //
				"XML->MAP", //
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
		
		return "ACM.MAP";
	}
}
