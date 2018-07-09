/*
 * Created on 11.09.2005
 */
package ru.myx.renderer.xslt;

import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import java.util.function.Function;
import ru.myx.ae3.eval.CompileTargetMode;
import ru.myx.ae3.eval.Evaluate;
import ru.myx.ae3.eval.LanguageImpl;
import ru.myx.ae3.exec.ProgramAssembly;

/**
 * @author myx
 * 
 */
public final class AcmXsltLanguageImpl implements LanguageImpl {

	@Override
	public final void compile(final String identity, final Function<String, String> folder, final String name, final ProgramAssembly assembly, final CompileTargetMode mode)
			throws Evaluate.CompilationException {
		
		final String source = folder.apply(name);
		if (source == null) {
			return;
		}
		final TransformerFactory factory = TransformerFactory.newInstance();
		final Source xsl = new StreamSource(new StringReader(source));
		final Transformer transformer;
		try {
			transformer = factory.newTransformer(xsl);
		} catch (final TransformerConfigurationException e) {
			throw new RuntimeException(e);
		}
		assembly.addInstruction(new WrapperRenderer(source, transformer));
	}

	@Override
	public String[] getAssociatedAliases() {

		return new String[]{
				//
				"XSLT", //
				"ACM.XSLT", //
				"XSL", //
				"ACM.XSL", //
		};
	}

	@Override
	public String[] getAssociatedExtensions() {

		return new String[]{
				//
				".xsl", //
				".xslt", //
		};
	}

	@Override
	public String[] getAssociatedMimeTypes() {

		return new String[]{
				//
				"text/xsl", //
				"text/xslt", //
				"application/xslt+xml", //
				"application/xsl+xml", //
		};
	}

	@Override
	public String getKey() {

		return "XSLT";
	}

}
