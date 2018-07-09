/*
 * Created on 26.06.2005
 */
package ru.myx.xstore.basics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.myx.ae1.types.Type;
import ru.myx.ae1.types.TypeRegistry;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BasePrimitive;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.help.Create;

final class HelperTypeLookup extends BaseHostLookup {
	
	
	private final Type<?> type;
	
	HelperTypeLookup(final Type<?> type) {
		assert type == null || type.getValidChildrenTypeNames() != null || type.getValidParentsTypeNames() != null;
		this.type = type;
	}
	
	@Override
	public final BaseObject baseGetLookupValue(final BaseObject key) {
		
		
		final Type<?> type = Context.getServer(Exec.currentProcess()).getTypes().getType(String.valueOf(key));
		return type == null
			? key
			: Base.forString(type.getTitle());
	}
	
	@Override
	public Iterator<String> baseKeysOwn() {
		
		
		final TypeRegistry registry = Context.getServer(Exec.currentProcess()).getTypes();
		final String[] typeNames = registry.getTypeNames();
		if (typeNames != null) {
			Map<String, Type<?>> temporary = null;
			checkNext : for (final String typeName : typeNames) {
				final Type<?> type = registry.getType(typeName);
				if (type == null || !type.isClientVisible()) {
					continue checkNext;
				}
				if (this.type != null) {
					{
						/**
						 * If 'current' type specified, we'd like to see only
						 * ones who allow it as a valid parent.
						 */
						final Collection<String> parents = type.getValidParentsTypeNames();
						/**
						 * If no parents specified we're assuming that any are
						 * welcome.
						 */
						if (parents != null) {
							boolean found = false;
							for (final String current : parents) {
								if (this.type.isInstance(current)) {
									found = true;
									break;
								}
							}
							if (!found) {
								continue checkNext;
							}
						}
					}
					{
						/**
						 * now we're gonna check for allowed children types (if
						 * any).
						 */
						final Collection<String> children = this.type.getValidChildrenTypeNames();
						/**
						 * If no children specified we're assuming that any are
						 * welcome.
						 */
						if (children != null) {
							boolean found = false;
							for (final String current : children) {
								if (type.isInstance(current)) {
									found = true;
									break;
								}
							}
							if (!found) {
								continue checkNext;
							}
						}
					}
				}
				if (temporary == null) {
					temporary = Create.tempMap();
				}
				temporary.put(type.getTitle() + " : " + typeName, type);
			}
			if (temporary != null) {
				final List<String> result = new ArrayList<>();
				for (final Type<?> type : temporary.values()) {
					result.add(type.getKey());
				}
				return result.iterator();
			}
		}
		return BaseObject.ITERATOR_EMPTY;
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
		
		
		return "[Lookup: Storage Helper: Types]";
	}
}
