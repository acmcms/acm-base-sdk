package ru.myx.ae1locking;

/*
 * Created on 30.06.2004
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import ru.myx.ae3.Engine;
import ru.myx.ae3.act.Act;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.report.Report;
import ru.myx.jdbc.lock.Interest;
import ru.myx.jdbc.lock.LockManager;
import ru.myx.jdbc.lock.Locker;

/** @author myx
 *
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments */
class LockManagerImpl implements LockManager {
	
	private final Set<Interest> ownLocks;

	private final Set<Interest> waitLocks;

	private final LinkedList<QueueRecord> ownQueue;

	private final LinkedList<QueueRecord> passQueue;

	private final Enumeration<Connection> connectionSource;

	private final String tableName;

	private final String identity;

	private LockThread lockThread;

	LockManagerImpl(final Enumeration<Connection> connectionSource, final String tableName, final String identity) {
		
		this.connectionSource = connectionSource;
		this.tableName = tableName;
		this.identity = identity;
		this.ownLocks = new HashSet<>();
		this.waitLocks = new HashSet<>();
		this.ownQueue = new LinkedList<>();
		this.passQueue = new LinkedList<>();
	}

	@Override
	public boolean addInterest(final Interest interest) {
		
		synchronized (this) {
			return this.waitLocks.add(interest);
		}
	}

	@Override
	public Locker createLock(final String guid, final int version) {
		
		try (final Connection conn = this.nextConnection()) {
			try {
				final String id = Context.getUserId(Exec.currentProcess());
				final long date = Engine.fastTime();
				final long expiration = date + 10L * 1000L * 60L;
				try (final PreparedStatement ps = conn
						.prepareStatement("INSERT INTO " + this.tableName + "(lockType,lockVersion,lockId,lockDate,lockExpiration) VALUES (?,?,?,?,?)")) {
					ps.setString(1, guid);
					ps.setInt(2, version);
					ps.setString(3, id);
					ps.setTimestamp(4, new Timestamp(date));
					ps.setTimestamp(5, new Timestamp(expiration));
					if (ps.executeUpdate() == 1) {
						return new LockJdbc(this, guid, version, id, true, date, expiration);
					}
				}
			} catch (final SQLException e) {
				// ignore
			}
			{
				try (final PreparedStatement ps = conn.prepareStatement(
						"SELECT lockId,lockDate,lockExpiration FROM " + this.tableName + " WHERE lockType=? AND lockVersion=?",
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY)) {
					ps.setString(1, guid);
					ps.setInt(2, version);
					try (final ResultSet rs = ps.executeQuery()) {
						if (rs.next()) {
							return new LockJdbc(this, guid, version, rs.getString(1), false, rs.getTimestamp(2).getTime(), rs.getTimestamp(3).getTime());
						}
						return null;
					}
				}
			}
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean removeInterest(final Interest interest) {
		
		synchronized (this) {
			if (this.waitLocks.remove(interest)) {
				return true;
			}
			if (this.ownLocks.remove(interest)) {
				interest.runner.stop();
				return true;
			}
			return false;
		}
	}

	@Override
	public synchronized void start(final String identity) {
		
		if (this.identity == identity || this.identity.equals(identity)) {
			if (this.lockThread == null) {
				Act.later(null, this.lockThread = new LockThread(this, this.tableName, identity), 15_000L);
				Report.info("DS/LOCK_MANAGER", "Started, identity=" + identity);
			}
		}
	}

	@Override
	public synchronized void stop(final String identity) {
		
		if (this.identity == identity || this.identity.equals(identity)) {
			if (this.lockThread != null) {
				this.lockThread.stop();
				this.lockThread = null;
				for (final Interest lock : this.ownLocks) {
					try {
						lock.runner.stop();
					} catch (final Throwable t) {
						Report.exception("DS/LOCK_MANAGER", "Error stopping a task on deactivation", t);
					}
				}
				Report.info("DS/LOCK_MANAGER", "Stopped, identity=" + identity);
			}
		}
	}

	Set<Interest> getOwnLocks() {
		
		return this.ownLocks;
	}

	LinkedList<QueueRecord> getOwnQueue() {
		
		return this.ownQueue;
	}

	LinkedList<QueueRecord> getPassQueue() {
		
		return this.passQueue;
	}

	Set<Interest> getWaitLocks() {
		
		return this.waitLocks;
	}

	void lockRelease(final String guid, final int version, final String id) {
		
		try (final Connection conn = this.nextConnection()) {
			try (final PreparedStatement ps = conn.prepareStatement("DELETE FROM " + this.tableName + " WHERE lockType=? AND lockVersion=? AND lockId=?")) {
				ps.setString(1, guid);
				ps.setInt(2, version);
				ps.setString(3, id);
				ps.execute();
			}
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}

	long lockUpdate(final String guid, final int version, final String id) {
		
		try (final Connection conn = this.nextConnection()) {
			final long expiration = Engine.fastTime() + 10L * 1000L * 60L;
			try (final PreparedStatement ps = conn.prepareStatement("UPDATE " + this.tableName + " SET lockExpiration=? WHERE lockType=? AND lockVersion=? AND lockId=?")) {
				ps.setTimestamp(1, new Timestamp(expiration));
				ps.setString(2, guid);
				ps.setInt(3, version);
				ps.setString(4, id);
				if (ps.executeUpdate() == 1) {
					return expiration;
				}
				throw new RuntimeException("Not a lock owner, guid=" + guid + ", version=" + version + ", id=" + id);
			}
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}

	Connection nextConnection() {
		
		return this.connectionSource.nextElement();
	}
}
