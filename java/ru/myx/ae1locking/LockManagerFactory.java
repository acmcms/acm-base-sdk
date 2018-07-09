package ru.myx.ae1locking;

import java.sql.Connection;
import java.util.Enumeration;

import ru.myx.jdbc.lock.Lock;
import ru.myx.jdbc.lock.LockManager;

/*
 * Created on 09.10.2004
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author myx
 * 
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class LockManagerFactory implements Lock.ManagerFactoryImpl {
	
	@Override
	public LockManager createManager(
			final Enumeration<Connection> connectionSource,
			final String tableName,
			final String identity) {
		return new LockManagerImpl( connectionSource, tableName, identity );
	}
}
