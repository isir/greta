package hmi.flipper2.postgres;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import hmi.flipper2.FlipperException;
import hmi.flipper2.TemplateController;
import hmi.flipper2.TemplateFile;
import java.io.FileInputStream;

public class Database {
	
	private static Logger logger = LoggerFactory.getLogger(Database.class);
	
	private Connection conn = null;
	private Properties props = null;
	
	public void commit() throws FlipperException {
		try {
			// System.out.println("COMMIT!");
			conn.commit();
		} catch (SQLException e) {
			throw new FlipperException(e);
		}
	}
	
	public void rollback() throws FlipperException {
		try {
			conn.rollback();
		} catch (SQLException e) {
			throw new FlipperException(e);
		}
	}
	
	private Database() {
	}

	public Database(String url, String user, String password) throws FlipperException {
		try {
			logger.trace("Database():connecting to:"+url+" | "+user, Database.class.getName());
			try {
				Class.forName("org.postgresql.Driver");
			} catch (ClassNotFoundException e) {
				throw new FlipperException("Class org.postgresql.Driver not found");
			}
			this.props = new Properties();
			props.setProperty("url", url);
			props.setProperty("user", user);
			props.setProperty("password", password);
			this.conn = DriverManager.getConnection(url, props);
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			throw new FlipperException(e,"Connection Details: "+props);
		}
	}
	
	public Connection getConnection() {
		return this.conn;
	}
	
	public void clearAll() throws FlipperException {
		try {
			Statement st = conn.createStatement();
			st.execute("DROP TABLE IF EXISTS flipper;");
			st.execute("DROP TABLE IF EXISTS flipper_tf;");
			st.execute("DROP SEQUENCE IF EXISTS flipper_global_id;");
			st.execute("CREATE SEQUENCE flipper_global_id;");
			st.execute("CREATE TABLE flipper("+
						"cid INT PRIMARY KEY DEFAULT nextval('flipper_global_id')," +
						"name TEXT UNIQUE," +
					    "description TEXT," +
						"created TIMESTAMP" +
					    ");");
			st.execute("CREATE TABLE flipper_tf("+
					"tfid INT DEFAULT nextval('flipper_global_id')," +
					"cid INT," +
					"name TEXT PRIMARY KEY," +
					"path TEXT," +
					"xml TEXT," +
					"json_is JSONB," +
					"sync_is BIGINT DEFAULT 0," +
					"created TIMESTAMP," +
					"updated TIMESTAMP" +
				    ");");
			conn.commit();
		} catch (SQLException e) {
			throw new FlipperException(e);
		}
	}
	
	public void executeScript(String script) throws FlipperException {
		try {
			Statement st = conn.createStatement();
			st.addBatch(script);
			st.executeBatch();
		} catch (SQLException e) {
			throw new FlipperException(e);
		}
	}
	
	public void close() throws SQLException {
		conn.close();
	}
	
	public void createController(String name, String descr) throws FlipperException {
		try {
			String insertTableSQL = "INSERT INTO flipper" + "(name,description,created) VALUES" + "(?,?,?)";
			PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
			preparedStatement.setString(1, name);
			preparedStatement.setString(2, descr);
			preparedStatement.setTimestamp(3, getCurrentTimeStamp());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new FlipperException(e);
		}
	}
	
	public void destroyController(String name) throws FlipperException {
		try {
			PreparedStatement ps;
			
			int cid = getControllerID(name);
			//			//
			String delSQL1 = "DELETE FROM flipper_tf WHERE cid = ?;";
			ps = conn.prepareStatement(delSQL1);
			ps.setInt(1, cid);
			ps.executeUpdate();
			//
			String delSQL2 = "DELETE FROM flipper WHERE cid = ?;";
			ps = conn.prepareStatement(delSQL2);
			ps.setInt(1, cid);
			ps.executeUpdate();
			
		} catch (SQLException e) {
			throw new FlipperException(e);
		}
	}

	public void addTemplateFile(TemplateController tc, TemplateFile tf) throws FlipperException {
		try {
			String insertTableSQL = "INSERT INTO flipper_tf" + "(cid,name,path,xml,json_is,created) VALUES" + "(?,?,?,?,to_json(?::json),?) RETURNING tfid";
			PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
			preparedStatement.setInt(1, tc.cid);
			preparedStatement.setString(2, tf.id());
			preparedStatement.setString(3, tf.path);
			preparedStatement.setString(4, tf.xml_str);
			preparedStatement.setString(5, tf.is_json_value);
			preparedStatement.setTimestamp(6, getCurrentTimeStamp());
			ResultSet rs = preparedStatement.executeQuery();
			if ( rs.next() )
	            tf.tfid =  rs.getInt("tfid");
			else
				throw new FlipperException("addTemplateFile: UNEXPECTED RESULT");     
		} catch (SQLException e) {
			throw new FlipperException(e);
		}
	}
	
	public void removeTemplateFile(TemplateController tc, TemplateFile tf) throws FlipperException {
		try {
			String deleteTableSQL = "DELETE FROM flipper_tf WHERE tfid = ?;";
			PreparedStatement preparedStatement = conn.prepareStatement(deleteTableSQL);
			preparedStatement.setInt(1, tf.tfid);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new FlipperException(e);
		}
	}
	
	public void updateTemplateFileIs(TemplateFile tf, String is_value) throws FlipperException {
		try {
			// System.out.println("UPDATE TEMPLATE FILE (tf_id="+tf.tfid+"): "+ is_value);
			String updateTableSQL = "UPDATE flipper_tf SET json_is = to_json(?::json), sync_is = sync_is+1, updated = ? WHERE tfid = ? RETURNING sync_is;";
			PreparedStatement preparedStatement = conn.prepareStatement(updateTableSQL);
			preparedStatement.setString(1, is_value);
			preparedStatement.setTimestamp(2, getCurrentTimeStamp());
			preparedStatement.setInt(3, tf.tfid);
			ResultSet rs = preparedStatement.executeQuery();
			if ( rs.next() ) {
	            long new_sync_is = rs.getLong("sync_is");
	            tf.sync_is += 1;
	            if ( new_sync_is != tf.sync_is)
	            	throw new FlipperException("updateTemplateFileIs:Is:"+tf.is_name+":concurrent update error");
			} else
				throw new FlipperException("updateTemplateFileIs: UNEXPECTED RESULT");  
		} catch (SQLException e) {
			throw new FlipperException(e);
		}
	}
	
	public List<TemplateFile> getTemplateFiles(TemplateController tc) throws FlipperException {
		List<TemplateFile> res = new ArrayList<TemplateFile>();
		
		// System.out.println("INCOMPLETE:Database:getTemplateFiles");
		try {
			String selectSQL = "SELECT tfid,path,xml,json_is#>>'{}' AS json_is,sync_is FROM flipper_tf WHERE cid = ?;";
			PreparedStatement preparedStatement = conn.prepareStatement(selectSQL);
			preparedStatement.setInt(1, tc.cid);
			ResultSet rs = preparedStatement.executeQuery();
			while ( rs.next() ) {
				int tfid = rs.getInt("tfid");
	            String path = rs.getString("path");
	            String xml = rs.getString("xml");
	            String json_is = rs.getString("json_is");
	            long sync_is = rs.getLong("sync_is");
	            //
	            TemplateFile tf = new TemplateFile(tc, path, xml, json_is, sync_is);
	            tf.tfid = tfid;
	            res.add(tf);
			}
		} catch (SQLException e) {
			throw new FlipperException(e);
		}
		return res;
	}
	
	public int getControllerID(String name) throws FlipperException  {
		try {
			String selectSQL = "SELECT cid FROM flipper WHERE name = ?;";
			PreparedStatement preparedStatement = conn.prepareStatement(selectSQL);
			preparedStatement.setString(1, name);
			ResultSet rs = preparedStatement.executeQuery();
			if ( rs.next() )
	            return rs.getInt("cid");
	        throw new FlipperException("Flipper controller \""+name+"\" not found.");           
		} catch (SQLException e) {
			throw new FlipperException(e);
		}
	}
	
	public void create_Person_Table() throws SQLException {
		Statement st = conn.createStatement();
		st.execute("DROP TABLE IF EXISTS person;");
		st.execute("CREATE TABLE person(firstname text, lastname text, age integer);");
		st.close();
		PreparedStatement sti = conn.prepareStatement("INSERT INTO person VALUES(?, ?, ?);");
		sti.setString(1, "Jan");
		sti.setString(2, "Flokstra");
		sti.setInt(3, 33);
		sti.execute();
		sti.setString(1, "Pietje");
		sti.setString(2, "Puk");
		sti.setInt(3, 19);
		sti.execute();
		
//		JsonObject personObject = Json.createObjectBuilder()
//                .add("name", "John")
//                .add("age", 13)
//                .build();
		
		sti.close();
	}
	
	private static java.sql.Timestamp getCurrentTimeStamp() {

		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());

	}
	
	/*
	 * 
	 * 
	 */
	
	private static final String DB_CONFIG_FILE_NAME = "db.conf.local";
	
	
	public static Database openDatabaseFromConfig() throws FlipperException{
		String host =null, database = null, role = null, password = null;
		InputStream inputStream = null;
		try {
			Properties prop = new Properties();
                         InputStream inputstream = null;

                        inputstream = new FileInputStream(DB_CONFIG_FILE_NAME);


                        
			//inputStream = (new Database()).getClass().getClassLoader().getResourceAsStream(DB_CONFIG_FILE_NAME);
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FlipperException("\n! No DB config property file '" + DB_CONFIG_FILE_NAME + "' found in the resources directory.\n!Copy db.conf.distribute to db.conf.local and fill in the connection details. ");
			} 
			host = prop.getProperty("host");
			database = prop.getProperty("database");
			role = prop.getProperty("role");
			password = prop.getProperty("password");
		} catch (Exception e) {
			throw new FlipperException("\n!No (or bad) DB config property file '" + DB_CONFIG_FILE_NAME + "' found in the resources directory.\n!Copy db.conf.distribute to db.conf.local and fill in the connection details. ");
		} finally {	
			try {
				if ( inputStream != null )
					inputStream.close();
			} catch (IOException e) {
				// IGNORE
			}
		}
		return new Database("jdbc:postgresql://"+host+"/"+database, role, password);
	}
	
	
	
	
}
