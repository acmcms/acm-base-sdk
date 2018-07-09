package ru.myx.xstore;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import ru.myx.ae1.control.ControlEntry;
import ru.myx.ae1.know.Server;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.PluginRegistry;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.ControlBasic;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.produce.ObjectFactory;
import ru.myx.ae3.serve.ServeRequest;

/*
 * Created on 14.11.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
/**
 * @author myx
 *
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public final class FinderFactory implements ObjectFactory<String, ControlEntry<?>> {
	
	private static final Class<?>[] TARGETS = {
			ControlEntry.class
	};

	private static final String[] VARIETY = {
			"storage"
	};

	private static final Class<?>[] SOURCES = {
			String.class
	};

	private final static BaseEntry<?> checkSource(final StorageImpl parent, final String guid, final Server server, final boolean all) {
		
		List<BaseEntry<?>> result = null;
		{
			final Collection<ControlBasic<?>> entries = parent.getStorage().searchForIdentity(guid, false);
			if (entries != null && entries.size() > 0) {
				for (final ControlBasic<?> basic : entries) {
					final BaseEntry<?> entry = (BaseEntry<?>) basic;
					if (entry != null && (all || entry.getState() > 0)) {
						if (entry.getGuid().equals(guid)) {
							return entry;
						}
						if (result == null) {
							result = new ArrayList<>();
						}
						result.add(entry);
					}
				}
			}
		}
		if (result == null) {
			return null;
		}
		if (result.size() == 1) {
			return result.get(0);
		}
		{
			final String controlBase = server.getControlBase();
			for (final BaseEntry<?> entry : result) {
				if (entry == null) {
					continue;
				}
				final String location = entry.getLocationControl();
				if (location != null && location.startsWith(controlBase)) {
					return entry;
				}
			}
		}
		{
			final ServeRequest query = Context.getRequest(Exec.currentProcess());
			if (query != null) {
				final String target = query.getTarget();
				if (target != null) {
					for (final BaseEntry<?> entry : result) {
						if (entry == null) {
							continue;
						}
						final URL url;
						try {
							url = new URL(entry.getLocationAbsolute());
						} catch (final MalformedURLException e) {
							continue;
						}
						if (target.equals(url.getHost())) {
							return entry;
						}
					}
				}
			}
		}
		{
			for (final BaseEntry<?> entry : result) {
				if (entry != null) {
					return entry;
				}
			}
		}
		return null;
	}

	@Override
	public final boolean accepts(final String variant, final BaseObject attributes, final Class<?> source) {
		
		return true;
	}

	@Override
	public final ControlEntry<?> produce(final String variant, final BaseObject attributes, final String source) {
		
		final String id = String.valueOf(source).trim();
		final int pos = id.indexOf(',');
		if (pos == -1) {
			return null;
		}
		final String storageName = id.substring(0, pos).trim();
		if (storageName == null) {
			return null;
		}
		final String guid = id.substring(pos + 1).trim();
		{
			final Server server = Context.getServer(Exec.currentProcess());
			final StorageImpl plugin = PluginRegistry.getPlugin(server, storageName);
			if (plugin != null) {
				final BaseEntry<?> check1 = FinderFactory.checkSource(plugin, guid, server, attributes == null);
				if (check1 != null) {
					return check1;
				}
			}
			{
				final Set<StorageImpl> plugins = PluginRegistry.getPlugins(server);
				if (plugins != null && !plugins.isEmpty()) {
					for (final StorageImpl parent : plugins) {
						if (parent != plugin) {
							final ControlEntry<?> check1 = FinderFactory.checkSource(parent, guid, server, attributes == null);
							if (check1 != null) {
								return check1;
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public final Class<?>[] sources() {
		
		return FinderFactory.SOURCES;
	}

	@Override
	public final Class<?>[] targets() {
		
		return FinderFactory.TARGETS;
	}

	@Override
	public final String[] variety() {
		
		return FinderFactory.VARIETY;
	}
}
