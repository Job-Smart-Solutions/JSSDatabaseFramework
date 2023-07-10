package integration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;

import integration.tables.multiselect.Cart;
import integration.tables.multiselect.CartItem;
import integration.tables.multiselect.Category;
import integration.tables.multiselect.Item;
import integration.tables.multiselect.ItemFromShop;
import integration.tables.multiselect.ItemPhoto;
import integration.tables.multiselect.Photo;
import integration.tables.multiselect.User;
import integration.tables.multiselect.UserAddInfo;
import jss.database.Database;
import jss.database.DatabaseConfig;
import jss.database.DatabaseException;
import jss.database.MultiSelectQuery;
import jss.database.SelectQuery;

@Ignore
public class MultiSelectTest {

	public MultiSelectTest() throws DatabaseException {
		long start, stop;

		System.out.println();

		System.out.println("Multi SELECT: SQLite");
		start = System.currentTimeMillis();
		testSqlite();
		stop = System.currentTimeMillis();
		System.out.println("SQLite END: " + (stop - start));

		System.out.println("Multi SELECT: MySQL");
		start = System.currentTimeMillis();
		testMysql();
		stop = System.currentTimeMillis();
		System.out.println("MySQL END: " + (stop - start));

		System.out.println("Multi SELECT: PostgreSQL");
		start = System.currentTimeMillis();
		testPostgresql();
		stop = System.currentTimeMillis();
		System.out.println("PostgreSQL END: " + (stop - start));

		System.out.println();
	}

	private void addTables(DatabaseConfig cfg) throws DatabaseException {
		cfg.addTableClass(Category.class);
		cfg.addTableClass(Item.class);
		cfg.addTableClass(Photo.class);
		cfg.addTableClass(ItemPhoto.class);
		cfg.addTableClass(User.class);
		cfg.addTableClass(UserAddInfo.class);
		cfg.addTableClass(Cart.class);
		cfg.addTableClass(CartItem.class);
		cfg.addTableClass(ItemFromShop.class);
	}

	private static void dropTables(Database db) throws DatabaseException {
		db.getTableManager().deleteTable(ItemFromShop.class);
		db.getTableManager().deleteTable(CartItem.class);
		db.getTableManager().deleteTable(Cart.class);
		db.getTableManager().deleteTable(UserAddInfo.class);
		db.getTableManager().deleteTable(User.class);
		db.getTableManager().deleteTable(ItemPhoto.class);
		db.getTableManager().deleteTable(Photo.class);
		db.getTableManager().deleteTable(Item.class);
		db.getTableManager().deleteTable(Category.class);

		db.createTablesIfNotExists();
	}

	void testSqlite() throws DatabaseException {
		DatabaseConfig cfg = Main.createSqlite();
		addTables(cfg);
		Database db = new Database(cfg);
		db.connect();

		doTest(db);

		db.close();
	}

	void testMysql() throws DatabaseException {
		DatabaseConfig cfg = Main.createMysql();
		addTables(cfg);
		Database db = new Database(cfg);
		db.connect();

		doTest(db);

		db.close();
	}

	void testPostgresql() throws DatabaseException {
		DatabaseConfig cfg = Main.createPostgresql();
		addTables(cfg);
		Database db = new Database(cfg);
		db.connect();

		doTest(db);

		db.close();
	}

	public static void doTest(Database db) throws DatabaseException {
		dropTables(db);

		testInsertOne(db);
		testRollback(db);
		testCartInsert(db);

		long start = System.currentTimeMillis();
		testCartSelect(db);
		testSelectMultiple(db);

		long stop = System.currentTimeMillis();
		System.out.println("Czas MultiSelect: " + (stop - start));

		// testPerformanceSelect(db);
	}

	/*private static void testPerformanceSelect(Database db) throws DatabaseException {
		long timeSelMulti = testSelectMultiplePerformance(db);
		long timeSelCart = testCartSelectPerformance(db);

		System.out.println();
		System.out.println("----------------");
		System.out.println("Select Multiple: " + timeSelMulti);
		System.out.println("Select Cart: " + timeSelCart);
	}

	static long testSelectMultiplePerformance(Database db) throws DatabaseException {
		final int TIMES = 5000;

		long start = System.currentTimeMillis();

		for (int i = 0; i < TIMES; i++) {
			testSelectMultiple(db);
		}

		long stop = System.currentTimeMillis();
		System.out.println("Czas SelectMultiple: " + (stop - start));
		return stop - start;
	}

	static long testCartSelectPerformance(Database db) throws DatabaseException {
		final int TIMES = 5000;

		long start = System.currentTimeMillis();

		for (int i = 0; i < TIMES; i++) {
			testCartSelect(db);
		}

		long stop = System.currentTimeMillis();
		System.out.println("Czas CartSelect: " + (stop - start));
		return stop - start;
	}*/

	static void testRollback(Database db) throws DatabaseException {
		Category c1 = new Category();
		c1.addNum = -99;
		c1.name = "Abec";
		c1.opis = "sasdadsadads";

		Category c2 = new Category();
		c2.addNum = 32000;
		c2.name = "Kat2";
		c2.opis = "UMC!!!";

		db.beginTransaction();

		db.getDataSaver().insert(c1);
		db.getDataSaver().insert(c2);

		// db.commit();
		db.rollback();

		Category c3 = new Category();
		c3.name = "po rollback";
		c3.opis = "opis :)";
		c3.addNum = 12345;

		db.getDataSaver().insert(c3);
	}

	static void testSelectMultiple(Database db) throws DatabaseException {
		MultiSelectQuery<Item> query = db.createMultiSelectQuery(Item.class);
		query.addPreparingClass(ItemPhoto.class);
		query.addPreparingClass(Category.class);
		query.addPreparingClass(Photo.class);

		query.setOwnQuery("SELECT {LISTFIELDS:Item}, {LISTFIELDS:Category}, {LISTFIELDS:ItemPhoto}, {LISTFIELDS:Photo} "
				+ "FROM {TABLE:Item} " + "LEFT JOIN {TABLE:Category} ON {FIELD:Item:categoryId} = {FIELD:Category:id} "
				+ "LEFT JOIN {TABLE:ItemPhoto} ON {FIELD:Item:id} = {FIELD:ItemPhoto:itemId} "
				+ "LEFT JOIN {TABLE:Photo} ON {FIELD:ItemPhoto:photoId} = {FIELD:Photo:id} ");
		// + "WHERE {FIELD:ItemPhoto:isMain} = {PARAM:ismain}");
		query.setParam("ismain", true);

		query.execute();

		List<Item> items = query.getEntities();
		System.out.println("ANS");
		for (Item i : items) {
			System.out.println();
			System.out.println(i);
			System.out.println();
		}
	}

	static void testInsertOne(Database db) throws DatabaseException {
		Category cat = new Category();
		cat.name = "kategoria druga";
		cat.opis = "opis nr 2";
		cat.addNum = 127;
		db.getDataSaver().insert(cat);

		Category cat2 = new Category();
		cat2.name = "Opony";
		cat2.opis = "Opis opon";
		cat2.addNum = 120;
		db.getDataSaver().insert(cat2);

		// items
		Item it = new Item();
		it.name = "abec1";
		it.countWare = 234;
		it.description = "µQ";
		it.priceNet = new BigDecimal("999");
		it.categoryId = cat.id;

		Item it2 = new Item();
		it2.name = "djp";
		it2.countWare = 997;
		it2.description = "998";
		it2.priceNet = BigDecimal.TEN;
		it2.categoryId = cat.id;

		Item it3 = new Item();
		it3.name = "przedmiot3";
		it3.countOffer = 99;
		it3.countWare = 127;
		it3.priceNet = new BigDecimal("123.45");
		it3.taxPercent = new BigDecimal("23");
		it3.description = "opis przedmiotu nr 3 :)";
		it3.category = cat2;

		Item it4 = new Item();// simple item, without category and photos
		it4.name = "simple item";
		it4.countWare = 5;
		it4.countOffer = 15;
		it4.priceNet = new BigDecimal("50");
		it4.taxPercent = new BigDecimal("23");
		it4.description = "test simple item witout category and photos µΩ";

		List<Item> items = new ArrayList<>();
		items.add(it);
		items.add(it3);
		items.add(it2);
		items.add(it4);
		db.getDataSaver().insert(items);

		// photos
		Photo p1 = new Photo();
		p1.setData("zdjecie 1");

		Photo p2 = new Photo();
		p2.setData("zdjecie nr 2");

		db.getDataSaver().insert(p1);
		db.getDataSaver().insert(p2);

		List<ItemPhoto> photos = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			ItemPhoto ph = new ItemPhoto();
			ph.itemId = it.id;
			ph.isMain = i == 0;
			ph.path = "path-" + i;
			ph.setPhoto(p1);

			photos.add(ph);
		}

		for (int i = 0; i < 6; i++) {
			ItemPhoto ph = new ItemPhoto();
			ph.itemId = it2.id;
			ph.isMain = i == 0;
			ph.path = "zdjnr-" + i;
			ph.setPhoto(p2);

			photos.add(ph);
		}

		for (int i = 0; i < 4; i++) {
			ItemPhoto ph = new ItemPhoto();
			ph.itemId = it3.id;
			ph.isMain = i == 0;
			ph.path = "path-3-" + i;
			if (i < 2) {
				ph.setPhoto(p1);
			} else {
				ph.setPhoto(p2);
			}

			photos.add(ph);
		}

		db.getDataSaver().insert(photos);
		
		//items for shop
		ItemFromShop ifs1 = new ItemFromShop();
		ifs1.setItem(it3);
		ifs1.data = new Date();
		ifs1.name2 = "test item from shop 1 :)";
		
		ItemFromShop ifs2 = new ItemFromShop();
		ifs2.setItem(it4);
		ifs2.data = new Date(ifs1.data.getTime() - 200000);
		ifs2.name2 = "xD µΩ";
		
		db.getDataSaver().insert(ifs1);
		db.getDataSaver().insert(ifs2);
	}

	static void testCartInsert(Database db) throws DatabaseException {
		// get items
		SelectQuery<Item> query = db.createSelectQuery(Item.class);
		query.execute();

		// user
		User u = new User();
		u.setLogin("testowy");
		u.setPassword("xxx");
		u.setEmail("mail@mail.com");
		db.getDataSaver().insert(u);

		User u2 = new User();
		u2.setLogin("adam");
		u2.setPassword("xdd");
		u2.setEmail("adam@example.com");
		u2.getOrCreateAddInfo().setNameSurname("adam xyz");
		u2.getOrCreateAddInfo().setCity("krakow");
		db.getDataSaver().insert(u2);

		u2.getAddInfo().setUser(u2);// save user ID to UserAddInfo!
		db.getDataSaver().insert(u2.getAddInfo());

		// cart
		Cart c = new Cart();
		c.setLastModAt(new Date());
		c.setUser(u);
		db.getDataSaver().insert(c);

		Cart c2 = new Cart();
		c2.setLastModAt(new Date());
		c2.setUser(u2);
		db.getDataSaver().insert(c2);

		// items in cart
		short qty = 1;
		List<CartItem> cartItems = new LinkedList<>();
		for (Item it : query.getEntities()) {
			CartItem ci = new CartItem(c, it);
			ci.setQuantity(qty);
			qty++;

			cartItems.add(ci);

			CartItem ci2 = new CartItem(c2, it);
			ci2.setQuantity(qty);
			cartItems.add(ci2);
		}

		db.getDataSaver().insert(cartItems);
	}

	static void testCartSelect(Database db) throws DatabaseException {
		String q = "SELECT {LISTFIELDS:Cart}, {LISTFIELDS:CartItem}, {LISTFIELDS:Item}, {LISTFIELDS:ItemPhoto}, {LISTFIELDS:Photo}, "
				+ "{LISTFIELDS:User}, {LISTFIELDS:UserAddInfo} " + "FROM {TABLE:Cart} "
				+ "LEFT JOIN {TABLE:CartItem} ON {FIELD:Cart:id} = {FIELD:CartItem:cartId} "
				+ "LEFT JOIN {TABLE:Item} ON {FIELD:CartItem:itemId} = {FIELD:Item:id} "
				+ "LEFT JOIN {TABLE:ItemPhoto} ON {FIELD:Item:id} = {FIELD:ItemPhoto:itemId} "
				+ "LEFT JOIN {TABLE:Photo} ON {FIELD:ItemPhoto:photoId} = {FIELD:Photo:id} "
				+ "JOIN {TABLE:User} ON {FIELD:Cart:userId} = {FIELD:User:id} "
				+ "LEFT JOIN {TABLE:UserAddInfo} ON {FIELD:UserAddInfo:userId} = {FIELD:User:id} ";
		// + "WHERE ({FIELD:ItemPhoto:isMain}=1 OR {FIELD:ItemPhoto:isMain} IS NULL) ";

		MultiSelectQuery<Cart> query = db.createMultiSelectQuery(Cart.class);
		query.addPreparingClass(CartItem.class);
		query.addPreparingClass(Item.class);
		query.addPreparingClass(ItemPhoto.class);
		query.addPreparingClass(Photo.class);
		query.addPreparingClass(User.class);
		query.addPreparingClass(UserAddInfo.class);
		query.setOwnQuery(q);

		query.execute();

		for (Cart e : query.getEntities()) {
			System.out.println();
			System.out.println(e);
		}
	}

}
