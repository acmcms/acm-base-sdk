/*
 * Created on 15.06.2005
 */
package ru.myx.xstore.basics;

import java.util.Collection;
import java.util.Collections;

import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.ModuleInterface;
import ru.myx.ae1.types.Type;
import ru.myx.ae3.Engine;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.control.ControlLookupStatic;
import ru.myx.ae3.control.field.ControlField;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.exec.Exec;

/**
 * @author myx
 * 
 */
public final class Helper {
	private static final Object					NAME_GENERATOR	= new HelperNameGenerator();
	
	private static final BaseHostLookup		LOOKUP_TEMPLATE	= new HelperTypeLookup( null );
	
	private static final BaseHostLookup		LOOKUP_STATES	= Helper.getLookupStatesImpl( null );
	
	private static final BaseHostLookup		LOOKUP_ISDIR	= new ControlLookupStatic()
																		.putAppend( "false",
																				MultivariantString.getString( "File",
																						Collections.singletonMap( "ru",
																								"Файл" ) ) )
																		.putAppend( "true",
																				MultivariantString.getString( "Folder",
																						Collections.singletonMap( "ru",
																								"Папка" ) ) );
	
	private static final ControlField	FIELD_TEMPLATE	= ControlFieldFactory
																		.createFieldString( "$type",
																				MultivariantString.getString( "Type",
																						Collections.singletonMap( "ru",
																								"Тип" ) ),
																				"" )
																		.setFieldType( "select" )
																		.setAttribute( "lookup", Helper.LOOKUP_TEMPLATE );
	
	private static final ControlField	FIELD_MODIFIED	= ControlFieldFactory.createFieldDate( "$modified",
																		MultivariantString.getString( "Modified",
																				Collections.singletonMap( "ru",
																						"Изменен" ) ),
																		Engine.CURRENT_TIME );
	
	private static final ControlField	FIELD_STATE		= ControlFieldFactory
																		.createFieldInteger( "$state",
																				MultivariantString.getString( "State",
																						Collections.singletonMap( "ru",
																								"Статус" ) ),
																				ModuleInterface.STATE_DRAFT )
																		.setFieldType( "select" )
																		.setAttribute( "lookup", Helper.LOOKUP_STATES );
	
	private static final ControlField	FIELD_OWNER		= ControlFieldFactory.createFieldOwner( "$owner",
																		MultivariantString.getString( "Owner",
																				Collections.singletonMap( "ru",
																						"Владелец" ) ) );
	
	private static final ControlFieldset<?>		FIELDSET_LOAD	= ControlFieldset
																		.createFieldset()
																		.addField( ControlFieldFactory
																				.createFieldString( "$type",
																						"Type",
																						"*" ) )
																		.addField( ControlFieldFactory
																				.createFieldString( "$title",
																						"Title",
																						"Document Title!" )
																				.setAttribute( "multivariant", true ) )
																		.addField( ControlFieldFactory
																				.createFieldString( "$key",
																						"Name",
																						Helper.NAME_GENERATOR ) )
																		.addField( ControlFieldFactory
																				.createFieldOwner( "$owner", "Owner" ) )
																		.addField( ControlFieldFactory
																				.createFieldBoolean( "$folder",
																						"Entry type",
																						false ) )
																		.addField( ControlFieldFactory
																				.createFieldInteger( "$state",
																						"State",
																						ModuleInterface.STATE_DRAFT ) )
																		.addField( ControlFieldFactory.createFieldDate( "$created",
																				"Created",
																				0L ) )
																		.addField( ControlFieldFactory.createFieldDate( "$modified",
																				"Modified",
																				0L ) );
	
	/**
	 * @param type
	 * @return listing fieldset
	 */
	public static final ControlFieldset<?> getContentListingFieldset(final Type<?> type) {
		final Collection<String> fields = type == null
				? null
				: type.getContentListingFields();
		if (fields == null || fields.isEmpty()) {
			final ControlFieldset<?> result = ControlFieldset.createFieldset();
			result.addField( ControlFieldFactory.createFieldString( "$key", //
					MultivariantString.getString( "Name", //
							Collections.singletonMap( "ru", "Имя" ) ),
					"" ) );
			result.addField( ControlFieldFactory.createFieldString( "$title", //
					MultivariantString.getString( "Title", //
							Collections.singletonMap( "ru", "Заголовок" ) ),
					"" ) );
			result.addField( ControlFieldFactory.createFieldInteger( "$state", //
					MultivariantString.getString( "State", //
							Collections.singletonMap( "ru", "Статус" ) ),
					ModuleInterface.STATE_DRAFT )//
					.setFieldType( "select" )//
					.setAttribute( "lookup", Helper.LOOKUP_STATES ) );
			result.addField( Helper.FIELD_OWNER );
			result.addField( ControlFieldFactory.createFieldDate( "$created", //
					MultivariantString.getString( "Created", //
							Collections.singletonMap( "ru", "Создан" ) ),
					0L )//
					.setConstant()//
					.setAttribute( "lookup", BaseHostLookup.COMPACT_RELATIVE_DATE_FORMATTER ) );
			result.addField( ControlFieldFactory.createFieldDate( "$modified", //
					MultivariantString.getString( "Modified", //
							Collections.singletonMap( "ru", "Изменён" ) ),
					0L )//
					.setConstant()//
					.setAttribute( "lookup", BaseHostLookup.COMPACT_RELATIVE_DATE_FORMATTER ) );
			result.addField( Helper.FIELD_TEMPLATE );
			return result;
		}
		final ControlFieldset<?> result = ControlFieldset.createFieldset();
		for (final String fieldDefinition : fields) {
			final int pos = fieldDefinition.indexOf( ':' );
			final String name;
			final ControlFieldset<?> source;
			if (pos == -1) {
				source = Helper.getFieldsetEdit( type );
				name = fieldDefinition;
			} else {
				name = fieldDefinition.substring( pos + 1 ).trim();
				if (name.length() == 0) {
					continue;
				}
				final String typeName = fieldDefinition.substring( 0, pos ).trim();
				final Type<?> typeFieldset = Context.getServer( Exec.currentProcess() ).getTypes().getType( typeName );
				source = typeFieldset == null
						? Helper.getFieldsetEdit( type )
						: Helper.getFieldsetEdit( typeFieldset );
			}
			final ControlField field = "$type".equals( name )
					? Helper.FIELD_TEMPLATE
					: "$modified".equals( name )
							? Helper.FIELD_MODIFIED
							: "$state".equals( name )
									? Helper.FIELD_STATE
									: "$owner".equals( name )
											? Helper.FIELD_OWNER
											: source.getField( name );
			if (field == null) {
				final ControlField baseField = Helper.getFieldsetEdit( null ).getField( name );
				if (baseField == null) {
					result.addField( ControlFieldFactory.createFieldString( name, name, "" ) );
				}
			} else {
				if (field.getFieldClass().equals( "date" )) {
					final ControlField date = field.cloneField();
					date.setAttribute( "lookup", BaseHostLookup.COMPACT_RELATIVE_DATE_FORMATTER );
					result.addField( date );
				} else {
					result.addField( field );
				}
			}
		}
		return result;
	}
	
	/**
	 * @param type
	 * @return create fieldset
	 */
	public static ControlFieldset<?> getFieldsetCreate(final Type<?> type) {
		if (type == null) {
			return Helper.getFieldsetCreateIntern( null, null );
		}
		final Collection<Integer> validStates = type.getValidStateList();
		final ControlFieldset<?> typeFieldset = type.getFieldsetCreate();
		if (typeFieldset == null) {
			return Helper.getFieldsetCreateIntern( type.getParentType(), validStates );
		}
		return ControlFieldset
				.createFieldset( Helper.getFieldsetCreateIntern( type.getParentType(), validStates ), typeFieldset );
	}
	
	/**
	 * @param type
	 * @return create fieldset
	 */
	public static ControlFieldset<?> getFieldsetCreateClean(final Type<?> type) {
		if (type == null) {
			return null;
		}
		final ControlFieldset<?> typeFieldset = type.getFieldsetCreate();
		if (typeFieldset == null) {
			return Helper.getFieldsetCreateClean( type.getParentType() );
		}
		return typeFieldset;
	}
	
	private static ControlFieldset<?> getFieldsetCreateIntern(final Type<?> type, final Collection<Integer> validStates) {
		if (type == null) {
			final ControlFieldset<?> result = ControlFieldset.createFieldset();
			result.addField( ControlFieldFactory.createFieldString( "$title",
					MultivariantString.getString( "Title", Collections.singletonMap( "ru", "Заголовок" ) ),
					"New Document!" ) );
			result.addField( ControlFieldFactory.createFieldString( "$key",
					MultivariantString.getString( "Name", Collections.singletonMap( "ru", "Имя" ) ),
					Helper.NAME_GENERATOR ) );
			result.addField( ControlFieldFactory.createFieldBoolean( "$folder", //
					MultivariantString.getString( "Entry type", Collections.singletonMap( "ru", "Тип объекта" ) ),
					false )//
					.setFieldType( "select" )//
					.setAttribute( "lookup", Helper.LOOKUP_ISDIR ) //
			);
			result.addField( ControlFieldFactory.createFieldDate( "$created", //
					MultivariantString.getString( "Created", Collections.singletonMap( "ru", "Создан" ) ),
					Engine.CURRENT_TIME ) );
			if (validStates != null && !validStates.isEmpty()) {
				result.addField( ControlFieldFactory.createFieldInteger( "$state", //
						MultivariantString.getString( "State", Collections.singletonMap( "ru", "Статус" ) ),
						ModuleInterface.STATE_DRAFT )//
						.setFieldType( "select" )//
						.setAttribute( "lookup", Helper.getLookupStatesImpl( validStates ) ) );
			}
			return result;
		}
		final ControlFieldset<?> typeFieldset = type.getFieldsetCreate();
		if (typeFieldset == null) {
			return Helper.getFieldsetCreateIntern( type.getParentType(), validStates );
		}
		return ControlFieldset
				.createFieldset( Helper.getFieldsetCreateIntern( type.getParentType(), validStates ), typeFieldset );
	}
	
	/**
	 * @return default fieldset
	 */
	public static final ControlFieldset<?> getFieldsetDefault() {
		return Helper.FIELDSET_LOAD;
	}
	
	/**
	 * @param type
	 * @return edit fieldset
	 */
	public static ControlFieldset<?> getFieldsetEdit(final Type<?> type) {
		if (type == null) {
			return Helper.getFieldsetEditIntern( null, null );
		}
		final Collection<Integer> validStates = type.getValidStateList();
		final ControlFieldset<?> typeFieldset = type.getFieldsetProperties();
		if (typeFieldset == null) {
			return Helper.getFieldsetEditIntern( type.getParentType(), validStates );
		}
		return ControlFieldset
				.createFieldset( Helper.getFieldsetEditIntern( type.getParentType(), validStates ), typeFieldset );
	}
	
	private static ControlFieldset<?> getFieldsetEditIntern(final Type<?> type, final Collection<Integer> validStates) {
		if (type == null) {
			final ControlFieldset<?> result = ControlFieldset.createFieldset();
			result.addField( ControlFieldFactory.createFieldString( "$title", //
					MultivariantString.getString( "Title", Collections.singletonMap( "ru", "Заголовок" ) ),
					"Document Title!" ) );
			result.addField( ControlFieldFactory.createFieldString( "$key", //
					MultivariantString.getString( "Name", Collections.singletonMap( "ru", "Имя" ) ),
					Helper.NAME_GENERATOR ) );
			result.addField( ControlFieldFactory.createFieldBoolean( "$folder", //
					MultivariantString.getString( "Entry type", //
							Collections.singletonMap( "ru", "Тип объекта" ) ),
					false )//
					.setFieldType( "select" )//
					.setAttribute( "lookup", Helper.LOOKUP_ISDIR ) //
			);
			if (validStates != null && !validStates.isEmpty()) {
				result.addField( ControlFieldFactory
						.createFieldInteger( "$state",
								MultivariantString.getString( "State", Collections.singletonMap( "ru", "Статус" ) ),
								ModuleInterface.STATE_DRAFT ).setFieldType( "select" )
						.setAttribute( "lookup", Helper.getLookupStatesImpl( validStates ) ) );
			}
			result.addField( ControlFieldFactory.createFieldDate( "$created",
					MultivariantString.getString( "Created", Collections.singletonMap( "ru", "Создан" ) ),
					0L ) );
			return result;
		}
		final ControlFieldset<?> typeFieldset = type.getFieldsetProperties();
		if (typeFieldset == null) {
			return Helper.getFieldsetEditIntern( type.getParentType(), validStates );
		}
		return ControlFieldset
				.createFieldset( Helper.getFieldsetEditIntern( type.getParentType(), validStates ), typeFieldset );
	}
	
	/**
	 * @param type
	 * @return properties fieldset
	 */
	public static ControlFieldset<?> getFieldsetPropertiesClean(final Type<?> type) {
		if (type == null) {
			return null;
		}
		final ControlFieldset<?> typeFieldset = type.getFieldsetProperties();
		return typeFieldset == null
				? Helper.getFieldsetPropertiesClean( type.getParentType() )
				: typeFieldset;
	}
	
	private static final BaseHostLookup getLookupStatesImpl(final Collection<Integer> filter) {
		final ControlLookupStatic result = new ControlLookupStatic();
		if (filter == null || filter.contains( new Integer( ModuleInterface.STATE_DRAFT ) )) {
			result.putAppend( "" + ModuleInterface.STATE_DRAFT,
					MultivariantString.getString( "Draft", Collections.singletonMap( "ru", "Черновик" ) ) );
		}
		if (filter == null || filter.contains( new Integer( ModuleInterface.STATE_READY ) )) {
			result.putAppend( "" + ModuleInterface.STATE_READY,
					MultivariantString.getString( "Ready", Collections.singletonMap( "ru", "Готов" ) ) );
		}
		if (filter == null || filter.contains( new Integer( ModuleInterface.STATE_SYSTEM ) )) {
			result.putAppend( "" + ModuleInterface.STATE_SYSTEM,
					MultivariantString.getString( "System", Collections.singletonMap( "ru", "Системный" ) ) );
		}
		if (filter == null || filter.contains( new Integer( ModuleInterface.STATE_PUBLISHED ) )) {
			result.putAppend( "" + ModuleInterface.STATE_PUBLISHED,
					MultivariantString.getString( "Published", Collections.singletonMap( "ru", "Опубликованный" ) ) );
		}
		if (filter == null || filter.contains( new Integer( ModuleInterface.STATE_ARCHIEVED ) )) {
			result.putAppend( "" + ModuleInterface.STATE_ARCHIEVED,
					MultivariantString.getString( "Archive", Collections.singletonMap( "ru", "Архивный" ) ) );
		}
		if (filter == null || filter.contains( new Integer( ModuleInterface.STATE_DEAD ) )) {
			result.putAppend( "" + ModuleInterface.STATE_DEAD,
					MultivariantString.getString( "Dead", Collections.singletonMap( "ru", "Устаревший" ) ) );
		}
		return result;
	}
	
	/**
	 * @param entry
	 * @return lookup
	 */
	public static final BaseHostLookup getLookupValidChildrenTypes(final BaseEntry<?> entry) {
		if (entry == null) {
			return Helper.LOOKUP_TEMPLATE;
		}
		final Type<?> type = entry.getType();
		return type == null || type.getValidChildrenTypeNames() == null && type.getValidParentsTypeNames() == null
				? Helper.LOOKUP_TEMPLATE
				: new HelperTypeLookup( type );
	}
	
	/**
	 * @param entry
	 * @return lookup
	 */
	public static final BaseHostLookup getLookupValidStates(final BaseEntry<?> entry) {
		if (entry == null) {
			return Helper.LOOKUP_STATES;
		}
		final Type<?> type = entry.getType();
		if (type == null) {
			return Helper.LOOKUP_STATES;
		}
		final Collection<Integer> validStates = type.getValidStateList();
		if (validStates == null) {
			return Helper.LOOKUP_STATES;
		}
		return Helper.getLookupStatesImpl( validStates );
	}
}
