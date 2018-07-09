/*
 * Created on 27.05.2004
 */
package ru.myx.xstore.schedule;

import java.util.ArrayList;
import java.util.List;

import ru.myx.ae1.schedule.Action;
import ru.myx.ae1.schedule.Change;
import ru.myx.ae1.storage.BaseSchedule;
import ru.myx.ae1.types.Type;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.ControlBasic;

/**
 * @author myx
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
final class ChangeListing implements BaseSchedule {
	private final Change			change;
	
	private final Type<?>			type;
	
	private List<ControlBasic<?>>	result	= null;
	
	ChangeListing(final Type<?> type, final Change change) {
		this.change = change;
		this.type = type;
	}
	
	@Override
	public void clear() {
		this.change.clear();
		this.result = null;
	}
	
	@Override
	public void commit() {
		this.change.commit();
	}
	
	Action<?> get(final String key) {
		return this.change.get( key );
	}
	
	final List<ControlBasic<?>> getListing() {
		final List<ControlBasic<?>> ready = this.result;
		if (ready != null) {
			return ready;
		}
		final List<ControlBasic<?>> result = new ArrayList<>();
		final Action<?>[] current = this.change.getCurrent();
		if (current == null || current.length == 0) {
			this.result = result;
			return result;
		}
		for (final Action<?> element : current) {
			result.add( new ActionBasic( this.type, element ) );
		}
		this.result = result;
		return result;
	}
	
	@Override
	public boolean isEmpty() {
		return this.change.isEmpty();
	}
	
	void schedule(final Action<?> action) {
		this.change.schedule( action );
		this.result = null;
	}
	
	@Override
	public void schedule(
			final String name,
			final boolean replace,
			final long date,
			final String command,
			final BaseObject parameters) {
		this.change.schedule( name, replace, date, command, parameters );
		this.result = null;
	}
	
	@Override
	public void scheduleCancel(final String name) {
		this.change.scheduleCancel( name );
		this.result = null;
	}
	
	@Override
	public void scheduleCancelGuid(final String key) {
		this.change.scheduleCancelGuid( key );
		this.result = null;
	}
	
	@Override
	public void scheduleFill(final BaseSchedule schedule, final boolean replace) {
		this.change.scheduleFill( schedule, replace );
	}
	
	@Override
	public void scheduleGuid(
			final String key,
			final boolean replace,
			final long date,
			final String command,
			final BaseObject parameters) {
		this.change.scheduleGuid( key, replace, date, command, parameters );
		this.result = null;
	}
}
