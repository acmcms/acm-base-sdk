package ru.myx.ae1evtlog;

import ru.myx.ae3.report.AbstractEvent;

/*
 * Created on 03.12.2003
 * 
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

final class FirstEvent extends AbstractEvent implements Runnable {
	
	private final EventRecieverToStdout parent;
	
	FirstEvent(final EventRecieverToStdout parent) {
		this.parent = parent;
	}
	
	@Override
	public String getEventTypeId() {
		
		return "FEVT:FEVT";
	}
	
	@Override
	public String getSubject() {
		
		return "{Java Version=\"" + System.getProperty("java.version") + "\", Java Vendor=\"" + System.getProperty("java.vendor") + "\", JVM Name=\""
				+ System.getProperty("java.vm.name") + "\", JVM Version=\"" + System.getProperty("java.vm.version") + "\", JVM Vendor=\"" + System.getProperty("java.vm.vendor")
				+ "\", JRE Name=\"" + System.getProperty("java.runtime.name") + "\", JRE Version=\"" + System.getProperty("java.runtime.version") + "\", OS Architect=\""
				+ System.getProperty("os.arch") + "\", OS Name=\"" + System.getProperty("os.name") + "\", OS Version=\"" + System.getProperty("os.version") + "\"}";
	}
	
	@Override
	public String getTitle() {
		
		return "ATTACHED";
	}
	
	@Override
	public void run() {
		
		if (!this.parent.started) {
			System.out.println("WATCHDOG - SYSTEM SEEMS TO BE LOCKED - RESTARTING!");
			try {
				Runtime.getRuntime().exit(-31);
			} catch (final Throwable t) {
				t.printStackTrace();
				try {
					Thread.sleep(20000L);
				} catch (final InterruptedException ie) {
					// ignore
				}
				Runtime.getRuntime().halt(-32);
			}
		}
	}
}
