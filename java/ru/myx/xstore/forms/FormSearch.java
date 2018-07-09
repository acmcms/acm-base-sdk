/*
 * Created on 12.01.2005
 */
package ru.myx.xstore.forms;

import java.util.Collections;
import java.util.List;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.ControlBasic;
import ru.myx.ae3.control.ControlLookupStatic;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.help.Convert;
import ru.myx.xstore.basics.Helper;

/**
 * @author myx
 */
public class FormSearch extends AbstractForm<FormSearch> {
	
	private static final BaseObject STR_TEXT = MultivariantString.getString("Text", Collections.singletonMap("ru", "Текст"));
	
	private static final BaseObject STR_PATH = MultivariantString.getString("Path", Collections.singletonMap("ru", "Путь"));
	
	private static final BaseObject STR_ALL = MultivariantString.getString("Within", Collections.singletonMap("ru", "Среди"));
	
	private static final BaseObject STR_SORT = MultivariantString.getString("Sort", Collections.singletonMap("ru", "Порядок"));
	
	private static final BaseObject STR_RESULTS = MultivariantString.getString("Search results", Collections.singletonMap("ru", "Результаты поиска"));
	
	private static final BaseHostLookup LOOKUP_ALL = new ControlLookupStatic()
			.putAppend("true", MultivariantString.getString("All but draft", Collections.singletonMap("ru", "Всех, кроме черновиков")))
			.putAppend("false", MultivariantString.getString("Only published and archived", Collections.singletonMap("ru", "Только опубликованных и архивных")));
			
	private static final BaseHostLookup LOOKUP_SORT = new ControlLookupStatic()
			.putAppend("*", MultivariantString.getString("Relevance", Collections.singletonMap("ru", "по релевантности")))
			.putAppend("history", MultivariantString.getString("History (newer first)", Collections.singletonMap("ru", "история (новые в начале списка)")))
			.putAppend("log", MultivariantString.getString("Log (newest last)", Collections.singletonMap("ru", "Журнал (новые в конце списка)")))
			.putAppend("alphabet", MultivariantString.getString("Alphabet (by title ascending)", Collections.singletonMap("ru", "Алфавитный (по заголовку, по нарастающей)")));
			
	private final ControlFieldset<?> fieldset;
	
	private static final BaseObject STR_TITLE = MultivariantString.getString("Search", Collections.singletonMap("ru", "Поиск"));
	
	private final BaseEntry<?> parent;
	
	private final ControlFieldset<?> listing;
	
	private static final BaseObject STR_SEARCH = MultivariantString.getString("Search", Collections.singletonMap("ru", "Искать"));
	
	private static final ControlCommand<?> CMD_SEARCH = Control.createCommand("search", FormSearch.STR_SEARCH).setCommandIcon("command-search");
	
	/**
	 * @param parent
	 */
	public FormSearch(final BaseEntry<?> parent) {
		this.parent = parent;
		this.setAttributeIntern("id", "search");
		this.setAttributeIntern("title", FormSearch.STR_TITLE);
		this.recalculate();
		this.listing = Helper.getContentListingFieldset(null);
		final ControlFieldset<?> listingFieldset = ControlFieldset.createFieldset().addField(ControlFieldFactory.createFieldString("$path", FormSearch.STR_PATH, "", 1, 255))
				.addFields(this.listing);
		this.fieldset = ControlFieldset.createFieldset().addField(ControlFieldFactory.createFieldString("text", FormSearch.STR_TEXT, "", 1, 255))
				.addField(ControlFieldFactory.createFieldBoolean("all", FormSearch.STR_ALL, false).setFieldType("select").setAttribute("lookup", FormSearch.LOOKUP_ALL))
				.addField(ControlFieldFactory.createFieldString("sort", FormSearch.STR_SORT, "*").setFieldType("select").setAttribute("lookup", FormSearch.LOOKUP_SORT)).addField(
						Control.createFieldList("result", FormSearch.STR_RESULTS, null).setAttribute("content_fieldset", listingFieldset)
								.setAttribute("content_handler", new EntryListingContainerProvider(parent.getStorageImpl(), null)));
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) throws Exception {
		
		if (command == FormSearch.CMD_SEARCH) {
			final String text = Base.getString(this.getData(), "text", "").trim();
			final boolean all = Convert.MapEntry.toBoolean(this.getData(), "all", false);
			final String sort = Base.getString(this.getData(), "sort", "*").replace('*', ' ').trim();
			final long startDate = -1L;
			final long endDate = -1L;
			final List<ControlBasic<?>> files = this.parent.search(0, all, 45000L, sort, startDate, endDate, text);
			this.getData().baseDefine("result", Base.forArray(new EntryListingFilter(this.listing, files, this.parent, null, null)));
			return this;
		}
		return super.getCommandResult(command, arguments);
	}
	
	@Override
	public ControlCommandset getCommands() {
		
		return Control.createOptionsSingleton(FormSearch.CMD_SEARCH);
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		
		return this.fieldset;
	}
}
