package ru.myx.xstore.basics;

import java.util.Collections;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractActor;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.jdbc.lock.Locker;
import ru.myx.xstore.forms.FormEntrySynchronization;
import ru.myx.xstore.forms.FormLocked;

/**
 * @author myx
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
final class CommonActor extends AbstractActor<CommonActor> {
	private static final ControlCommand<?>	CMD_SYNC	= Control
																.createCommand( "sync",
																		MultivariantString
																				.getString( "Synchronization settings",
																						Collections
																								.singletonMap( "ru",
																										"Настройки синхронизации" ) ) )
																.setCommandPermission( "$publish" )
																.setCommandIcon( "command-synchronization" );
	
	private final BaseEntry<?>				entry;
	
	/**
	 * @param entry
	 */
	CommonActor(final BaseEntry<?> entry) {
		this.entry = entry;
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		if (command == CommonActor.CMD_SYNC) {
			final StorageImpl parent = this.entry.getStorageImpl();
			final Locker lock = parent.areLocksSupported()
					? parent.getLocker().createLock( this.entry.getGuid(), 0 )
					: null;
			if (lock == null || lock.isOwned()) {
				return new FormEntrySynchronization( this.entry, lock );
			}
			return new FormLocked( lock, new FormEntrySynchronization( this.entry, null ) );
		}
		throw new IllegalArgumentException( "Unknown command: " + command.getKey() );
	}
	
	@Override
	public ControlCommandset getCommands() {
		final ControlCommandset result = Control.createOptions();
		result.add( CommonActor.CMD_SYNC );
		return result;
	}
}
