/*
 * Created on 27.05.2004
 */
package ru.myx.xstore.schedule;

import ru.myx.ae1.provide.ProvideRunner;
import ru.myx.ae1.provide.TaskRunner;
import ru.myx.ae1.schedule.Action;
import ru.myx.ae1.types.Type;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseMap;
import ru.myx.ae3.base.BaseNativeObject;
import ru.myx.ae3.base.BaseObject;

/**
 * 
 * !!! Не должно ли быть всё это прям в Action?
 * 
 * @author myx
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
final class ActionBasic extends Action<ActionBasic> {
	private final Type<?>		type;
	
	private BaseMap	data	= null;
	
	ActionBasic(final Type<?> type, final Action<?> action) {
		super( action );
		this.type = type;
	}
	
	ActionBasic(final Type<?> type,
			final String key,
			final long date,
			final String name,
			final String actor,
			final BaseObject data) {
		super( key, date, name, actor, data );
		this.type = type;
	}
	
	@Override
	public BaseMap getData() {
		if (this.data == null) {
			final BaseMap data = new BaseNativeObject()//
					.putAppend( "title", this.getTitle() )//
					.putAppend( "name", this.getName() )//
					.putAppend( "state", this.getState() )//
					.putAppend( "date", Base.forDateMillis( this.getDate() ) )//
					.putAppend( "owner", this.getOwner() )//
			;
			return this.data = data;
		}
		return this.data;
	}
	
	@Override
	public String getIcon() {
		return null;
	}
	
	@Override
	public String getTitle() {
		final TaskRunner runner = ProvideRunner.forName( this.getActor() );
		if (runner == null) {
			final BaseObject attributes = this.type.getCommandAttributes( this.getActor() );
			return Base.getString( attributes, "title", this.getActor() );
		}
		return runner.describe( this.data );
	}
}
