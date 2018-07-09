/*
 * Created on 13.04.2004
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ru.myx.xstore.forms;

import java.util.Collections;
import java.util.Set;

import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseChange;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae1.types.Type;
import ru.myx.ae1.types.TypeRegistry;
import ru.myx.ae3.act.Context;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseHostLookup;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.AbstractForm;
import ru.myx.ae3.control.ControlLookupStatic;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.help.Convert;
import ru.myx.ae3.help.Create;
import ru.myx.jdbc.lock.Locker;
import ru.myx.xstore.basics.Helper;

/**
 * @author myx
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FormEntryChangeTemplate extends AbstractForm<FormEntryChangeTemplate> {
	private static final Object				STR_VERSIONING			= MultivariantString.getString( "Version control",
																			Collections.singletonMap( "ru",
																					"Контроль версий" ) );
	
	private static final BaseHostLookup		LOOKUP_VERSION_MODES	= new ControlLookupStatic()
																			.putAppend( "-",
																					MultivariantString
																							.getString( "Default mode - as designed in type definition",
																									Collections
																											.singletonMap( "ru",
																													"По умолчанию - как задумано в описании типа" ) ) )
																			.putAppend( "true",
																					MultivariantString
																							.getString( "Version control: activated",
																									Collections
																											.singletonMap( "ru",
																													"Контроль версий: включен" ) ) )
																			.putAppend( "false",
																					MultivariantString
																							.getString( "Version control: deactivated",
																									Collections
																											.singletonMap( "ru",
																													"Контроль версий: отключен" ) ) );
	
	private static final ControlCommand<?>	CMD_CLOSE				= Control.createCommand( "close", " * " )
																			.setCommandIcon( "command-close" );
	
	private static final ControlCommand<?>	CMD_NEXT				= Control
																			.createCommand( "next",
																					MultivariantString
																							.getString( "Next...",
																									Collections
																											.singletonMap( "ru",
																													"Далее..." ) ) )
																			.setCommandPermission( "modify" )
																			.setCommandIcon( "command-next" );
	
	private static final ControlCommand<?>	CMD_NEXT_PUBLISHED		= Control
																			.createCommand( "next",
																					MultivariantString
																							.getString( "Next...",
																									Collections
																											.singletonMap( "ru",
																													"Далее..." ) ) )
																			.setCommandPermission( "publish" )
																			.setCommandIcon( "command-next" );
	
	private static final ControlCommand<?>	CMD_INFO				= Control
																			.createCommand( "info",
																					MultivariantString
																							.getString( "Info...",
																									Collections
																											.singletonMap( "ru",
																													"Информация..." ) ) )
																			.setCommandPermission( "create" )
																			.setCommandIcon( "command-info" );
	
	private final StorageImpl				plugin;
	
	private final BaseChange				change;
	
	private final Locker					lock;
	
	private final ControlFieldset<?>		fieldset;
	
	private final boolean					published;
	
	/**
	 * @param plugin
	 * @param change
	 * @param lock
	 */
	public FormEntryChangeTemplate(final StorageImpl plugin, final BaseChange change, final Locker lock) {
		this.plugin = plugin;
		this.change = change;
		this.lock = lock;
		final BaseEntry<?> section = plugin.getStorage().getByGuid( change.getParentGuid() );
		this.fieldset = ControlFieldset.createFieldset( "choose_template" ).addField( ControlFieldFactory
				.createFieldString( "template",
						MultivariantString.getString( "Type", Collections.singletonMap( "ru", "Тип" ) ),
						change.getTypeName() ).setFieldType( "select" ).setFieldVariant( "bigselect" )
				.setAttribute( "lookup", Helper.getLookupValidChildrenTypes( section ) ) );
		if (plugin.areObjectVersionsSupported()) {
			this.fieldset.addField( ControlFieldFactory
					.createFieldString( "versioning",
							FormEntryChangeTemplate.STR_VERSIONING,
							String.valueOf( change.getVersioning() ) ).setFieldType( "select" )
					.setAttribute( "lookup", FormEntryChangeTemplate.LOOKUP_VERSION_MODES ) );
		}
		this.setAttributeIntern( "id", "choose_template" );
		this.setAttributeIntern( "title",
				MultivariantString.getString( "Choose entry type",
						Collections.singletonMap( "ru", "Выбор типа объекта" ) ) );
		this.setAttributeIntern( "path", change.getLocationControl() );
		
		if (lock != null) {
			this.setAttributeIntern( "on_close", FormEntryChangeTemplate.CMD_CLOSE );
		}
		
		this.published = change.getState() > 0;
		this.recalculate();
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) {
		if (command == FormEntryChangeTemplate.CMD_INFO) {
			final TypeRegistry types = Context.getServer( Exec.currentProcess() ).getTypes();
			final String typeName = Base.getString( this.getData(), "template", types.getTypeNameDefault() );
			final Type<?> type = types.getType( typeName );
			return new FormTypeInfo( type, this, this.published
					? FormEntryChangeTemplate.CMD_NEXT_PUBLISHED
					: FormEntryChangeTemplate.CMD_NEXT );
		}
		if (command == FormEntryChangeTemplate.CMD_CLOSE) {
			if (this.lock != null) {
				this.lock.lockRelease();
			}
			return null;
		}
		if (command == FormEntryChangeTemplate.CMD_NEXT || command == FormEntryChangeTemplate.CMD_NEXT_PUBLISHED) {
			final TypeRegistry types = Context.getServer( Exec.currentProcess() ).getTypes();
			final String template = Base.getString( this.getData(), "template", types.getTypeNameDefault() );
			final Type<?> type = types.getType( template );
			final boolean versioning = Convert.MapEntry.toBoolean( this.getData(),
					"versioning",
					type.getDefaultVersioning() );
			this.change.setTypeName( template );
			this.change.setVersioning( versioning );
			final ControlFieldset<?> fieldset = type.getFieldsetProperties();
			if (fieldset == null) {
				return new FormEntryEdit( this.plugin, this.change, this.lock );
			}
			final Set<String> innerFields = fieldset.innerFields();
			final Set<String> realFields = Create.tempSet( this.change.getData().baseKeysOwn() );
			realFields.removeAll( innerFields );
			realFields.removeAll( Helper.getFieldsetEdit( null ).innerFields() );
			realFields.removeAll( Helper.getFieldsetDefault().innerFields() );
			realFields.remove( "$type" );
			if (realFields.isEmpty()) {
				return new FormEntryEdit( this.plugin, this.change, this.lock );
			}
			return new FormEntryChangeTemplateCleanup( this.plugin, this.change, this.lock, realFields, template );
		}
		throw new IllegalArgumentException( "Unknown command: " + command.getKey() );
	}
	
	@Override
	public ControlCommandset getCommands() {
		final ControlCommandset result = Control.createOptions();
		if (this.published) {
			result.add( FormEntryChangeTemplate.CMD_NEXT_PUBLISHED );
			result.add( FormEntryChangeTemplate.CMD_INFO );
		} else {
			result.add( FormEntryChangeTemplate.CMD_NEXT );
			result.add( FormEntryChangeTemplate.CMD_INFO );
		}
		return result;
	}
	
	@Override
	public ControlFieldset<?> getFieldset() {
		return this.fieldset;
	}
}
