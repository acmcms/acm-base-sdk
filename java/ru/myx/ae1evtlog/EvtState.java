package ru.myx.ae1evtlog;

import ru.myx.ae3.status.StatusInfo;
import ru.myx.ae3.status.StatusProvider;

/*
 * Created on 16.05.2006
 */

/** @author myx */
public final class EvtState implements StatusProvider {
	
	private final EventRecieverToStdout ets;

	/** @param ets
	 */
	public EvtState(final EventRecieverToStdout ets) {
		
		this.ets = ets;
	}

	@Override
	public String statusDescription() {
		
		return "Eventing to Log status";
	}

	@Override
	public void statusFill(final StatusInfo data) {
		
		this.ets.statusFill(data);
	}

	@Override
	public String statusName() {
		
		return "ets";
	}
}
