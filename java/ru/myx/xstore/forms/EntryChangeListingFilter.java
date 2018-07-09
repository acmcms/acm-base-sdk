/*
 * Created on 31.01.2006
 */
package ru.myx.xstore.forms;

import java.util.AbstractList;
import java.util.List;

import ru.myx.ae1.storage.BaseChange;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.ControlBasic;
import ru.myx.ae3.control.FilterBasic;
import ru.myx.ae3.control.fieldset.ControlFieldset;

final class EntryChangeListingFilter extends AbstractList<ControlBasic<?>> {
	static final class EntryFilter extends FilterBasic<EntryFilter, BaseEntry<?>> {
		final ControlFieldset<?>	listing;
		
		final BaseEntry<?>			parent;
		
		final BaseChange			root;
		
		EntryFilter(final ControlFieldset<?> listing, final BaseEntry<?> parent, final BaseChange root) {
			super( parent );
			this.listing = listing;
			this.parent = parent;
			this.root = root;
		}
		
		@Override
		public BaseObject getData() {
			final BaseObject data = new BaseNativeObject()//
					.putAppend( "$path", this.getPath( this.parent.getParent(), "" ) )//
			;
			this.listing.dataRetrieve( this.parent.getData(), data );
			return data;
		}
		
		@Override
		public String getKey() {
			return this.parent.getGuid();
		}
		
		final String getPath(final BaseEntry<?> entry, final String suffix) {
			if (entry == null) {
				return "DEAD / LOST";
			}
			final BaseEntry<?> parent = entry.getParent();
			if (parent == null || parent == this.root || parent.getGuid().equals( this.root.getGuid() )) {
				return entry.getTitle() + suffix;
			}
			return this.getPath( parent, " / " + entry.getTitle() + suffix );
		}
	}
	
	private final ControlFieldset<?>	listing;
	
	private final List<ControlBasic<?>>	parent;
	
	private final BaseChange			root;
	
	EntryChangeListingFilter(final ControlFieldset<?> listing, final List<ControlBasic<?>> parent, final BaseChange root) {
		this.listing = listing;
		this.parent = parent;
		this.root = root;
	}
	
	@Override
	public ControlBasic<?> get(final int index) {
		return new EntryFilter( this.listing, (BaseEntry<?>) this.parent.get( index ), this.root );
	}
	
	@Override
	public int size() {
		if (this.parent == null) {
			return 0;
		}
		return this.parent.size();
	}
}
