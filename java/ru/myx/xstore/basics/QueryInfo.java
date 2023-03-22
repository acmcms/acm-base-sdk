/*
 * Created on 22.01.2005
 */
package ru.myx.xstore.basics;

import java.util.Set;

import ru.myx.ae1.access.AccessGroup;
import ru.myx.ae1.access.AuthLevels;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae1.types.Type;
import ru.myx.ae3.access.AccessPrincipal;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.answer.AbstractReplyException;
import ru.myx.ae3.answer.Reply;
import ru.myx.ae3.answer.ReplyAnswer;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseMessage;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.binary.Transfer;
import ru.myx.ae3.binary.TransferBuffer;
import ru.myx.ae3.binary.TransferCollector;
import ru.myx.ae3.binary.TransferCopier;
import ru.myx.ae3.binary.TransferTarget;
import ru.myx.ae3.cache.CacheL2;
import ru.myx.ae3.common.Value;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.exec.ExecProcess;
import ru.myx.ae3.extra.External;
import ru.myx.ae3.report.Report;
import ru.myx.ae3.serve.ServeRequest;
import ru.myx.util.QueueStackRecord;

final class QueryInfo {
	
	private static final String OWNER = "QHANDLER";

	private static final int TP_QUERY = 0;

	private static final int TP_FIELD = 1;

	private static final int TP_REDIRECT = 2;

	private static final int TP_ERROR = 3;

	private static final int TP_LAST_ANY = 4;

	private static final ReplyAnswer handleFieldRequest(final StorageImpl parent, final BaseEntry<?> entry, final ServeRequest query, final String field) {
		
		final Set<String> respondable = entry.getType().getFieldsPublic();
		if (!respondable.contains(field)) {
			return Reply.string(
					QueryInfo.OWNER, //
					query,
					"Not accessible: " + entry.getLocationControl() + ":" + field)//
					.setCode(Reply.CD_DENIED);
		}
		final String guid = entry.getGuid();
		final boolean attachment = Base.getString(query.getParameters(), "mode", "").equals("download");
		final String cid = "fldresp:" + (attachment
			? ';'
			: ':') + field;
		final CacheL2<ReplyAnswer> cache = parent.getObjectCache();
		ReplyAnswer response = cache.get(guid, cid);
		if (response != null) {
			return response.nextClone(query);
		}
		final ExecProcess process = Exec.currentProcess();
		final BaseObject flags = Context.getFlags(process);

		try {

			final BaseObject fields = entry.getData();
			final BaseObject objectImpl = fields.baseGet(field, BaseObject.UNDEFINED);
			assert objectImpl != null : "NULL java object";

			if (objectImpl == BaseObject.UNDEFINED) {
				return Reply.string(QueryInfo.OWNER, query, "Not found: " + entry.getLocationControl() + ":" + field).setCode(Reply.CD_UNKNOWN).setFlags(flags);
			}

			final Object object;
			for (Object temp = objectImpl;;) {
				if (temp instanceof External) {
					temp = ((External) temp).toBinary();
				} else if (temp instanceof Value<?>) {
					final Object base = ((Value<?>) temp).baseValue();
					if (base == temp) {
						object = temp;
						break;
					}
					temp = base;
				} else {
					object = temp;
					break;
				}
			}

			String contentName = null;

			if (object == null) {
				return Reply.string(
						QueryInfo.OWNER, //
						query,
						"Not found: " + entry.getLocationControl() + ":extra:" + field)//
						.setCode(Reply.CD_UNKNOWN)//
						.setFlags(flags);
			} else if (object instanceof byte[]) {
				if (Report.MODE_DEBUG) {
					Report.devel(QueryInfo.OWNER, "FREQ: BYTES");
				}
				final byte[] bytes = (byte[]) object;
				final String extension = Base.getString(fields, field + "_extension", null);
				if (extension != null) {
					contentName = entry.getKey() + '-' + field + extension;
				}
				final String contentType = Base.getString(fields, field + "_contenttype", "application/octet-stream");
				contentName = Base.getString(query.getParameters(), "name", contentName);

				if (bytes.length <= Transfer.BUFFER_MEDIUM) {
					response = Reply.wrap(QueryInfo.OWNER, query, bytes, contentName).setContentType(contentType).setTimeToLiveDays(1).setFinal();
				} else {
					final TransferCollector coll = Transfer.createCollector();
					try (final TransferTarget target = coll.getTarget()) {
						target.absorbArray(bytes, 0, bytes.length);
					}
					response = Reply.binary(QueryInfo.OWNER, query, coll.toBinary(), contentName).setContentType(contentType).setTimeToLiveDays(1).setFinal();
				}
			} else if (object instanceof TransferBuffer) {
				if (Report.MODE_DEBUG) {
					Report.devel(QueryInfo.OWNER, "FREQ: BUFFER");
				}
				final TransferBuffer buffer = (TransferBuffer) object;
				final String extension = Base.getString(fields, field + "_extension", null);
				if (extension != null) {
					contentName = entry.getKey() + '-' + field + extension;
				}
				final String contentType = Base.getString(fields, field + "_contenttype", "application/octet-stream");
				contentName = Base.getString(query.getParameters(), "name", contentName);

				response = Reply.binary(QueryInfo.OWNER, query, buffer, contentName).setContentType(contentType).setTimeToLiveDays(1).setFinal();
			} else if (object instanceof TransferCopier) {
				if (Report.MODE_DEBUG) {
					Report.devel(QueryInfo.OWNER, "FREQ: COPIER");
				}
				final TransferCopier copier = (TransferCopier) object;
				final String extension = Base.getString(fields, field + "_extension", null);
				if (extension != null) {
					contentName = entry.getKey() + '-' + field + extension;
				}
				final String contentType = Base.getString(fields, field + "_contenttype", "application/octet-stream");
				contentName = Base.getString(query.getParameters(), "name", contentName);

				response = Reply.binary(QueryInfo.OWNER, query, copier, contentName).setContentType(contentType).setTimeToLiveDays(1).setFinal();
			} else if (object instanceof BaseMessage) {
				if (Report.MODE_DEBUG) {
					Report.devel(QueryInfo.OWNER, "FREQ: MESSAGE");
				}
				final BaseMessage message = (BaseMessage) object;
				response = Reply.object(cid, query, message).setTimeToLiveDays(1).setFinal();
			} else if (object instanceof AbstractReplyException) {
				if (Report.MODE_DEBUG) {
					Report.devel(QueryInfo.OWNER, "FREQ: REPLY");
				}
				response = ((AbstractReplyException) object).getReply();
				if (Base.getString(response.getAttributes(), "Content-Name", "").isBlank()) {
					contentName = Base.getString(query.getParameters(), "name", entry.getKey());
					response = response.setContentName(contentName);
				}
				if (response.getCode() == Reply.CD_OK && !response.isPrivate()) {
					response.setTimeToLiveDays(1);
				}
			} else {
				if (Report.MODE_DEBUG) {
					Report.devel(QueryInfo.OWNER, "FREQ: STRING - " + object.getClass().getName());
				}
				contentName = Base.getString(query.getParameters(), "name", entry.getKey());
				response = Reply.string(QueryInfo.OWNER, query, object.toString()).setContentName(contentName).setFlags(flags);
			}

			if (contentName != null && attachment) {
				response.setAttribute("Content-Disposition", "attachment; filename=\"" + contentName + '"');
			}

		} catch (final AbstractReplyException e) {
			response = e.getReply();
		}

		response.setLastModified(entry.getModified());

		if (response.isPrivate()) {
			return response;
		}
		cache.put(guid, cid, response, 1000L * 60L * 60L);
		return response.nextClone(query);
	}

	private int type = QueryInfo.TP_QUERY;

	private BaseEntry<?> targetEntry = null;

	private boolean containsDraft = false;

	private String paramString;

	private QueueStackRecord<BaseEntry<?>> filters;

	private BaseEntry<?> lastAny = null;

	private String lastAnyPrefix = null;

	private String lastAnyIdentifier = null;

	final QueryInfo addFilter(final BaseEntry<?> entry) {
		
		if (this.filters == null) {
			this.filters = new QueueStackRecord<>();
		}
		this.filters.push(entry);
		return this;
	}

	final ReplyAnswer handleQuery(final StorageImpl parent, final ServeRequest query) {
		
		final BaseEntry<?> targetEntry = this.targetEntry;
		switch (this.type) {
			case TP_QUERY : {
				final boolean privateResponse;
				if ("POST".equals(query.getVerb())) {
					final AccessPrincipal<?> principal = parent.getServer().getAccessManager().securityCheck(
							AuthLevels.AL_AUTHORIZED_NORMAL,
							targetEntry.getLocationControl(),
							this.containsDraft
								? "execute_draft"
								: "execute");
					if (principal == null) {
						Report.audit(
								QueryInfo.OWNER, //
								"DENIED",
								"Security check failed: path=" + targetEntry.getLocationControl() + ", principal=" + principal);
						return Reply.stringForbidden(QueryInfo.OWNER, query, "No access!");
					}
					if (principal.isPerson() || ((AccessGroup<?>) principal).getAuthLevel() >= AuthLevels.AL_AUTHORIZED_AUTOMATICALLY) {
						Report.audit(
								QueryInfo.OWNER, //
								"GRANTED",
								"Security check granted execution: path=" + targetEntry.getLocationControl() + ", principal=" + principal);
					}
					privateResponse = true;
				} else {
					final AccessPrincipal<?> principal = parent.getServer().getAccessManager().securityCheck(
							AuthLevels.AL_AUTHORIZED_NORMAL,
							targetEntry.getLocationControl(),
							this.containsDraft
								? "execute_draft"
								: "execute");
					if (principal == null) {
						Report.audit(
								QueryInfo.OWNER, //
								"DENIED",
								"Security check failed: path=" + targetEntry.getLocationControl() + ", principal=" + principal);
						return Reply.stringForbidden(QueryInfo.OWNER, query, "No access!");
					}
					if (principal.isPerson() || ((AccessGroup<?>) principal).getAuthLevel() >= AuthLevels.AL_AUTHORIZED_AUTOMATICALLY) {
						Report.audit(
								QueryInfo.OWNER, //
								"GRANTED",
								"Security check granted elevation: path=" + targetEntry.getLocationControl() + ", principal=" + principal);
						privateResponse = true;
					} else {
						privateResponse = false;
					}
				}
				
				ReplyAnswer response;
				final long serverTtl;
				final int clientTtl;
				{
					final Type<?> type = targetEntry.getType();
					if (type == null) {
						serverTtl = 0L;
						clientTtl = 0;
					} else {
						if (type.getTypeBehaviorHandleToParent()) {
							final BaseEntry<?> parentEntry = targetEntry.getParent();
							if (parentEntry == null) {
								return null;
								// Reply.empty( QueryInfo.OWNER, query
								// ).setPrivate();
							}
							response = Reply.redirect(QueryInfo.OWNER, query, true, parentEntry.getLocation());
							if (privateResponse) {
								response.setPrivate();
							}
							return response;
						}
						serverTtl = privateResponse
							? 0L
							: targetEntry.getType().getTypeBehaviorResponseCacheServerTtl();
						clientTtl = targetEntry.getType().getTypeBehaviorResponseCacheClientTtl();
					}
				}
				
				/** only for cache */
				final CacheL2<ReplyAnswer> cache;
				/** only for cache */
				final String cid;
				/**
				 *
				 */
				final String key = targetEntry.getGuid();
				if (serverTtl > 30000L) {
					cache = parent.getObjectCache();
					cid = "resp:" + query.getUrl();
					response = cache.get(key, cid);
					if (response != null) {
						return response.nextClone(query);
					}
				} else {
					cache = null;
					cid = null;
				}
				
				try {
					response = targetEntry.onQuery(query);
				} catch (final AbstractReplyException e) {
					response = e.getReply();
				}
				
				if (response == null) {
					return null;
				}
				
				if (response.getCode() == Reply.CD_LOOKAT) {
					return response.setNoCaching();
				}
				
				if (this.filters != null) {
					if (response.isObject() && !response.isFinal()) {
						BaseObject object = Base.forUnknown(response.getObject());
						/** FIXME decide either all either BaseNativeObject only */
						// if (!object.baseIsPrimitive())
						{
							final ExecProcess ctx = Exec.currentProcess();
							for (;;) {
								final BaseEntry<?> entry = this.filters.pop();
								if (entry == null) {
									break;
								}
								try {
									object = entry.handleResponseFilter(ctx, object);
									if (object == null) {
										response = Reply.empty(response.getEventTypeId(), query, response.getAttributes());
										break;
									} else if (!object.baseIsPrimitive()) {
										if (this.filters.isEmpty()) {
											response = Reply.object(response.getEventTypeId(), query, response.getAttributes(), object);
											break;
										}
									} else if (object instanceof ReplyAnswer) {
										response = (ReplyAnswer) object;
										if (response.isObject() && !response.isFinal()) {
											object = Base.forUnknown(response.getObject());
										} else {
											break;
										}
									} else {
										response = Reply.object(response.getEventTypeId(), query, response.getAttributes(), object);
										break;
									}
								} catch (final AbstractReplyException e) {
									response = e.getReply();
									if (response.isObject() && !response.isFinal()) {
										object = Base.forUnknown(response.getObject());
									} else {
										break;
									}
								}
							}
						}
					}
				}
				
				if (cache != null) {
					if (clientTtl > 0) {
						response.setTimeToLiveSeconds(clientTtl);
						if (!response.isPrivate()) {
							cache.put(key, cid, response, serverTtl);
							return response.nextClone(query);
						}
						return response;
					}
					if (Base.getString(response.getAttributes(), "Content-Disposition", "").isBlank()) {
						response.setNoCaching();
					}
					if (!response.isPrivate()) {
						cache.put(key, cid, response, serverTtl);
						return response.nextClone(query);
					}
					return response;
					
				}
				
				if (privateResponse) {
					response.setPrivate();
				}
				
				return clientTtl > 0
					? response.setTimeToLiveSeconds(clientTtl)
					: Base.getString(response.getAttributes(), "Content-Disposition", "").isBlank()
						? response.setNoCaching()
						: response;
			}
			case TP_FIELD : {
				final boolean privateResponse;
				final AccessPrincipal<?> principal = parent.getServer().getAccessManager().securityCheck(
						AuthLevels.AL_AUTHORIZED_NORMAL,
						targetEntry.getLocationControl(),
						this.containsDraft
							? "execute_draft"
							: "execute");
				if (principal == null) {
					return Reply.stringForbidden(QueryInfo.OWNER, query, "No access!");
				}
				if (principal.isPerson() || ((AccessGroup<?>) principal).getAuthLevel() >= AuthLevels.AL_AUTHORIZED_AUTOMATICALLY) {
					privateResponse = true;
				} else {
					privateResponse = false;
				}
				
				ReplyAnswer response;
				try {
					response = QueryInfo.handleFieldRequest(parent, targetEntry, query, this.paramString);
				} catch (final AbstractReplyException e) {
					response = e.getReply();
				}
				
				if (privateResponse) {
					response.setPrivate();
				}
				
				return response;
			}
			case TP_REDIRECT : {
				return Reply.redirect(QueryInfo.OWNER, query, true, Context.getServer(Exec.currentProcess()).fixUrl(this.paramString)).setTimeToLiveMinutes(30);
			}
			case TP_ERROR : {
				return Reply.string(QueryInfo.OWNER, query, this.paramString).setCode(Reply.CD_EXCEPTION).setNoCaching().setPrivate()
						.setFlags(Context.getContext(Exec.currentProcess()).getFlags());
			}
			case TP_LAST_ANY : {
				if (this.lastAny == null) {
					return null;
				}
				this.type = QueryInfo.TP_QUERY;
				this.targetEntry = this.lastAny;
				query.setResourcePrefix(this.lastAnyPrefix);
				query.setResourceIdentifier(this.lastAnyIdentifier);
				return this.handleQuery(parent, query);
			}
			default : {
				return Reply.string(QueryInfo.OWNER, query, "UNKNOWN RESPONSE!").setCode(Reply.CD_EXCEPTION).setNoCaching().setPrivate()
						.setFlags(Context.getContext(Exec.currentProcess()).getFlags());
			}
		}
	}

	final void setContainsDraft() {
		
		this.containsDraft = true;
	}

	final QueryInfo setError(final String error) {
		
		this.paramString = error;
		this.type = QueryInfo.TP_ERROR;
		return this;
	}

	final QueryInfo setField(final BaseEntry<?> entry, final String fieldName) {
		
		this.targetEntry = entry;
		this.paramString = fieldName;
		this.type = QueryInfo.TP_FIELD;
		return this;
	}

	final boolean setLastAny() {
		
		this.type = QueryInfo.TP_LAST_ANY;
		return this.lastAny != null;
	}

	final void setLastAny(final BaseEntry<?> lastAny, final String lastAnyPrefix, final String lastAnyIdentifier) {
		
		this.lastAny = lastAny;
		this.lastAnyPrefix = lastAnyPrefix;
		this.lastAnyIdentifier = lastAnyIdentifier;
	}

	final QueryInfo setQuery(final BaseEntry<?> entry) {
		
		this.targetEntry = entry;
		this.type = QueryInfo.TP_QUERY;
		return this;
	}

	final QueryInfo setRedirect(final String url) {
		
		this.paramString = url;
		this.type = QueryInfo.TP_REDIRECT;
		return this;
	}

}
