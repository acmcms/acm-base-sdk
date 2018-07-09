/*
 * Created on 22.04.2006
 */
package ru.myx.ae2virtual;

import ru.myx.ae3.flow.PipeVirtualAbstract;
import ru.myx.ae3.serve.ServeRequest;

final class PipeVirtual extends PipeVirtualAbstract<ServeRequest> {
	
	PipeVirtual() {
	
		super( ServeRequest.class );
	}
	
}
