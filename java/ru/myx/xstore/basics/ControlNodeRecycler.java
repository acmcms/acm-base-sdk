/*
 * Created on 20.08.2004
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ru.myx.xstore.basics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.myx.ae1.control.AbstractNode;
import ru.myx.ae1.control.Control;
import ru.myx.ae1.control.ControlNode;
import ru.myx.ae1.control.MultivariantString;
import ru.myx.ae1.storage.BaseEntry;
import ru.myx.ae1.storage.BaseRecycled;
import ru.myx.ae1.storage.StorageImpl;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseArray;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.control.ControlBasic;
import ru.myx.ae3.control.command.ControlCommand;
import ru.myx.ae3.control.command.ControlCommandset;
import ru.myx.ae3.control.field.ControlFieldFactory;
import ru.myx.ae3.control.fieldset.ControlFieldset;

/**
 * @author myx
 * 
 *         Window - Preferences - Java - Code Style - Code Templates
 */
class ControlNodeRecycler extends AbstractNode {
	private static final ControlCommand<?>	CMD_CLEAR_ALL			= Control
																			.createCommand( "clear_all",
																					MultivariantString
																							.getString( "Clear all",
																									Collections
																											.singletonMap( "ru",
																													"Удалить все" ) ) )
																			.setCommandPermission( "delete" )
																			.setCommandIcon( "command-delete" );
	
	private static final Object				STR_TITLE				= MultivariantString
																			.getString( "Recycled", Collections
																					.singletonMap( "ru", "Корзина" ) );
	
	private static final Object				STR_PASTE_THEN			= MultivariantString
																			.getString( "Objects are copied into clipboard. Choose a folder to restore in and use 'Paste...' command then",
																					Collections
																							.singletonMap( "ru",
																									"Объекты скопированы в буфер обмена. Выберите папку в выполните команду 'Вставить...'" ) );
	
	private static final Object				STR_ITEM_RESTORE		= MultivariantString.getString( "Restore",
																			Collections.singletonMap( "ru",
																					"Восстановить" ) );
	
	private static final Object				STR_ITEM_MOVE			= MultivariantString.getString( "Move", Collections
																			.singletonMap( "ru", "Переместить" ) );
	
	private static final Object				STR_ITEM_CLEAR			= MultivariantString
																			.getString( "Clear", Collections
																					.singletonMap( "ru", "Удалить" ) );
	
	private static final Object				STR_TRASHCAN_DISPOSED	= MultivariantString
																			.getString( "All elements were discarded",
																					Collections.singletonMap( "ru",
																							"Корзина очищена" ) );
	
	private static final ControlFieldset<?>	FIELDSET_LISTING		= ControlFieldset
																			.createFieldset()
																			.addField( ControlFieldFactory
																					.createFieldString( "title",
																							MultivariantString
																									.getString( "Title",
																											Collections
																													.singletonMap( "ru",
																															"Заголовок" ) ),
																							"" ) )
																			.addField( ControlFieldFactory
																					.createFieldDate( "date",
																							MultivariantString
																									.getString( "Deleted",
																											Collections
																													.singletonMap( "ru",
																															"Удален" ) ),
																							0L ) )
																			.addField( ControlFieldFactory
																					.createFieldString( "folder",
																							MultivariantString
																									.getString( "Folder",
																											Collections
																													.singletonMap( "ru",
																															"Папка" ) ),
																							"" ) )
																			.addField( ControlFieldFactory
																					.createFieldOwner( "owner",
																							MultivariantString
																									.getString( "User",
																											Collections
																													.singletonMap( "ru",
																															"Пользователь" ) ) ) );
	
	private final StorageImpl				storage;
	
	ControlNodeRecycler(final StorageImpl storage) {
		this.storage = storage;
		this.key = "$$trash";
	}
	
	@Override
	public Object getCommandResult(final ControlCommand<?> command, final BaseObject arguments) throws Exception {
		if (command == ControlNodeRecycler.CMD_CLEAR_ALL) {
			this.storage.getRecycler().clearAllRecycled();
			return ControlNodeRecycler.STR_TRASHCAN_DISPOSED;
		} else if ("restore".equals( command.getKey() )) {
			final String key = Base.getString( command.getAttributes(), "key", "" ).trim();
			final BaseRecycled record = this.storage.getRecycler().getRecycledByGuid( key );
			if (record != null && record.canRestore()) {
				record.doRestore();
			}
			return null;
		} else if ("move".equals( command.getKey() )) {
			final String key = Base.getString( command.getAttributes(), "key", "" ).trim();
			final BaseRecycled record = this.storage.getRecycler().getRecycledByGuid( key );
			if (record != null && record.canMove()) {
				Clipboard.setClipboard( this.storage, new Clipboard( Collections.singleton( record ) ) );
				return ControlNodeRecycler.STR_PASTE_THEN;
			}
			return null;
		} else if ("clear".equals( command.getKey() )) {
			final String key = Base.getString( command.getAttributes(), "key", "" ).trim();
			final BaseRecycled record = this.storage.getRecycler().getRecycledByGuid( key );
			if (record != null && record.canClean()) {
				record.doClean();
			}
			return null;
		} else {
			return super.getCommandResult( command, arguments );
		}
	}
	
	@Override
	public ControlCommandset getCommands() {
		final BaseRecycled[] recycled = this.storage.getRecycler().getRecycled();
		if (recycled == null || recycled.length == 0) {
			return null;
		}
		return Control.createOptionsSingleton( ControlNodeRecycler.CMD_CLEAR_ALL );
	}
	
	@Override
	public ControlCommandset getContentCommands(final String key) {
		final BaseRecycled record = this.storage.getRecycler().getRecycledByGuid( key );
		if (record == null) {
			return null;
		}
		final ControlCommandset result = Control.createOptions();
		if (record.canRestore()) {
			result.add( Control.createCommand( "restore", ControlNodeRecycler.STR_ITEM_RESTORE )
					.setCommandIcon( "command-create-restore" ).setAttribute( "key", key ) );
		}
		if (record.canMove()) {
			result.add( Control.createCommand( "move", ControlNodeRecycler.STR_ITEM_MOVE )
					.setCommandIcon( "command-edit-cut" ).setAttribute( "key", key ) );
		}
		if (record.canClean()) {
			result.add( Control.createCommand( "clear", ControlNodeRecycler.STR_ITEM_CLEAR )
					.setCommandIcon( "command-delete" ).setAttribute( "key", key ) );
		}
		return result;
	}
	
	@Override
	public ControlFieldset<?> getContentFieldset() {
		return ControlNodeRecycler.FIELDSET_LISTING;
	}
	
	@Override
	public ControlCommandset getContentMultipleCommands(final BaseArray keys) {
		return null;
	}
	
	@Override
	public List<ControlBasic<?>> getContents() {
		final BaseRecycled[] recycled = this.storage.getRecycler().getRecycled();
		if (recycled == null) {
			return null;
		}
		final List<ControlBasic<?>> result = new ArrayList<>();
		for (final BaseRecycled record : recycled) {
			final String title = record.getTitle();
			final String folderId = record.getFolder();
			final BaseEntry<?> folder = this.storage.getStorage().getByGuid( folderId );
			final BaseObject data = new BaseNativeObject()//
					.putAppend( "title", title )//
					.putAppend( "date", Base.forDateMillis( record.getDate() ) )//
					.putAppend( "folder", folder == null
							? "n/a"
							: folder.getLocationControl() + " - " + folder.getTitle() )//
					.putAppend( "owner", record.getOwner() )//
			;
			result.add( Control.createBasic( record.getGuid(), title, data ) );
		}
		return result;
	}
	
	@Override
	public String getIcon() {
		return "container-trashcan";
	}
	
	@Override
	public final String getLocationControl() {
		return '/' + this.storage.getMnemonicName() + "/" + this.key;
	}
	
	@Override
	public String getTitle() {
		return ControlNodeRecycler.STR_TITLE.toString();
	}
	
	@Override
	protected ControlNode<?> internGetChildByName(final String name) {
		return null;
	}
	
	@Override
	protected ControlNode<?>[] internGetChildren() {
		return null;
	}
	
	@Override
	protected ControlNode<?>[] internGetChildrenExternal() {
		return null;
	}
	
	@Override
	protected boolean internHasChildren() {
		return false;
	}
}
