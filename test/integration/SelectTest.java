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
import lukas.database.Database;
import lukas.database.DatabaseConfig;
import lukas.database.DatabaseException;
import lukas.database.SelectQuery;

@Ignore
public class SelectTest {

	public SelectTest() throws DatabaseException {
		long start, stop;

		System.out.println();

		System.out.println("SELECT: SQLite");
		start = System.currentTimeMillis();
		testSqlite();
		stop = System.currentTimeMillis();
		System.out.println("SQLite END: " + (stop - start));

		System.out.println("SELECT: MySQL");
		start = System.currentTimeMillis();
		testMysql();
		stop = System.currentTimeMillis();
		System.out.println("MySQL END: " + (stop - start));

		System.out.println("SELECT: PostgreSQL");
		start = System.currentTimeMillis();
		testPostgresql();
		stop = System.currentTimeMillis();
		System.out.println("PostgreSQL END: " + (stop - start));

		System.out.println();
	}

	private void testSqlite() throws DatabaseException {
		DatabaseConfig cfg = Main.createSqlite();
		Main.addTablesToConfig(cfg);
		Database db = new Database(cfg);
		db.connect();
		
		testAllSelect(db);
		
		db.close();
	}

	private void testMysql() throws DatabaseException {
		DatabaseConfig cfg = Main.createMysql();
		Main.addTablesToConfig(cfg);
		Database db = new Database(cfg);
		db.connect();
		
		testAllSelect(db);
		
		db.close();
	}

	private void testPostgresql() throws DatabaseException {
		DatabaseConfig cfg = Main.createPostgresql();
		Main.addTablesToConfig(cfg);
		Database db = new Database(cfg);
		db.connect();
		
		testAllSelect(db);
		
		db.close();
	}

	private void testAllSelect(Database db) throws DatabaseException {
		selectNumberTable(db);
		selectNumberObjTable(db);
		selectTextTable(db);
		selectDateTimeTable(db);
		selectDecimalTable(db);
		selectBinJsonTbl(db);
		selectTblsWithIndexes(db);
	}

	private void selectNumberTable(Database db) throws DatabaseException {
		SelectQuery<NumberTable> query = db.createSelectQuery(NumberTable.class);
		query.execute();
	}

	private void selectNumberObjTable(Database db) throws DatabaseException {
		SelectQuery<NumberObjTable> query = db.createSelectQuery(NumberObjTable.class);
		query.execute();
	}

	private void selectTextTable(Database db) throws DatabaseException {
		SelectQuery<TextTable> query = db.createSelectQuery(TextTable.class);
		query.execute();
	}

	private void selectDateTimeTable(Database db) throws DatabaseException {
		SelectQuery<DateTimeTable> query = db.createSelectQuery(DateTimeTable.class);
		query.execute();
	}

	private void selectDecimalTable(Database db) throws DatabaseException {
		SelectQuery<DecimalTable> query = db.createSelectQuery(DecimalTable.class);
		query.execute();
	}

	private void selectBinJsonTbl(Database db) throws DatabaseException {
		SelectQuery<BinaryAndJsonTable> query = db.createSelectQuery(BinaryAndJsonTable.class);
		query.execute();
	}

	private void selectTblsWithIndexes(Database db) throws DatabaseException {
		SelectQuery<TableWithIndexes> query1 = db.createSelectQuery(TableWithIndexes.class);
		query1.execute();
		
		SelectQuery<TableWithIndexes2> query2 = db.createSelectQuery(TableWithIndexes2.class);
		query2.execute();
	}
	
}
