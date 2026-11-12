package ru.myx.renderer.ecma;

import java.util.Properties;

import ru.myx.ae1.PluginInstance;
import ru.myx.ae1.know.Server;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.exec.Exec;

/*
 * Created on 05.05.2006
 */
/*
 * Distinct from its sibling renderer plugins: it carries no renderer of its own.
 * The ACM.ECMA language engine is registered by RendererEcmaMain, whose class
 * lives in ae3.sdk, so destroy/setup/start are deliberately empty and register()
 * does the only real job, marking ACM.ECMA a server default renderer.
 */
final class DummyPlugin implements PluginInstance {
	@Override
	public final void destroy() {
		// empty
	}
	
	@Override
	public final void register() {
		Context.getServer( Exec.currentProcess() ).registerRendererDefault( "ACM.ECMA" );
	}
	
	@Override
	public final void setup(final Server server, final Properties info) throws IllegalArgumentException {
		// empty
	}
	
	@Override
	public final void start() {
		// empty
	}
	
	@Override
	public String toString() {
		return "ACM:TPL dummy plugin";
	}
}
