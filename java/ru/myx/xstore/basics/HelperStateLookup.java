/*
 * Created on 26.06.2005
 */
package ru.myx.xstore.basics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.ModuleInterface;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BasePrimitive;

final class HelperStateLookup extends BaseHostLookup {
	
	
	private static final BaseObject ALL_STATES = HelperStateLookup.fillImpl();
	
	private static final BaseObject fillImpl() {
		
		
		final BaseObject result = new BaseNativeObject()//
				.putAppend("" + ModuleInterface.STATE_DRAFT, MultivariantString.getString("Draft", Collections.singletonMap("ru", "Черновик")))//
				.putAppend("" + ModuleInterface.STATE_READY, MultivariantString.getString("Ready", Collections.singletonMap("ru", "Готов")))//
				.putAppend("" + ModuleInterface.STATE_SYSTEM, MultivariantString.getString("System", Collections.singletonMap("ru", "Системный")))//
				.putAppend("" + ModuleInterface.STATE_PUBLISHED, MultivariantString.getString("Published", Collections.singletonMap("ru", "Опубликованный")))//
				.putAppend("" + ModuleInterface.STATE_ARCHIEVED, MultivariantString.getString("Archive", Collections.singletonMap("ru", "Архивный")))//
				.putAppend("" + ModuleInterface.STATE_DEAD, MultivariantString.getString("Dead", Collections.singletonMap("ru", "Устаревший")));
		return result;
	}
	
	private final Collection<Integer> filter;
	
	HelperStateLookup(final Collection<Integer> filter) {
		this.filter = filter;
	}
	
	@Override
	public final BaseObject baseGetLookupValue(final BaseObject key) {
		
		
		return HelperStateLookup.ALL_STATES.baseGet(key.baseToJavaString(), key);
	}
	
	@Override
	public Iterator<String> baseKeysOwn() {
		
		
		final List<String> result = new ArrayList<>();
		if (this.filter == null || this.filter.contains(new Integer(ModuleInterface.STATE_DRAFT))) {
			result.add("" + ModuleInterface.STATE_DRAFT);
		}
		if (this.filter == null || this.filter.contains(new Integer(ModuleInterface.STATE_READY))) {
			result.add("" + ModuleInterface.STATE_READY);
		}
		if (this.filter == null || this.filter.contains(new Integer(ModuleInterface.STATE_SYSTEM))) {
			result.add("" + ModuleInterface.STATE_SYSTEM);
		}
		if (this.filter == null || this.filter.contains(new Integer(ModuleInterface.STATE_PUBLISHED))) {
			result.add("" + ModuleInterface.STATE_PUBLISHED);
		}
		if (this.filter == null || this.filter.contains(new Integer(ModuleInterface.STATE_ARCHIEVED))) {
			result.add("" + ModuleInterface.STATE_ARCHIEVED);
		}
		if (this.filter == null || this.filter.contains(new Integer(ModuleInterface.STATE_DEAD))) {
			result.add("" + ModuleInterface.STATE_DEAD);
		}
		return result.iterator();
	}
	
	@Override
	public Iterator<? extends CharSequence> baseKeysOwnAll() {
		
		
		return this.baseKeysOwn();
	}
	
	@Override
	public Iterator<? extends BasePrimitive<?>> baseKeysOwnPrimitive() {
		
		
		return Base.iteratorPrimitiveSafe(this.baseKeysOwn());
	}
	
	@Override
	public String toString() {
		
		
		return "[Lookup: Storage Helper: State]";
	}
}
