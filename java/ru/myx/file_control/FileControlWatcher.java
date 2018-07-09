package ru.myx.file_control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import ru.myx.ae2.watch.Watch;
import ru.myx.ae3.Engine;
import ru.myx.ae3.act.Act;
import ru.myx.ae3.binary.Transfer;

/*
 * Created on 03.12.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

final class FileControlWatcher implements Watch.Monitor {
	private final File	restartFlag;
	
	private final File	suspendFlag;
	
	private final File	suspendReadyFlag;
	
	private final File	executeFlag;
	
	private final File	executeCommand;
	
	private final File	executeOut;
	
	private final File	executeErr;
	
	FileControlWatcher(final File restartFlag,
			final File suspendFlag,
			final File suspendReadyFlag,
			final File executeFlag,
			final File executeCommand,
			final File executeOut,
			final File executeErr) {
		this.restartFlag = restartFlag;
		this.suspendFlag = suspendFlag;
		this.suspendReadyFlag = suspendReadyFlag;
		this.executeFlag = executeFlag;
		this.executeCommand = executeCommand;
		this.executeOut = executeOut;
		this.executeErr = executeErr;
	}
	
	@Override
	public boolean check() throws Exception {
		if (!this.restartFlag.exists()) {
			System.out.println( "RU.MYX.AE2CTRL.FILECONTROL: restarting..." );
			try {
				Runtime.getRuntime().exit( 0 );
			} catch (final Throwable t) {
				t.printStackTrace();
				try {
					Thread.sleep( 20000L );
				} catch (final InterruptedException ie) {
					// ignore
				}
				Runtime.getRuntime().halt( 0 );
			}
			System.out.println( "RU.MYX.AE2CTRL.FILECONTROL: RESTART FAILED!..." );
			return false;
		}
		this.restartFlag.setLastModified( Engine.fastTime() );
		if (this.suspendFlag.exists()) {
			System.out.println( "RU.MYX.AE2CTRL.FILECONTROL: suspending (restarting)..." );
			this.suspendReadyFlag.delete();
			try {
				Runtime.getRuntime().exit( 0 );
			} catch (final Throwable t) {
				t.printStackTrace();
				try {
					Thread.sleep( 20000L );
				} catch (final InterruptedException ie) {
					// ignore
				}
				Runtime.getRuntime().halt( 0 );
			}
			System.out.println( "RU.MYX.AE2CTRL.FILECONTROL: RESTART FAILED!..." );
			return false;
		}
		this.suspendReadyFlag.setLastModified( Engine.fastTime() );
		if (!this.executeFlag.exists()) {
			try {
				final Process process = Runtime.getRuntime().exec( Transfer.createBuffer( this.executeCommand )
						.toString() );
				final InputStream stdout = process.getInputStream();
				final InputStream stderr = process.getErrorStream();
				final FileOutputStream fout = new FileOutputStream( this.executeOut );
				final FileOutputStream ferr = new FileOutputStream( this.executeErr );
				
				Act.launch( null, new Feeder( stdout, fout ) );
				
				Act.launch( null, new Feeder( stderr, ferr ) );
				
				process.waitFor();
				
			} catch (final Throwable t) {
				try (final FileOutputStream fout = new FileOutputStream( this.executeOut )) {
					//
				}
				
				try (final PrintStream ferr = new PrintStream( new FileOutputStream( this.executeErr ) )) {
					t.printStackTrace( ferr );
				}
			} finally {
				FileControl.createExecuteFlag( this.executeFlag );
			}
		} else {
			this.executeFlag.setLastModified( Engine.fastTime() );
		}
		return true;
	}
}
