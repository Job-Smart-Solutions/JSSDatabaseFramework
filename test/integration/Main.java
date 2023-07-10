package integration;

import org.junit.Ignore;

import integration.tables.BinaryAndJsonTable;
import integration.tables.DateTimeTable;
import integration.tables.DecimalTable;
import integration.tables.NumberObjTable;
import integration.tables.NumberTable;
import integration.tables.TableWithIndexes;
import integration.tables.TableWithIndexes2;
import integration.tables.TextTable;
import lukas.database.DatabaseConfig;
import lukas.database.DatabaseException;
import lukas.database.TableManager;
import lukas.database.types.DatabaseType;

@Ignore
public class Main {

	public static void main(String[] args) throws DatabaseException {
		// cleanup tests
		CreateViews.cleanup();

		// start tests
		new CreateTables();
		new AddingItems();
		new SelectTest();
		new MultiSelectTest();
		new CreateViews();
		new SelectViews();
	}

	static DatabaseConfig createSqlite() throws DatabaseException {
		DatabaseConfig cfg = new DatabaseConfig(DatabaseType.SQLite);
		cfg.setServer("test.db");
		cfg.setCreateTables(false);
		return cfg;
	}

	static DatabaseConfig createMysql() throws DatabaseException {
		DatabaseConfig cfg = new DatabaseConfig(DatabaseType.MySQL);
		cfg.setServer("localhost").setUser("USER").setPass("PASS").setDbName("dbtest");
		cfg.setCreateTables(false);
		return cfg;
	}

	static DatabaseConfig createPostgresql() throws DatabaseException {
		DatabaseConfig cfg = new DatabaseConfig(DatabaseType.PostgreSQL);
		cfg.setServer("localhost").setUser("USER").setPass("PASS").setDbName("dbtest");
		cfg.setCreateTables(false);
		return cfg;
	}

	static void dropTables(TableManager manager) throws DatabaseException {
		manager.deleteTable(TableWithIndexes2.class);
		manager.deleteTable(TableWithIndexes.class);
		manager.deleteTable(BinaryAndJsonTable.class);
		manager.deleteTable(DecimalTable.class);
		manager.deleteTable(DateTimeTable.class);
		manager.deleteTable(TextTable.class);
		manager.deleteTable(NumberObjTable.class);
		manager.deleteTable(NumberTable.class);
	}

	static void addTablesToConfig(DatabaseConfig cfg) throws DatabaseException {
		cfg.addTableClass(NumberTable.class);
		cfg.addTableClass(NumberObjTable.class);
		cfg.addTableClass(TextTable.class);
		cfg.addTableClass(DateTimeTable.class);
		cfg.addTableClass(DecimalTable.class);
		cfg.addTableClass(BinaryAndJsonTable.class);
		cfg.addTableClass(TableWithIndexes.class);
		cfg.addTableClass(TableWithIndexes2.class);
	}
}
