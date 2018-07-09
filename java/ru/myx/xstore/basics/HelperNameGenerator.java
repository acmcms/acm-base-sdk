/*
 * Created on 26.06.2005
 */
package ru.myx.xstore.basics;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.myx.ae3.Engine;

final class HelperNameGenerator {
	private final SimpleDateFormat	dateFormat	= new SimpleDateFormat( "yyyyMMddHHmm" );
	
	private static final int		BASE		= 36;
	
	private static final int		FLOOR		= (int) Math.pow( HelperNameGenerator.BASE, 3 );
	
	private static final int		CEIL		= (int) Math.pow( HelperNameGenerator.BASE, 4 )
														- HelperNameGenerator.FLOOR;
	
	@Override
	public final String toString() {
		return this.dateFormat.format( new Date() )
				+ '-'
				+ Integer.toString( Engine.createRandom( HelperNameGenerator.CEIL ) + HelperNameGenerator.FLOOR,
						HelperNameGenerator.BASE ) + ".htm";
	}
}
