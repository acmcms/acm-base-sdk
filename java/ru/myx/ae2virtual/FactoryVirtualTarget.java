/*
 * Created on 22.04.2006
 */
package ru.myx.ae2virtual;

import java.util.function.Function;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.flow.ObjectTarget;
import ru.myx.ae3.produce.ObjectFactory;
import ru.myx.ae3.serve.ServeRequest;

/**
 * @author myx
 *
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
final class FactoryVirtualTarget implements ObjectFactory<Object, ObjectTarget<ServeRequest>> {
	
	private static final Class<?>[] TARGETS = {
			ObjectTarget.class, Function.class
	};

	private final String id;

	private final String[] variety;

	private final PipeVirtual pipeVirtual;

	private static final Class<?>[] SOURCES = null;

	FactoryVirtualTarget(final String id, final PipeVirtual pipeVirtual) {
		this.id = id;
		this.variety = new String[]{
				id
		};
		this.pipeVirtual = pipeVirtual;
	}

	@Override
	public final boolean accepts(final String variant, final BaseObject attributes, final Class<?> source) {
		
		return true;
	}

	@Override
	public final ObjectTarget<ServeRequest> produce(final String variant, final BaseObject attributes, final Object source) {
		
		return this.pipeVirtual;
	}

	@Override
	public final Class<?>[] sources() {
		
		return FactoryVirtualTarget.SOURCES;
	}

	@Override
	public final Class<?>[] targets() {
		
		return FactoryVirtualTarget.TARGETS;
	}

	@Override
	public final String toString() {
		
		return "TargetVirtual, id=" + this.id;
	}

	@Override
	public final String[] variety() {
		
		return this.variety;
	}
}
