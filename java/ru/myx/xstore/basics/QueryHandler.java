/*
 * Created on 23.06.2004
 */
package ru.myx.xstore.basics;

import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.ModuleInterface;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae1.types.Type;
import ru.myx.ae3.answer.Reply;
import ru.myx.ae3.answer.ReplyAnswer;
import ru.myx.ae3.help.Format;
import ru.myx.ae3.i3.Handler;
import ru.myx.ae3.report.Report;
import ru.myx.ae3.serve.ServeRequest;

final class QueryHandler implements Handler {
	private static final String		OWNER	= "QHANDLER";
	
	private final StorageImpl		parent;
	
	private final ModuleInterface	storage;
	
	private final String			rootId;
	
	private final boolean			setAttachment;
	
	QueryHandler(final StorageImpl parent, final String rootId, final boolean setAttachment) {
		this.parent = parent;
		this.storage = parent.getStorage();
		this.rootId = rootId;
		this.setAttachment = setAttachment;
	}
	
	private final QueryInfo analyzeRequest(final QueryInfo info, final BaseEntry<?> entry, final ServeRequest query) {
		final int state = entry.getState();
		if (state < ModuleInterface.STATE_READY) {
			info.setContainsDraft();
		}
		final String request = query.getResourceIdentifier();
		final int pos = request.indexOf( '/' );
		try {
			final Type<?> type = entry.getType();
			if (pos == -1) {
				final int requestLength = request.length();
				if (requestLength == 0) {
					return info.setQuery( entry );
				}
				if (requestLength > 6) {
					if (request.endsWith( ".field" )) {
						final String lead = request.substring( 0, requestLength - 6 );
						final int fpos = lead.lastIndexOf( '.' );
						final String fieldName = lead.substring( fpos + 1 );
						final String docName = lead.substring( 0, fpos );
						if (docName.length() == 0) {
							return info.setField( entry, fieldName );
						}
						final BaseEntry<?> target = entry.getChildByName( docName );
						if (target != null) {
							return info.setField( target, fieldName );
						}
					}
					final int fieldPosition = request.indexOf( ".field." );
					if (fieldPosition != -1) {
						final String lead = request.substring( 0, fieldPosition );
						final int fpos = lead.lastIndexOf( '.' );
						final String fieldName = lead.substring( fpos + 1 );
						final String docName = lead.substring( 0, fpos );
						if (docName.length() == 0) {
							return info.setField( entry, fieldName );
						}
						final BaseEntry<?> target = entry.getChildByName( docName );
						if (target != null) {
							return info.setField( target, fieldName );
						}
					}
				}
				if (type != null && type.getTypeBehaviorHandleAllIncoming()) {
					return info.setQuery( entry );
				}
				final BaseEntry<?> handler = entry.getChildByName( request );
				if (handler != null) {
					if (handler.isFolder()) {
						return info.setRedirect( query.getResourcePrefix() + request + '/' );
					}
					if (type != null && type.getTypeBehaviorResponseFiltering()) {
						info.addFilter( entry );
					}
					return this.analyzeRequest( info, handler, query.shiftRequested( requestLength, false ) );
				}
				if (type != null && type.getTypeBehaviorHandleAnyThrough()) {
					return info.setQuery( entry );
				}
				if (info.setLastAny()) {
					return info;
				}
				return null;
			}
			if (type != null && type.getTypeBehaviorHandleAllIncoming()) {
				return info.setQuery( entry );
			}
			final BaseEntry<?> handler = entry.getChildByName( request.substring( 0, pos ) );
			if (handler == null) {
				if (type != null && type.getTypeBehaviorHandleAnyThrough()) {
					return info.setQuery( entry );
				}
				if (info.setLastAny()) {
					return info;
				}
				return null;
			}
			if (type != null && type.getTypeBehaviorHandleAnyThrough()) {
				info.setLastAny( entry, query.getResourcePrefix(), query.getResourceIdentifier() );
			}
			if (type != null && type.getTypeBehaviorResponseFiltering()) {
				info.addFilter( entry );
			}
			return this.analyzeRequest( info, handler, query.shiftRequested( pos + 1, false ) );
		} catch (final Throwable t) {
			Report.exception( QueryHandler.OWNER, ("Error while trying to respond: url=" + query.getUrl()), t );
			return info.setError( Format.Throwable.toText( "url=" + query.getUrl(), t ) );
		}
	}
	
	@Override
	public final ReplyAnswer onQuery(final ServeRequest query) {
		final BaseEntry<?> root;
		if (this.setAttachment) {
			final Object attachment = query.getAttachment();
			if (attachment != null && attachment instanceof BaseEntry<?>) {
				root = (BaseEntry<?>) attachment;
			} else {
				root = this.storage.getByGuid( this.rootId );
				if (root == null) {
					return Reply.string( QueryHandler.OWNER, query, "No share found!" );
				}
				query.setAttachment( root );
			}
		} else {
			root = this.storage.getByGuid( this.rootId );
			if (root == null) {
				return Reply.string( QueryHandler.OWNER, query, "No share found!" );
			}
		}
		final QueryInfo info = this.analyzeRequest( new QueryInfo(), root, query.shiftRequested( 1, false ) );
		if (info == null) {
			return null;
		}
		return info.handleQuery( this.parent, query );
	}
	
	@Override
	public final String toString() {
		return "StorageShare( root=" + this.rootId + ", storage=" + this.parent + " )";
	}
}
