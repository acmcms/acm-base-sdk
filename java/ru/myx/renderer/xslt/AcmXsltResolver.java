/*
 * Created on 23.04.2006
 */
package ru.myx.renderer.xslt;

import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import java.util.function.Function;

final class AcmXsltResolver implements URIResolver {
	private final Function<String, String>	folder;
	
	AcmXsltResolver(final Function<String, String> folder) {
		this.folder = folder;
	}
	
	@Override
	public final Source resolve(final String href, final String base) throws TransformerException {
		try {
			return new StreamSource( new StringReader( this.folder.apply( href ) ) );
		} catch (final Throwable e) {
			throw new TransformerException( e );
		}
	}
}
