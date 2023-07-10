package jss.database.types;

/**
 * Fabryka bazy danych
 * 
 * @author lukas
 */
public class DatabaseFactory {
	
	public static AbstractDatabase getDatabase(DatabaseType type) {
		switch(type) {
			case MySQL:
				return new MysqlDatabase();
			case PostgreSQL:
				return new PostgresqlDatabase();
			case SQLite:
				return new SQLiteDatabase();
			default:
				return null;
		}
	}
	
}
