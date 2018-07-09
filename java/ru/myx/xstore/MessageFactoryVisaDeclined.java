package ru.myx.xstore;

import java.util.Collections;

import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.messaging.Message;
import ru.myx.ae1.messaging.MessageFactory;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseMap;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.ControlForm;
import ru.myx.ae3.produce.ObjectFactory;

/*
 * Created on 09.10.2004
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
/**
 * @author myx
 *
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public final class MessageFactoryVisaDeclined implements ObjectFactory<Object, MessageFactory>, MessageFactory {

	private static final String[] VARIETY = {
			"STORAGE_VISA_DECLINED"
	};

	private static final Class<?>[] TARGETS = {
			MessageFactory.class
	};

	private static final BaseObject STR_MESSAGE_TITLE = MultivariantString.getString(//
			"Visa request: declined", //
			Collections.singletonMap("ru", "Запрос на визирование: отклонен")//
	);

	@Override
	public final boolean accepts(final String variant, final BaseObject attributes, final Class<?> source) {

		return true;
	}

	@Override
	public BaseMap createExternalMessage(final Message message) {

		final BaseMap result = new BaseNativeObject()//
				.putAppend("Subject", MessageFactoryVisaDeclined.STR_MESSAGE_TITLE)//
				.putAppend(
						"Body", //
						"Visa declined, comment:\r\n" + Base.getString(message.getMessageFactoryParams(), "comment", "").trim())//
		;
		return result;
	}

	@Override
	public ControlForm<?> createMessageForm(final Message message) {

		return null;
	}

	@Override
	public String getMessageTitle(final Message message) {

		return MessageFactoryVisaDeclined.STR_MESSAGE_TITLE.toString();
	}

	@Override
	public String getTitle() {

		return "Storage message factory";
	}

	@Override
	public boolean isExternalSupported() {

		return true;
	}

	@Override
	public boolean isFormSupported() {

		return false;
	}

	@Override
	public final MessageFactory produce(final String variant, final BaseObject attributes, final Object source) {

		return this;
	}

	@Override
	public final Class<?>[] sources() {

		return null;
	}

	@Override
	public final Class<?>[] targets() {

		return MessageFactoryVisaDeclined.TARGETS;
	}

	@Override
	public final String[] variety() {

		return MessageFactoryVisaDeclined.VARIETY;
	}
}
