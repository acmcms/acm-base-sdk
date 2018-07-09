package ru.myx.xstore.basics;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.ControlNode;
import ru.myx.ae1.know.Server;
import java.util.function.Function;
import ru.myx.ae3.control.ControlActor;

/**
 * @author myx
 *
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public final class StorageSynchronizationActorProvider implements Function<String, ControlActor<?>> {
	
	private final String prefix;

	private final Server server;

	/**
	 * @param server
	 * @param prefix
	 */
	public StorageSynchronizationActorProvider(final Server server, final String prefix) {
		this.prefix = prefix;
		this.server = server;
	}

	@Override
	public final ControlActor<?> apply(final String arg) {
		
		if (arg == null || !arg.startsWith(this.prefix)) {
			return null;
		}
		final ControlNode<?> node = Control.relativeNode(this.server.getControlRoot(), arg);
		if (node instanceof ControlNodeImpl) {
			return new CommonActor(((ControlNodeImpl) node).getEntry());
		}
		return null;
	}
}
