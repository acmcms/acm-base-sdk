/*
 * Created on 22.04.2006
 */
package ru.myx.ae2virtual;

import java.util.ConcurrentModificationException;

import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.flow.ObjectSource;
import ru.myx.ae3.produce.ObjectFactory;
import ru.myx.ae3.produce.Produce;
import ru.myx.ae3.serve.ServeRequest;

/** @author myx */
public final class FactoryVirtualSource implements ObjectFactory<Object, ServeRequest> {

	private static final Class<?>[] TARGETS = {
			ServeRequest.class
	};
	
	private static final Class<?>[] SOURCES = null;
	
	private static final String[] VARIETY = {
			"VIRTUAL"
	};
	
	@Override
	public final boolean accepts(final String variant, final BaseObject attributes, final Class<?> source) {

		return !Base.getString(attributes, "id", "").isBlank();
	}
	
	@Override
	public final ObjectSource<ServeRequest> prepare(final String type, final BaseObject attributes, final Object context) {

		final String id = Base.getString(attributes, "id", null);
		try {
			final PipeVirtual virtual = Pack.VIRTUALS.get(id);
			if (virtual != null) {
				return virtual;
			}
		} catch (final ConcurrentModificationException t) {
			// ignore
		}
		synchronized (Pack.VIRTUALS) {
			{
				final PipeVirtual virtual = Pack.VIRTUALS.get(id);
				if (virtual != null) {
					return virtual;
				}
			}
			final PipeVirtual created = new PipeVirtual();
			Pack.VIRTUALS.put(id, created);
			Produce.registerFactory(new FactoryVirtualTarget(id, created));
			return created;
		}
	}
	
	@Override
	public final ServeRequest produce(final String variant, final BaseObject attributes, final Object source) {

		final ObjectSource<ServeRequest> channel = this.prepare(variant, attributes, source);
		while (!channel.isReady() && !channel.isExhausted()) {
			try {
				synchronized (channel) {
					channel.wait(50L);
				}
			} catch (final InterruptedException e) {
				return null;
			}
		}
		return channel.isExhausted()
			? null
			: channel.next();
	}
	
	@Override
	public final Class<?>[] sources() {

		return FactoryVirtualSource.SOURCES;
	}
	
	@Override
	public final Class<?>[] targets() {

		return FactoryVirtualSource.TARGETS;
	}
	
	@Override
	public final String[] variety() {

		return FactoryVirtualSource.VARIETY;
	}
}
