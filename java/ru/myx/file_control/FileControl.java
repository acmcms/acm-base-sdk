package ru.myx.file_control;

/*
 * Created on 18.06.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import ru.myx.ae2.watch.Watch;
import ru.myx.ae3.Engine;
import ru.myx.ae3.help.Format;

/**
 * @author myx
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FileControl {
	
	static final void createExecuteFlag(final File executeFlag) throws Exception {
		try (final FileOutputStream fos = new FileOutputStream( executeFlag )) {
			fos.write( "Delete this file to execute command stored in execute.command file, \r\n execution output will be saved in execute.out and execute.err files.\r\n\r\n* The modification date of this file indicates when file-control service was last checked this flag."
					.getBytes( StandardCharsets.UTF_8 ) );
		}
	}
	
	private static final void createRestartFlag(final File restartFlag) throws Exception {
		try (final FileOutputStream fos = new FileOutputStream( restartFlag )) {
			fos.write( "Delete this file to restart the system.\r\n\r\n* The modification date of this file indicates when file-control service was last checked this flag."
					.getBytes( StandardCharsets.UTF_8 ) );
		}
	}
	
	static final void createSuspendReadyFlag(final File suspendReadyFlag) throws Exception {
		try (final FileOutputStream fos = new FileOutputStream( suspendReadyFlag )) {
			fos.write( "System is ready to suspend. Create 'suspend' file flag to suspend."
					.getBytes( StandardCharsets.UTF_8 ) );
		}
	}
	
	static final void createSuspendWaitingFlag(final File suspendWaitingFlag) throws Exception {
		try (final FileOutputStream fos = new FileOutputStream( suspendWaitingFlag )) {
			fos.write( "System is suspended. Delete 'suspend' file flag to resume.".getBytes( StandardCharsets.UTF_8 ) );
		}
	}
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		final File folder = new File( Engine.PATH_PRIVATE, "control" ).getAbsoluteFile();
		folder.mkdirs();
		System.out.println( "RU.MYX.AE2CTRL.FILECONTROL: flag folder: " + folder.getAbsolutePath() );
		
		final File restartFlag = new File( folder, "restart" );
		FileControl.createRestartFlag( restartFlag );
		
		final File suspendFlag = new File( folder, "suspend" );
		final File suspendReadyFlag = new File( folder, "suspend.ready" );
		final File suspendWaitingFlag = new File( folder, "suspend.waiting" );
		
		if (suspendFlag.exists()) {
			System.out.println( "RU.MYX.AE2CTRL.FILECONTROL: suspend request detected - suspending." );
			final long started = System.currentTimeMillis();
			suspendReadyFlag.delete();
			FileControl.createSuspendWaitingFlag( suspendWaitingFlag );
			for (;;) {
				synchronized (suspendFlag) {
					try {
						suspendFlag.wait( 1000L );
					} catch (final InterruptedException e) {
						suspendWaitingFlag.delete();
						System.out.println( "RU.MYX.AE2CTRL.FILECONTROL: suspend interrupted." );
						throw new RuntimeException( e );
					}
				}
				if (!restartFlag.exists()) {
					System.out.println( "RU.MYX.AE2CTRL.FILECONTROL: restart request while suspending, sleep time: "
							+ Format.Compact.toPeriod( System.currentTimeMillis() - started )
							+ "." );
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
					throw new RuntimeException( "CANNOT RESTART!" );
				}
				if (!suspendFlag.exists()) {
					System.out.println( "RU.MYX.AE2CTRL.FILECONTROL: suspend cancel, sleep time: "
							+ Format.Compact.toPeriod( System.currentTimeMillis() - started )
							+ "." );
					break;
				}
			}
		}
		
		suspendWaitingFlag.delete();
		FileControl.createSuspendReadyFlag( suspendReadyFlag );
		
		final File executeFlag = new File( folder, "execute" );
		final File executeScript = new File( folder, "execute.command" );
		final File executeOut = new File( folder, "execute.out" );
		final File executeErr = new File( folder, "execute.err" );
		FileControl.createExecuteFlag( executeFlag );
		
		Watch.monitor( new FileControlWatcher( restartFlag,
				suspendFlag,
				suspendReadyFlag,
				executeFlag,
				executeScript,
				executeOut,
				executeErr ) );
	}
}
