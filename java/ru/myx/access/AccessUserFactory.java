package ru.myx.access;

import ru.myx.ae1.access.AccessUser;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.produce.ObjectFactory;

/*
 * Created on 07.10.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
/**
 * @author myx
 *
 */
public final class AccessUserFactory implements ObjectFactory<String, AccessUser<?>> {
	
	private static final String[] VARIETY = {
			"USER", "UM-USER"
	};

	private static final Class<?>[] TARGETS = {
			AccessUser.class
	};

	private static final Class<?>[] SOURCES = {
			String.class
	};

	@Override
	public final boolean accepts(final String variant, final BaseObject attributes, final Class<?> source) {
		
		return true;
	}

	@Override
	public final AccessUser<?> produce(final String variant, final BaseObject attributes, final String source) {
		
		return Context.getServer(Exec.currentProcess()).getAccessManager().getUser(source.startsWith("AE1:")
			? source.substring(4)
			: source, false);
	}

	@Override
	public final Class<?>[] sources() {
		
		return AccessUserFactory.SOURCES;
	}

	@Override
	public final Class<?>[] targets() {
		
		return AccessUserFactory.TARGETS;
	}

	@Override
	public final String[] variety() {
		
		return AccessUserFactory.VARIETY;
	}
}
