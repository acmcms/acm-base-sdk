package ru.myx.ae1locking;

import ru.myx.jdbc.lock.Interest;

/*
 * Created on 10.04.2006
 */

final class QueueRecord {
	final long		date;
	
	final Interest	lock;
	
	QueueRecord(final long date, final Interest lock) {
		this.date = date;
		this.lock = lock;
	}
}
