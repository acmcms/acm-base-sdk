/*
 * Created on 21.06.2004
 */
package ru.myx.xstore.basics;

import java.util.Collections;
import java.util.Set;

import ru.myx.ae1.storage.BaseRecycled;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.help.Convert;

/**
 * @author myx
 * 		
 */
public final class Clipboard {
	
	private static final Set<String> EMPTY_STRING = Collections.emptySet();
	
	private static final Set<BaseRecycled> EMPTY_RECYCLED = Collections.emptySet();
	
	static final Clipboard EMPTY = new Clipboard(false, Clipboard.EMPTY_STRING);
	
	/**
	 * @param parent
	 * @return clipboard
	 */
	public static Clipboard getClipboard(final StorageImpl parent) {
		
		return (Clipboard) Convert.Any
				.toObject(Context.getSessionData(Exec.currentProcess()).baseGet(parent.getMnemonicName() + "_clipboard", BaseObject.UNDEFINED).baseValue(), Clipboard.EMPTY);
	}
	
	/**
	 * @param parent
	 * @param clipboard
	 * @return same clipboard
	 */
	public static Clipboard setClipboard(final StorageImpl parent, final Clipboard clipboard) {
		
		Context.getSessionData(Exec.currentProcess()).baseDefine(parent.getMnemonicName() + "_clipboard", Base.forUnknown(clipboard));
		return clipboard;
	}
	
	/**
	 * 
	 */
	public final boolean restore;
	
	/**
	 * 
	 */
	public final boolean move;
	
	/**
	 * 
	 */
	public final Set<String> objects;
	
	/**
	 * 
	 */
	public final Set<BaseRecycled> recycled;
	
	/**
	 * @param move
	 * @param objects
	 */
	public Clipboard(final boolean move, final Set<String> objects) {
		this.move = move;
		this.restore = false;
		this.objects = objects;
		this.recycled = Clipboard.EMPTY_RECYCLED;
	}
	
	/**
	 * @param objects
	 */
	public Clipboard(final Set<BaseRecycled> objects) {
		this.move = false;
		this.restore = true;
		this.objects = Clipboard.EMPTY_STRING;
		this.recycled = objects;
	}
	
	/**
	 * @return boolean
	 */
	public boolean isEmpty() {
		
		return this.restore
			? this.recycled.isEmpty()
			: this.objects.isEmpty();
	}
}
