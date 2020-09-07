package hmi.flipper2.value;

import java.sql.Connection;

import hmi.flipper2.FlipperException;
import hmi.flipper2.Is;

public class DbJavaValue extends JavaValue {

	private Connection db_connection;
	
	public DbJavaValue(Is is, String name) throws FlipperException {
		if ( name != null && !name.equals("default"))
				throw new FlipperException("DbJavaValue: only default db can be used at the moment");
		this.db_connection = is.getDbConnection();
		if ( this.db_connection == null)
			throw new FlipperException("DbJavaValue: no db connection");
	}
	
	@Override
	public Object getObject() throws FlipperException {
		return db_connection;
	}

	@Override
	public Class<?> objectClass() throws FlipperException {
		return Connection.class;
	}
}