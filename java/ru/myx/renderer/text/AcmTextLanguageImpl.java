/*
 * Created on 11.09.2005
 */
package ru.myx.renderer.text;

import java.util.function.Function;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.eval.CompileTargetMode;
import ru.myx.ae3.eval.Evaluate;
import ru.myx.ae3.eval.LanguageImpl;
import ru.myx.ae3.exec.OperationsA10;
import ru.myx.ae3.exec.ProgramAssembly;
import ru.myx.ae3.exec.ResultHandler;

/**
 * @author myx
 *
 */
public final class AcmTextLanguageImpl implements LanguageImpl {
	
	@Override
	public final void compile(final String identity, final Function<String, String> folder, final String name, final ProgramAssembly assembly, final CompileTargetMode mode)
			throws Evaluate.CompilationException {
		
		final String source = folder.apply(name);
		if (source == null) {
			return;
		}
		assembly.addInstruction(OperationsA10.XFLOAD_P.instruction(Base.forString(source.trim()), null, 0, ResultHandler.FA_BNN_NXT));
	}

	@Override
	public String[] getAssociatedAliases() {
		
		return new String[]{
				//
				"TEXT", //
				"ACM.TEXT", //
				"TXT", //
				"ACM.TXT", //
				"SOURCE", //
				"SOURCE_CODE", //
				"EXACT", //
		};
	}

	@Override
	public String[] getAssociatedExtensions() {
		
		return new String[]{
				//
				".txt", //
				".text", //
		};
	}

	@Override
	public String[] getAssociatedMimeTypes() {
		
		return new String[]{
				//
				"text/plain", //
		};
	}

	@Override
	public String getKey() {
		
		return "TEXT";
	}

}
