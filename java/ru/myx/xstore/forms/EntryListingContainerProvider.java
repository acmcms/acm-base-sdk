/*
 * Created on 31.01.2006
 */
package ru.myx.xstore.forms;

import ru.myx.ae1.storage.BaseSync;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae3.base.BaseFunctionActAbstract;

final class EntryListingContainerProvider extends BaseFunctionActAbstract<Void, EntryListingContainer> {
	private final StorageImpl	parent;
	
	private final BaseSync		synchronization;
	
	EntryListingContainerProvider(final StorageImpl parent, final BaseSync synchronization) {
		super( Void.class, EntryListingContainer.class );
		this.parent = parent;
		this.synchronization = synchronization;
	}
	
	@Override
	public final EntryListingContainer apply(final Void argument) {
		return new EntryListingContainer( this.parent, this.synchronization );
	}
}
