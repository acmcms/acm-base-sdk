/*
 * Created on 11.09.2005
 */
package ru.myx.renderer.nothing;

import java.util.function.Function;
import ru.myx.ae3.eval.CompileTargetMode;
import ru.myx.ae3.eval.Evaluate;
import ru.myx.ae3.eval.LanguageImpl;
import ru.myx.ae3.exec.ModifierArgumentA30IMM;
import ru.myx.ae3.exec.OperationsA10;
import ru.myx.ae3.exec.ProgramAssembly;
import ru.myx.ae3.exec.ResultHandler;

/**
 * @author myx
 *
 */
public final class AcmNullLanguageImpl implements LanguageImpl {
	
	@Override
	public void compile(final String identity, final Function<String, String> folder, final String name, final ProgramAssembly assembly, final CompileTargetMode mode)
			throws Evaluate.CompilationException {
		
		if (mode != CompileTargetMode.INLINE) {
			assembly.addInstruction(OperationsA10.XFLOAD_P.instruction(ModifierArgumentA30IMM.NULL, 0, ResultHandler.FC_PNN_RET));
		}
	}
	
	@Override
	public String[] getAssociatedAliases() {
		
		return new String[]{
				//
				"ACM.NULL", //
				"NULL", //
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
		
		return "NULL";
	}
}
