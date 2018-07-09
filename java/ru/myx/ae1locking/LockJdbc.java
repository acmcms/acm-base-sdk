package ru.myx.ae1locking;

/*
 * Created on 21.08.2004
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
import ru.myx.jdbc.lock.Locker;

class LockJdbc implements Locker {
	
	private final LockManagerImpl	lockManager;
	
	private final String			guid;
	
	private final int				version;
	
	private final String			id;
	
	private final boolean			owned;
	
	private final long				date;
	
	private long					expiration;
	
	LockJdbc(final LockManagerImpl lockManager,
			final String guid,
			final int version,
			final String id,
			final boolean owned,
			final long date,
			final long expiration) {
		this.lockManager = lockManager;
		this.guid = guid;
		this.version = version;
		this.id = id;
		this.owned = owned;
		this.date = date;
		this.expiration = expiration;
	}
	
	@Override
	public long getLockDate() {
		return this.date;
	}
	
	@Override
	public long getLockExpiration() {
		return this.expiration;
	}
	
	@Override
	public String getLockId() {
		return this.id;
	}
	
	@Override
	public boolean isOwned() {
		return this.owned;
	}
	
	@Override
	public void lockRelease() {
		if (this.owned) {
			this.lockManager.lockRelease( this.guid, this.version, this.id );
		} else {
			throw new IllegalArgumentException( "Not a lock owner!" );
		}
	}
	
	@Override
	public void lockUpdate() {
		if (this.owned) {
			this.expiration = this.lockManager.lockUpdate( this.guid, this.version, this.id );
		} else {
			throw new IllegalArgumentException( "Not a lock owner!" );
		}
	}
	
}
