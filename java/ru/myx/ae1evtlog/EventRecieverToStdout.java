package ru.myx.ae1evtlog;

import ru.myx.ae3.act.Act;
import ru.myx.ae3.report.Event;
import ru.myx.ae3.report.Report;
import ru.myx.ae3.report.ReportReceiver;

/** @author myx
 *
 *         myx - barachta "typecomment": Window>Preferences>Java>Templates. To enable and disable
 *         the creation of type comments go to Window>Preferences>Java>Code Generation. */
public final class EventRecieverToStdout extends BufferedReciever {
	
	private ReportReceiver reciever;

	boolean started = false;

	/**
	 * 
	 */
	public EventRecieverToStdout() {
		
		super(null, null);
		this.setDelaySeconds(3);
	}

	@Override
	protected void processEvents(final Event[] events) {
		
		if (this.reciever == null) {
			final boolean created;
			synchronized (this) {
				if (this.reciever == null) {
					final ReportReceiver reciever = Report.createReceiver(null);
					this.reciever = reciever;
					created = true;
				} else {
					created = false;
				}
			}
			if (created) {
				System.out.println("EVENTING: creating test event.");
				final FirstEvent event = new FirstEvent(this);
				Act.later(null, event, 30_000L);
				System.out.println("EVENTING: sending test event.");
				this.reciever.event(event);
				System.out.println("EVENTING: switching logs.");
				try {
					final int eventCount = events.length;
					for (int i = 0; i < eventCount; ++i) {
						this.reciever.event(events[i]);
					}
				} catch (final Throwable t) {
					t.printStackTrace();
				}
				this.started = true;
				return;
			}
		}
		try {
			final int eventCount = events.length;
			for (int i = 0; i < eventCount; ++i) {
				this.reciever.event(events[i]);
			}
		} catch (final Throwable t) {
			t.printStackTrace();
		}
	}
}
