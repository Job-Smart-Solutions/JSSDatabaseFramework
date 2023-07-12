package jss.database.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import jss.database.DatabaseException;
import jss.database.FieldLister.DbFieldInfo;
import jss.database.FieldLister.DbFieldOrViewInfo;

class SQLiteDatabase extends AbstractDatabase {
	private final static Map<SqlType, String> types = new HashMap<>();

	static {
		// number and boolean types
		types.put(SqlType.BOOLEAN, "INTEGER");
		types.put(SqlType.TINYINT, "INTEGER");
		types.put(SqlType.SMALLINT, "INTEGER");
		types.put(SqlType.MEDIUMINT, "INTEGER");
		types.put(SqlType.INTEGER, "INTEGER");
		types.put(SqlType.BIGINT, "INTEGER");
		types.put(SqlType.FLOAT, "REAL");
		types.put(SqlType.DOUBLE, "REAL");

		// decimal
		types.put(SqlType.DECIMAL, "TEXT");// store decimal as text

		// string
		types.put(SqlType.CHAR, "TEXT");
		types.put(SqlType.VARCHAR, "TEXT");
		types.put(SqlType.TEXT, "TEXT");
		types.put(SqlType.LONGTEXT, "TEXT");
		types.put(SqlType.CLOB, "TEXT");

		// date and time
		types.put(SqlType.DATETIME, "INTEGER");
		types.put(SqlType.DATE, "INTEGER");
		types.put(SqlType.TIME, "INTEGER");
		types.put(SqlType.TIMESTAMP, "INTEGER");

		// binary
		types.put(SqlType.BLOB, "BLOB");
		types.put(SqlType.LONGBLOB, "BLOB");

		// json
		types.put(SqlType.JSON, "TEXT");
	}

	SQLiteDatabase() {

	}

	@Override
	public String getDriver() {
		return "org.sqlite.JDBC";
	}

	@Override
	public String getConnectionString() {
		return "jdbc:sqlite:{SERVER}";
	}

	@Override
	public String getConnectionStringWithDatabase() {
		return "jdbc:sqlite:{DBNAME}";// the same with server
	}

	@Override
	public String getAutoincrement() {
		return "AUTOINCREMENT";
	}

	@Override
	public String getLimit() {
		return "LIMIT {offset},{rowcount}";
	}

	@Override
	public String getCreateView() {
		return "CREATE VIEW IF NOT EXISTS {viewname} AS";
	}

	@Override
	public String getTypeString(DbFieldInfo info) {
		return types.get(info.getSqlType());
	}

	@Override
	public Object convertDbToObj(Object o, DbFieldOrViewInfo info) throws DatabaseException {
		if (o == null) {
			return null;
		}

		Class<?> nativeType = info.getNativeType();

		// data i czas
		if (nativeType.isAssignableFrom(java.util.Date.class)) {
			Number num = (Number) o;
			return new java.util.Date(num.longValue());
		} else if (nativeType.isAssignableFrom(java.sql.Date.class)) {
			Number num = (Number) o;
			return new java.sql.Date(num.longValue());
		} else if (nativeType.isAssignableFrom(java.sql.Time.class)) {
			Number num = (Number) o;
			return new java.sql.Time(num.longValue());
		} else if (nativeType.isAssignableFrom(java.sql.Timestamp.class)) {
			Number num = (Number) o;
			return new java.sql.Timestamp(num.longValue());
		}
		// BigInteger, BigDecimal
		else if (nativeType.isAssignableFrom(BigInteger.class)) {
			if (o instanceof Number) {
				return new BigInteger(Long.toString(((Number) o).longValue()));
			}
			return new BigInteger((String) o);
		} else if (nativeType.isAssignableFrom(BigDecimal.class)) {
			if (o instanceof Number) {
				return new BigDecimal(o.toString());
			}
			return new BigDecimal((String) o);
		}

		return super.convertDbToObj(o, info);// default otherwise
	}
}
