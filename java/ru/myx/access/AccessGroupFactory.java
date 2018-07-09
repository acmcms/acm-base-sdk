package ru.myx.access;

import ru.myx.ae1.access.AccessGroup;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.produce.ObjectFactory;

/*
 * Created on 07.10.2003 To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
/**
 * @author myx
 *
 */
public final class AccessGroupFactory implements ObjectFactory<String, AccessGroup<?>> {
	
	private static final String[] VARIETY = {
			"GROUP", "UM-GROUP"
	};

	private static final Class<?>[] TARGETS = {
			AccessGroup.class
	};

	private static final Class<?>[] SOURCES = {
			String.class
	};
	
	@Override
	public final boolean accepts(final String variant, final BaseObject attributes, final Class<?> source) {
		
		return true;
	}
	
	@Override
	public final AccessGroup<?> produce(final String variant, final BaseObject attributes, final String source) {
		
		return Context.getServer(Exec.currentProcess()).getAccessManager().getGroup(source.startsWith("AE1:")
			? source.substring(4)
			: source, false);
	}
	
	@Override
	public final Class<?>[] sources() {
		
		return AccessGroupFactory.SOURCES;
	}
	
	@Override
	public final Class<?>[] targets() {
		
		return AccessGroupFactory.TARGETS;
	}
	
	@Override
	public final String[] variety() {
		
		return AccessGroupFactory.VARIETY;
	}
}
