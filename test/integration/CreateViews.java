package integration;

import org.junit.Ignore;

import integration.views.ItemsView;
import integration.views.ItemsView2;
import integration.views.MultiSelectView;
import integration.views.MultiSelectView2;
import integration.views.MultiSelectView3;
import integration.views.MultiSelectView4;
import jss.database.Database;
import jss.database.DatabaseConfig;
import jss.database.DatabaseException;
import jss.database.ViewManager;

/**
 * Test creating views
 * 
 * @author lukas
 */
@Ignore
public class CreateViews {
	private final static boolean DELETE_OLD_TEST = false;// delete old test data?

	public CreateViews() throws DatabaseException {
		long start, stop;

		System.out.println();

		System.out.println("SQLITE START - create views");
		start = System.currentTimeMillis();
		testSQLite();
		stop = System.currentTimeMillis();
		System.out.println("SQLITE END: " + (stop - start));

		System.out.println("MYSQL START - create views");
		start = System.currentTimeMillis();
		testMySQL();
		stop = System.currentTimeMillis();
		System.out.println("MYSQL END: " + (stop - start));

		System.out.println("PostgreSQL START - create views");
		start = System.currentTimeMillis();
		testPostgreSQL();
		stop = System.currentTimeMillis();
		System.out.println("PostgreSQL END: " + (stop - start));

		System.out.println();
	}

	private void testSQLite() throws DatabaseException {
		DatabaseConfig cfg = Main.createSqlite();
		doTest(cfg);
	}

	private void testMySQL() throws DatabaseException {
		DatabaseConfig cfg = Main.createMysql();
		doTest(cfg);
	}

	private void testPostgreSQL() throws DatabaseException {
		DatabaseConfig cfg = Main.createPostgresql();
		doTest(cfg);
	}

	private void doTest(DatabaseConfig cfg) throws DatabaseException {
		addViewsToConfig(cfg);
		Database db = new Database(cfg);
		db.connect();

		if (DELETE_OLD_TEST) {
			dropViews(db.getViewManager());
		}

		db.createViewsIfNotExists();

		db.close();
	}

	static void addViewsToConfig(DatabaseConfig cfg) throws DatabaseException {
		cfg.addViewClass(ItemsView.class);
		cfg.addViewClass(ItemsView2.class);
		cfg.addViewClass(MultiSelectView.class);
		cfg.addViewClass(MultiSelectView2.class);
		cfg.addViewClass(MultiSelectView3.class);
		cfg.addViewClass(MultiSelectView4.class);
	}

	private static void dropViews(ViewManager manager) throws DatabaseException {
		manager.deleteView(MultiSelectView4.class);
		manager.deleteView(MultiSelectView3.class);
		manager.deleteView(MultiSelectView2.class);
		manager.deleteView(MultiSelectView.class);
		manager.deleteView(ItemsView2.class);
		manager.deleteView(ItemsView.class);
	}

	/**
	 * Posprzątaj - by zacząć testy od nowa
	 * 
	 * @throws DatabaseException
	 */
	static void cleanup() throws DatabaseException {
		DatabaseConfig cfgSqlite = Main.createSqlite();
		DatabaseConfig cfgMysql = Main.createMysql();
		DatabaseConfig cfgPgsql = Main.createPostgresql();

		System.out.println("CLEANUP VIEWS BEFORE TESTS:");

		Database db1 = new Database(cfgSqlite);
		db1.connect();
		dropViews(db1.getViewManager());
		db1.close();

		Database db2 = new Database(cfgMysql);
		db2.connect();
		dropViews(db2.getViewManager());
		db2.close();

		Database db3 = new Database(cfgPgsql);
		db3.connect();
		dropViews(db3.getViewManager());
		db3.close();
	}

}
