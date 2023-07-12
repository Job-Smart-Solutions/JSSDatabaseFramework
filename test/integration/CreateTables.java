package integration;

import org.junit.Ignore;

import jss.database.Database;
import jss.database.DatabaseConfig;
import jss.database.DatabaseException;

/**
 * Test tworzenia tabel
 * 
 * @author lukas
 */
@Ignore
public class CreateTables {
	private final static boolean DELETE_OLD_TEST = true;// delete old test data?

	public CreateTables() throws DatabaseException {
		long start, stop;

		System.out.println();

		System.out.println("SQLITE START - create tables");
		start = System.currentTimeMillis();
		testSQLite();
		stop = System.currentTimeMillis();
		System.out.println("SQLITE END: " + (stop - start));

		System.out.println("MYSQL START - create tables");
		start = System.currentTimeMillis();
		testMySQL();
		stop = System.currentTimeMillis();
		System.out.println("MYSQL END: " + (stop - start));

		System.out.println("PostgreSQL START - create tables");
		start = System.currentTimeMillis();
		testPostgreSQL();
		stop = System.currentTimeMillis();
		System.out.println("PostgreSQL END: " + (stop - start));

		System.out.println();
	}

	/**
	 * Test SQLITE
	 * 
	 * @throws DatabaseException
	 */
	private void testSQLite() throws DatabaseException {
		DatabaseConfig cfg = Main.createSqlite();
		if(cfg != null) {
			doTest(cfg);
		}
	}

	/**
	 * Test MYSQL
	 * 
	 * @throws DatabaseException
	 */
	private void testMySQL() throws DatabaseException {
		DatabaseConfig cfg = Main.createMysql();
		if(cfg != null) {
			doTest(cfg);
		}
	}

	/**
	 * Test PGSQL
	 * 
	 * @throws DatabaseException
	 */
	private void testPostgreSQL() throws DatabaseException {
		DatabaseConfig cfg = Main.createPostgresql();
		if(cfg != null) {
			doTest(cfg);
		}
	}

	private void doTest(DatabaseConfig cfg) throws DatabaseException {
		Main.addTablesToConfig(cfg);
		Database db = new Database(cfg);
		db.connect();

		if (DELETE_OLD_TEST) {
			Main.dropTables(db.getTableManager());
		}

		db.createTablesIfNotExists();

		db.close();
	}

}
