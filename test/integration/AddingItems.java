package integration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;

import integration.tables.BinaryAndJsonTable;
import integration.tables.DateTimeTable;
import integration.tables.DecimalTable;
import integration.tables.NumberObjTable;
import integration.tables.NumberTable;
import integration.tables.TableWithIndexes;
import integration.tables.TableWithIndexes2;
import integration.tables.TextTable;
import lukas.database.DataSaver;
import lukas.database.Database;
import lukas.database.DatabaseConfig;
import lukas.database.DatabaseException;

@Ignore
public class AddingItems {

	public AddingItems() throws DatabaseException {
		long start, stop;

		System.out.println();

		System.out.println("ADDING ITEMS: SQLite");
		start = System.currentTimeMillis();
		testSqlite();
		stop = System.currentTimeMillis();
		System.out.println("SQLite END: " + (stop - start));

		System.out.println("ADDING ITEMS: MySQL");
		start = System.currentTimeMillis();
		testMysql();
		stop = System.currentTimeMillis();
		System.out.println("MySQL END: " + (stop - start));

		System.out.println("ADDING ITEMS: PostgreSQL");
		start = System.currentTimeMillis();
		testPostgresql();
		stop = System.currentTimeMillis();
		System.out.println("PostgreSQL END: " + (stop - start));

		System.out.println();
	}

	void testSqlite() throws DatabaseException {
		DatabaseConfig cfg = Main.createSqlite();
		Main.addTablesToConfig(cfg);
		Database db = new Database(cfg);
		db.connect();

		testAllInsert(db);

		db.close();
	}

	void testMysql() throws DatabaseException {
		DatabaseConfig cfg = Main.createMysql();
		Main.addTablesToConfig(cfg);
		Database db = new Database(cfg);
		db.connect();

		testAllInsert(db);

		db.close();
	}

	void testPostgresql() throws DatabaseException {
		DatabaseConfig cfg = Main.createPostgresql();
		Main.addTablesToConfig(cfg);
		Database db = new Database(cfg);
		db.connect();

		testAllInsert(db);

		db.close();
	}

	private void testAllInsert(Database db) throws DatabaseException {
		DataSaver ds = db.getDataSaver();

		createNumberTable(ds);
		createNumberObjTable(ds);
		createTextTable(ds);
		createDateTimeTable(ds);
		createDecimalTable(ds);
		createBinJsonTbl(ds);
		createTblsWithIndexes(ds);
	}

	private void createNumberTable(DataSaver ds) throws DatabaseException {
		NumberTable n1 = new NumberTable();
		n1.bigIntType = 203443L;
		n1.boolType = true;
		n1.byteType = -99;
		n1.doubleType = -98765.4321d;
		n1.floatType = 789.123f;
		n1.intType = -4567890;
		n1.mediumIntType = 123456;
		n1.shortType = 32000;

		ds.insert(n1);
	}

	private void createNumberObjTable(DataSaver ds) throws DatabaseException {
		NumberObjTable n = new NumberObjTable();
		n.bool1 = false;
		n.bool2 = true;
		n.byte1 = -34;
		n.byte2 = 120;
		n.double1 = 234.567d;
		n.double2 = -983.123d;
		n.float1 = -32.456f;
		n.float2 = 64.567f;
		n.int1 = -1234567;
		n.int2 = 23456789;
		n.long1 = 12345667890L;
		n.long2 = -1234567890L;
		n.medium1 = 123456;
		n.medium2 = -123456;
		n.short1 = 32000;
		n.short2 = -29876;

		ds.insert(n);
		ds.update(n);

		// wstawianie NULL tam gdzie można
		NumberObjTable n2 = new NumberObjTable();
		n2.bool1 = true;
		n2.byte1 = (byte) 255; // should -1
		n2.double1 = 234.567d;
		n2.float1 = -32.456f;
		n2.int1 = -1234567;
		n2.long1 = 12345667890L;
		n2.medium1 = 123456;
		n2.short1 = 32000;

		ds.insert(n2);
	}

	private void createTextTable(DataSaver ds) throws DatabaseException {
		TextTable t = new TextTable();
		t.oneChar1 = 'x';
		t.oneChar2 = 'y';
		t.oneChar3 = 'z';

		t.strNormal = "hello";
		t.strWithCustomLen = "customLen";
		t.strNullable = "µΩŁĄ";
		t.strNullableWithCustomLen = "zażółć gęślą jaźń";

		t.strText = "a text type \n abcde";
		t.strTextLong = "long text type \n test";
		t.strTextNullable = "nullable txt type";
		t.strTextLongNullable = "long nullable text \n ZAŻÓŁĆ GĘŚLĄ JAŹŃ";

		t.strChar = "string as CHAR fixed len";
		t.strCharCustomLen = "1234567890";
		t.strCharNullable = "nullable chars";
		t.strCharNullableCustomLen = "7chars!";

		t.chArr = new char[] { 'a', 'c', 'd' };
		t.chArrNullable = "nullableChars".toCharArray();
		t.chArrCustomLen = "max 15 chars!µΩ".toCharArray();
		t.chArrNullableCustomLen = new char[] { 'µ', 'Ω', 'Ł', 'Œ', '¼', '≈' };

		t.chFixedArr = new char[] { 'f', 'i', 'x', 'e', 'd' };
		t.chFixedArrNullable = "nullable fixed array".toCharArray();
		t.chFixedArrCustomLen = new char[] { 'm', 'a', 'x', ' ', '1', '5', ' ', 'c', 'h', 'a', 'r', 's', 'µ', 'Ω',
				'Ł' };
		t.chFixedArrNullableCustomLen = new char[] { 'µ', '”', '„', 'ð', 'æ', 'ŋ', '’', '¬', '¡', '¿', '£', '¼', '‰',
				'¾', '·' };

		// table with nullable values (WITH EMOJI TEST)
		TextTable t2 = new TextTable();
		t2.oneChar1 = 'x';
		t2.oneChar2 = 'y';

		t2.strNormal = "hello 🙋🙋🙋";
		t2.strWithCustomLen = "customLen";

		t2.strText = "a text type \n abcde 🙋🙋🙋";
		t2.strTextLong = "long text type \n test";

		t2.strChar = "string as CHAR fixed len 🙋🙋🙋";
		t2.strCharCustomLen = "1234567890";

		t2.chArr = new char[] { 'a', 'c', 'd', '‰' };
		t2.chArrCustomLen = "max 15 chars!µΩ".toCharArray();

		t2.chFixedArr = new char[] { 'f', 'i', 'x', 'e', 'd' };
		t2.chFixedArrCustomLen = new char[] { 'm', 'a', 'x', ' ', '1', '5', ' ', 'c', 'h', 'a', 'r', 's', 'µ', 'Ω',
				'Ł' };

		// test insert in set
		Set<TextTable> set = new LinkedHashSet<>();
		set.add(t);
		set.add(t2);

		ds.insert(set);
	}

	private void createDateTimeTable(DataSaver ds) throws DatabaseException {
		Date now = new Date();// util date
		Date yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000);

		DateTimeTable dt = new DateTimeTable();

		dt.utilDate = now;
		dt.utilDateNullable = yesterday;
		dt.sqlDate = new java.sql.Date(now.getTime());
		dt.sqlDateNullable = new java.sql.Date(yesterday.getTime());
		dt.sqlTime = new java.sql.Time(now.getTime());
		dt.sqlTimeNullable = new java.sql.Time(yesterday.getTime());
		dt.sqlTimestamp = new java.sql.Timestamp(now.getTime());
		dt.sqlTimestampNullable = new java.sql.Timestamp(yesterday.getTime());

		ds.insert(dt);

		// test next with nullable
		Date next2h = new Date(now.getTime() + 2 * 60 * 60 * 1000);

		DateTimeTable dt2 = new DateTimeTable();

		dt2.utilDate = next2h;
		dt2.sqlDate = new java.sql.Date(yesterday.getTime());
		dt2.sqlTime = new java.sql.Time(next2h.getTime());
		dt2.sqlTimestamp = new java.sql.Timestamp(now.getTime());

		ds.insert(dt2);
	}

	private void createDecimalTable(DataSaver ds) throws DatabaseException {
		DecimalTable d1 = new DecimalTable();
		d1.bi = new BigInteger("123456789019");
		d1.biNullable = new BigInteger("9876543210");
		d1.biPrec = new BigInteger("12345");// 5 precision
		d1.biPrecNullable = new BigInteger("12341234");// 8 precision

		d1.bd1 = new BigDecimal("1234.567");
		d1.bdNullable = new BigDecimal("123456.912");
		d1.bdPrec = new BigDecimal("123.45");
		d1.bdPrecNullable = new BigDecimal("123456.78");
		d1.bdScale = new BigDecimal("12345.67891");
		d1.bdScaleNullable = new BigDecimal("1.123456789");
		d1.bdPrecScale = new BigDecimal("12345.6789");
		d1.bdPrecScaleNullable = BigDecimal.ONE;

		DecimalTable d2 = new DecimalTable();
		d2.bi = new BigInteger("123456789012345");
		d2.biPrec = new BigInteger("12345");// 5 precision

		d2.bd1 = new BigDecimal("1234.567");
		d2.bdPrec = new BigDecimal("123.45");
		d2.bdScale = new BigDecimal("12345.99876");
		d2.bdPrecScale = new BigDecimal("12345.67899");

		List<DecimalTable> lst = new LinkedList<>();
		lst.add(d1);
		lst.add(d2);
		ds.insert(lst);
	}

	private void createBinJsonTbl(DataSaver ds) throws DatabaseException {
		BinaryAndJsonTable bj = new BinaryAndJsonTable();
		bj.binData = new byte[] { 1, 2, 3 };
		bj.binDataNullable = new byte[] { (byte) 0xFF, (byte) 0xEE, (byte) 0xAA };
		bj.binLongData = new byte[] { 99, 98, 97, 0x7F };
		bj.binLongDataNullable = new byte[] { -1, -4, -33, -128 };

		bj.jsonField = "['a', 'r', 'r', 'a', 1234, 89.1, true, false, {'a': 51}, {}]".replace('\'', '"');
		bj.jsonFieldNullable = "{'a': 'test', 'b': true, 'c': 123.948, 'd': {}, 'e': {'x': 99.1, 'e': false} }"
				.replace('\'', '"');

		ds.insert(bj);

		// nullable test
		BinaryAndJsonTable bj2 = new BinaryAndJsonTable();
		bj2.binData = new byte[] { 1, 2, 3 };
		bj2.binLongData = new byte[] { 99, 98, 97, 0x7F };
		bj2.jsonField = "{}";// empty object

		ds.insert(bj2);
	}

	private void createTblsWithIndexes(DataSaver ds) throws DatabaseException {
		TableWithIndexes t11 = new TableWithIndexes();
		t11.ind1 = 123;
		t11.ind2 = 987;
		t11.uniqueField = 9988776655L;
		ds.insert(t11);

		// ind2 null
		TableWithIndexes t12 = new TableWithIndexes();
		t12.ind1 = -9876;
		t12.uniqueField = 9988776650L;
		ds.insert(t12);

		// next table
		TableWithIndexes2 t21 = new TableWithIndexes2();
		t21.testval1 = "ABEC";
		t21.indStr1 = "xd";
		t21.indStr2 = "test";
		t21.testval2 = 'd';
		t21.indLong = -99999L;
		ds.insert(t21);

		// nullable and default values
		TableWithIndexes2 t22 = new TableWithIndexes2();
		t22.testval1 = "ABEC";
		t22.indStr2 = "test";
		t22.testval2 = 'Ω';
		ds.insert(t22);
	}
}
