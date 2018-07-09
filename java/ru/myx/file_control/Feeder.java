package ru.myx.file_control;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/*
 * Created on 03.12.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

final class Feeder implements Runnable {
	private final InputStream		input;
	
	private final FileOutputStream	output;
	
	private final byte[]			buffer	= new byte[1024];
	
	Feeder(final InputStream input, final FileOutputStream output) {
		this.input = input;
		this.output = output;
	}
	
	@Override
	public final void run() {
		try {
			try {
				for (;;) {
					final int read = this.input.read( this.buffer );
					if (read <= 0) {
						return;
					}
					this.output.write( this.buffer, 0, read );
				}
			} finally {
				this.output.close();
			}
		} catch (final IOException e) {
			throw new RuntimeException( e );
		} catch (final Throwable e) {
			throw e;
		}
	}
}
