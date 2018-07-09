package ru.myx.ae1evtlog;

/*
 * Created on 06.10.2003
 *
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

import ru.myx.ae1.know.Server;
import ru.myx.ae3.act.Act;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.report.Event;
import ru.myx.ae3.report.LogReceiver;
import ru.myx.ae3.report.Report;
import ru.myx.ae3.status.StatusInfo;

abstract class BufferedReciever extends LogReceiver {
	boolean						active			= false;
	
	private final Runnable		eventProcessor	= new Runnable() {
													@Override
													public void run() {
														if (Report.MODE_DEBUG) {
															Report.devel( "JOS5/SML/ERB", "Started, erb=" + this );
														}
														
														final Event[] events;
														synchronized (BufferedReciever.this) {
															if (BufferedReciever.this.eventsSize == 0) {
																BufferedReciever.this.active = false;
																return;
															}
															
															events = new Event[BufferedReciever.this.eventsSize];
															int j = BufferedReciever.this.eventsStart;
															for (int i = 0; i < BufferedReciever.this.eventsSize; ++i) {
																events[i] = BufferedReciever.this.events[j];
																BufferedReciever.this.events[j++] = null;
																j %= BufferedReciever.this.eventsCapacity;
															}
															BufferedReciever.this.eventsSize = 0;
															BufferedReciever.this.eventsStart = BufferedReciever.this.eventsEnd;
														}
														
														try {
															BufferedReciever.this.processEvents( events );
														} catch (final Throwable t) {
															t.printStackTrace();
														}
														
														Act.later( BufferedReciever.this.context.getRootContext(),
																this,
																BufferedReciever.this.delaySeconds * 1000L );
													}
													
													@Override
													public String toString() {
														return "Buffered event reciever feeder.";
													}
												};
	
	private int					stStarted		= 0;
	
	private static final int	STEP			= 128;
	
	Event[]					events			= new Event[BufferedReciever.STEP];
	
	int							eventsStart		= 0;
	
	int							eventsEnd		= 0;
	
	int							eventsSize		= 0;
	
	int							eventsCapacity	= BufferedReciever.STEP;
	
	private final String[]		eventClasses;
	
	private final String[]		eventTypes;
	
	final Server				context;
	
	int							delaySeconds	= 2;
	
	private int					maxCollected	= Integer.MAX_VALUE;
	
	BufferedReciever(final String[] eventClasses, final String[] eventTypes) {
		this.eventClasses = eventClasses;
		this.eventTypes = eventTypes;
		this.context = Context.getServer( Exec.currentProcess() );
	}
	
	@Override
	protected final String[] eventClasses() {
		return this.eventClasses;
	}
	
	@Override
	protected final String[] eventTypes() {
		return this.eventTypes;
	}
	
	@Override
	protected final void onEvent(final Event event) {
		boolean start = false;
		synchronized (this) {
			if (this.eventsSize + 1 == this.eventsCapacity) {
				final Event[] newFifo = new Event[this.eventsCapacity += BufferedReciever.STEP];
				if (this.eventsStart < this.eventsEnd || this.eventsEnd == 0) {
					System.arraycopy( this.events, this.eventsStart, newFifo, 0, this.eventsSize );
				} else {
					final int firstLength = this.eventsCapacity - BufferedReciever.STEP - this.eventsStart;
					System.arraycopy( this.events, this.eventsStart, newFifo, 0, firstLength );
					System.arraycopy( this.events, 0, newFifo, firstLength, this.eventsEnd );
				}
				this.events = newFifo;
				this.eventsStart = 0;
				this.eventsEnd = this.eventsSize;
			}
			this.events[this.eventsEnd++] = event;
			this.eventsEnd %= this.eventsCapacity;
			this.eventsSize++;
			
			if (!this.active) {
				this.active = true;
				start = true;
			}
		}
		
		try {
			if (start) {
				this.stStarted++;
				Act.launch( this.context.getRootContext(), this.eventProcessor );
			}
		} catch (final Throwable t) {
			this.active = false;
			t.printStackTrace();
		}
	}
	
	protected abstract void processEvents(final Event[] events);
	
	protected void setDelaySeconds(final int seconds) {
		this.delaySeconds = seconds;
	}
	
	protected void setMaxCollected(final int count) {
		this.maxCollected = count;
	}
	
	@Override
	public void statusFill(final StatusInfo data) {
		super.statusFill( data );
		data.put( "Buffer capacity", this.eventsCapacity );
		data.put( "Process delay, sec", this.delaySeconds );
		data.put( "Collected limit", this.maxCollected );
		data.put( "Processor started", this.stStarted );
	}
}
