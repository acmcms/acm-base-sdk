package ru.myx.ae1evtlog;

/*
 * Created on 03.12.2003
 * 
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
/**
 * @author myx
 * 
 */
public final class EvtLogShutdownHook extends Thread {
	/**
     * 
     */
	public EvtLogShutdownHook() {
		super( "ctrl-ae1evtlog: shotdown hook" );
	}
	
	@Override
	public void run() {
		System.out.flush();
		System.err.flush();
	}
	
	@Override
	public String toString() {
		return "Flush stderr & stdout on exit.";
	}
}
