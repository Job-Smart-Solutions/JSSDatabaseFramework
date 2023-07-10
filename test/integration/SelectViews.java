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
import jss.database.ViewQuery;

/**
 * Test SELECT VIEW
 * 
 * @author lukas
 */
@Ignore
public class SelectViews {

	public SelectViews() throws DatabaseException {
		long start, stop;

		System.out.println();

		System.out.println("SQLITE START - select views");
		start = System.currentTimeMillis();
		testSQLite();
		stop = System.currentTimeMillis();
		System.out.println("SQLITE END: " + (stop - start));

		System.out.println("MYSQL START - select views");
		start = System.currentTimeMillis();
		testMySQL();
		stop = System.currentTimeMillis();
		System.out.println("MYSQL END: " + (stop - start));

		System.out.println("PostgreSQL START - select views");
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
		CreateViews.addViewsToConfig(cfg);

		Database db = new Database(cfg);
		db.connect();

		testItemsView(db);
		testItemsView2(db);
		testMultiSelectView(db);
		testMultiSelectView2(db);
		testMultiSelectView3(db);
		testMultiSelectView4(db);

		db.close();
	}

	private void testItemsView(Database db) throws DatabaseException {
		ViewQuery<ItemsView> query = db.createViewQuery(ItemsView.class);
		query.execute();
		for (ItemsView e : query.getEntities()) {
			System.out.println(e);
		}
	}

	private void testItemsView2(Database db) throws DatabaseException {
		ViewQuery<ItemsView2> query = db.createViewQuery(ItemsView2.class);
		query.execute();
		for (ItemsView2 e : query.getEntities()) {
			System.out.println(e);
		}
	}

	private void testMultiSelectView(Database db) throws DatabaseException {
		ViewQuery<MultiSelectView> query = db.createViewQuery(MultiSelectView.class);
		query.execute();
		for (MultiSelectView e : query.getEntities()) {
			System.out.println(e);
		}
	}

	private void testMultiSelectView2(Database db) throws DatabaseException {
		ViewQuery<MultiSelectView2> query = db.createViewQuery(MultiSelectView2.class);
		query.execute();
		for (MultiSelectView2 e : query.getEntities()) {
			System.out.println(e);
		}
	}

	private void testMultiSelectView3(Database db) throws DatabaseException {
		ViewQuery<MultiSelectView3> query = db.createViewQuery(MultiSelectView3.class);
		query.setOwnQuery(" WHERE {FIELD:Item:countWare} > 10");
		query.execute();
		for (MultiSelectView3 e : query.getEntities()) {
			System.out.println(e);
		}
	}

	private void testMultiSelectView4(Database db) throws DatabaseException {
		ViewQuery<MultiSelectView4> query = db.createViewQuery(MultiSelectView4.class);
		query.setOwnQuery(" ORDER BY {FIELD:Item:id} ASC");
		query.execute();
		for (MultiSelectView4 e : query.getEntities()) {
			System.out.println(e);
		}
	}

}
