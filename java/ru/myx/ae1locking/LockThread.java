package ru.myx.ae1locking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import ru.myx.ae3.Engine;
import ru.myx.ae3.act.Act;
import ru.myx.ae3.report.Report;
import ru.myx.jdbc.lock.Interest;

/*
 * Created on 10.04.2006
 */
final class LockThread implements Runnable {
	
	private static final long TIME_CHECK_PERIOD = 1L * 60_000L;

	private static final long TIME_RELEASE = 5L * 60_000L;

	private static final long TIME_LOCK_EXPIRATION = 3L * 60_000L;

	private static final long TIME_HOLD_LOCK = 60L * 60_000L;

	private final LockManagerImpl manager;

	private final String tableName;

	private final String identity;

	private boolean destroyed = false;

	LockThread(final LockManagerImpl manager, final String tableName, final String identity) {
		
		this.manager = manager;
		this.tableName = tableName;
		this.identity = identity;
	}

	@Override
	public void run() {
		
		if (this.destroyed) {
			return;
		}
		try {
			synchronized (this.manager) {
				final long date = Engine.fastTime();
				final Set<Interest> ownLocks = this.manager.getOwnLocks();
				final Set<Interest> waitLocks = this.manager.getWaitLocks();
				final LinkedList<QueueRecord> ownQueue = this.manager.getOwnQueue();
				final LinkedList<QueueRecord> passQueue = this.manager.getPassQueue();
				final Set<Interest> releaseLocks = new HashSet<>();
				for (; !passQueue.isEmpty();) {
					final QueueRecord record = passQueue.getFirst();
					if (record.date < date) {
						waitLocks.add(record.lock);
						passQueue.removeFirst();
					} else {
						break;
					}
				}
				for (; !ownQueue.isEmpty();) {
					final QueueRecord record = ownQueue.getFirst();
					if (record.date < date) {
						try {
							record.lock.runner.stop();
							Report.info("DS/LOCK_MANAGER", "Stopping a task: " + record.lock.lock);
						} catch (final Throwable t) {
							Report.exception("DS/LOCK_MANAGER", "Error stopping a task", t);
						}
						ownLocks.remove(record.lock);
						passQueue.add(new QueueRecord(date + LockThread.TIME_RELEASE, record.lock));
						releaseLocks.add(record.lock);
						ownQueue.removeFirst();
					} else {
						break;
					}
				}
				try (final Connection conn = this.manager.nextConnection()) {
					if (conn == null) {
						return;
					}
					try (final PreparedStatement ps = conn.prepareStatement("DELETE FROM " + this.tableName + " WHERE lockExpiration<?")) {
						ps.setTimestamp(1, new Timestamp(date));
						ps.executeUpdate();
					}
					if (!releaseLocks.isEmpty()) {
						try (final PreparedStatement ps = conn.prepareStatement("DELETE FROM " + this.tableName + " WHERE lockType=? AND lockVersion=? AND lockId=?")) {
							for (final Interest lock : releaseLocks) {
								ps.setString(1, lock.lock);
								ps.setInt(2, lock.version);
								ps.setString(3, this.identity);
								ps.executeUpdate();
								ps.clearParameters();
							}
						}
					}
					if (!ownLocks.isEmpty()) {
						final Set<Interest> lostLocks = new HashSet<>();
						for (final Interest lock : ownLocks) {
							try (final PreparedStatement ps = conn
									.prepareStatement("UPDATE " + this.tableName + " SET lockExpiration=? WHERE lockType=? AND lockVersion=? AND lockId=?")) {
								ps.setTimestamp(1, new Timestamp(Engine.fastTime() + LockThread.TIME_LOCK_EXPIRATION));
								ps.setString(2, lock.lock);
								ps.setInt(3, lock.version);
								ps.setString(4, this.identity);
								if (ps.executeUpdate() == 0) {
									lostLocks.add(lock);
								} else {
											/** TODO: pre-cache for queue */
											try (final PreparedStatement ps2 = conn.prepareStatement(
													"SELECT lockId FROM " + this.tableName + " WHERE lockType=? ORDER BY lockVersion DESC",
													ResultSet.TYPE_FORWARD_ONLY,
													ResultSet.CONCUR_READ_ONLY)) {
										ps2.setString(1, lock.lock);
										ps2.setMaxRows(1);
										try (final ResultSet rs = ps2.executeQuery()) {
											if (rs.next() && !this.identity.equals(rs.getString(1))) {
												lostLocks.add(lock);
											}
										}
									}
								}
							}
						}
						if (!lostLocks.isEmpty()) {
							for (final Iterator<QueueRecord> i = ownQueue.iterator(); i.hasNext();) {
								final QueueRecord record = i.next();
								if (lostLocks.contains(record.lock)) {
									Report.warning("DS/LOCK_MANAGER", "Lost task lock, taskVersion=" + record.lock.version + ", taskId=" + record.lock.lock);
									try {
										record.lock.runner.stop();
									} catch (final Throwable t) {
										Report.exception("DS/LOCK_MANAGER", "Error stopping a task", t);
									}
									i.remove();
								}
							}
							ownLocks.removeAll(lostLocks);
							waitLocks.addAll(lostLocks);
						}
					}
					if (!waitLocks.isEmpty()) {
						for (final Iterator<Interest> i = waitLocks.iterator(); i.hasNext();) {
							final Interest lock = i.next();
							try {
								try (final PreparedStatement ps = conn
										.prepareStatement("INSERT INTO " + this.tableName + "(lockType,lockVersion,lockId,lockDate,lockExpiration) VALUES (?,?,?,?,?)")) {
									ps.setString(1, lock.lock);
									ps.setInt(2, lock.version);
									ps.setString(3, this.identity);
									ps.setTimestamp(4, new Timestamp(Engine.fastTime()));
									ps.setTimestamp(5, new Timestamp(Engine.fastTime() + LockThread.TIME_LOCK_EXPIRATION));
									ps.execute();
								}
								try (final PreparedStatement ps = conn.prepareStatement(
										"SELECT lockId, lockVersion FROM " + this.tableName + " WHERE lockType=? ORDER BY lockVersion DESC",
										ResultSet.TYPE_FORWARD_ONLY,
										ResultSet.CONCUR_READ_ONLY)) {
									ps.setString(1, lock.lock);
									ps.setMaxRows(1);
									try (final ResultSet rs = ps.executeQuery()) {
										if (rs.next()) {
											if (this.identity.equals(rs.getString(1))) {
												try {
													lock.runner.start();
													Report.info("DS/LOCK_MANAGER", "Starting a task: " + lock.lock);
												} catch (final Throwable t) {
													Report.exception("DS/LOCK_MANAGER", "Error starting a task", t);
												}
												i.remove();
												ownLocks.add(lock);
												ownQueue.add(new QueueRecord(Engine.fastTime() + LockThread.TIME_HOLD_LOCK, lock));
												break;
											}
											final int otherVersion = rs.getInt(2);
											if (otherVersion > lock.version) {
												Report.warning(
														"DS/LOCK_MANAGER",
														"Ignore task, newer version detected, taskVersion=" + lock.version + " vs " + otherVersion + ", taskId=" + lock.lock);
											} else {
												Report.warning("DS/LOCK_MANAGER", "Ignore task, lock is taken, taskVersion=" + lock.version + ", taskId=" + lock.lock);
											}
										} else {
											Report.warning("DS/LOCK_MANAGER", "Ignore task, cannot register interest, taskVersion=" + lock.version + ", taskId=" + lock.lock);
										}
									}
								}
							} catch (final SQLException e) {
								conn.clearWarnings();
							}
						}
					}
				} catch (final SQLException e) {
					Report.exception("DS/LOCK_MANAGER", "Unhandled exception in main loop", e);
				}
			}
		} finally {
			if (!this.destroyed) {
				Act.later(null, this, LockThread.TIME_CHECK_PERIOD);
			}
		}
	}

	@Override
	public String toString() {
		
		return "LockManagerThread: id=" + this.identity + ", ownLocks=" + this.manager.getOwnLocks().size() + ", waitLocks=" + this.manager.getWaitLocks().size() + ", ownQueue="
				+ this.manager.getOwnQueue().size() + ", passQueue=" + this.manager.getPassQueue().size() + ", table=" + this.tableName;
	}

	void stop() {
		
		this.destroyed = true;
	}
}
