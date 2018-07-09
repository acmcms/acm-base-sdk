package ru.myx.renderer.text;

import java.util.Properties;

import ru.myx.ae1.PluginInstance;
import ru.myx.ae1.know.Server;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.exec.Exec;

/*
 * Created on 05.05.2006
 */
final class DummyPlugin implements PluginInstance {
	@Override
	public final void destroy() {
		// empty
	}
	
	@Override
	public final void register() {
		Context.getServer( Exec.currentProcess() ).registerRendererDefault( "ACM.TEXT" );
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
		return "ACM:TEXT dummy plugin";
	}
}
