/*
 * Created on 11.09.2005
 */
package ru.myx.renderer.xslt;

import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Element;

import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BasePrimitiveString;
import ru.myx.ae3.exec.ExecProcess;
import ru.myx.ae3.exec.ExecStateCode;
import ru.myx.ae3.exec.Instruction;
import ru.myx.ae3.xml.Xml;

final class WrapperRenderer implements Instruction {
	
	private final String source;
	
	private final Transformer transformer;
	
	WrapperRenderer(final String source, final Transformer transformer) {
		this.source = source;
		this.transformer = transformer;
	}
	
	@Override
	public ExecStateCode execCall(final ExecProcess ctx) throws Exception {
		
		final Element context = Xml.toElement("context", ctx.rb7FV, false);
		final Source source = new DOMSource(context);
		final StringWriter writer = new StringWriter();
		final Result target = new javax.xml.transform.stream.StreamResult(writer);
		this.transformer.transform(source, target);
		final BasePrimitiveString result = Base.forString(writer.toString());
		ctx.execOutput(result);
		ctx.ra0RB = result;
		return null;
	}
	
	@Override
	public int getOperandCount() {
		
		return 0;
	}
	
	@Override
	public int getResultCount() {
		
		return 1;
	}
	
	@Override
	public String toCode() {
		
		return this.source;
	}
	
	@Override
	public final String toString() {
		
		return this.source;
	}
}
