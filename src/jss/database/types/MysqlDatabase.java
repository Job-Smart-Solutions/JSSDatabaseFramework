package jss.database.types;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.mariadb.jdbc.MariaDbBlob;

import jss.database.DatabaseException;
import jss.database.FieldLister.DbFieldInfo;
import jss.database.FieldLister.DbFieldOrViewInfo;

class MysqlDatabase extends AbstractDatabase {
	private static final String MYSQL_DRV = "com.mysql.jdbc.Driver";
	private static final String MARIADB_DRV = "org.mariadb.jdbc.Driver";

	private final static Map<SqlType, String> types = new HashMap<>();

	static {
		// number and boolean types
		types.put(SqlType.BOOLEAN, "TINYINT(1)");
		types.put(SqlType.TINYINT, "TINYINT");
		types.put(SqlType.SMALLINT, "SMALLINT");
		types.put(SqlType.MEDIUMINT, "MEDIUMINT");
		types.put(SqlType.INTEGER, "INTEGER");
		types.put(SqlType.BIGINT, "BIGINT");
		types.put(SqlType.FLOAT, "FLOAT");
		types.put(SqlType.DOUBLE, "DOUBLE");

		// decimal
		types.put(SqlType.DECIMAL, "DECIMAL({precision},{scale})");

		// string
		types.put(SqlType.CHAR, "CHAR({strlen})");
		types.put(SqlType.VARCHAR, "VARCHAR({strlen})");
		types.put(SqlType.TEXT, "TEXT");
		types.put(SqlType.LONGTEXT, "LONGTEXT");
		types.put(SqlType.CLOB, "LONGTEXT");

		// date and time
		types.put(SqlType.DATETIME, "DATETIME");
		types.put(SqlType.DATE, "DATE");
		types.put(SqlType.TIME, "TIME");
		types.put(SqlType.TIMESTAMP, "DATETIME");

		// binary
		types.put(SqlType.BLOB, "BLOB");
		types.put(SqlType.LONGBLOB, "LONGBLOB");

		// json
		types.put(SqlType.JSON, "JSON");
	}

	MysqlDatabase() {

	}

	@Override
	public String getDriver() {
		try {
			Class.forName(MARIADB_DRV);// check is maria db driver
			return MARIADB_DRV;
		} catch (Exception e) {// if exception, mysql driver
			return MYSQL_DRV;
		}
	}

	@Override
	public String getConnectionString() {
		// return "jdbc:mysql://{SERVER}:{PORT}/";
		return "jdbc:mariadb://{SERVER}:{PORT}/";
	}

	@Override
	public String getConnectionStringWithDatabase() {
		// return "jdbc:mysql://{SERVER}:{PORT}/{DBNAME}";
		return "jdbc:mariadb://{SERVER}:{PORT}/{DBNAME}";
	}

	@Override
	public void setPropertiesForConnection(Properties prop) {
		prop.setProperty("character_set_server", "utf8mb4");
	}

	@Override
	public String getCreateDatabase() {
		return "CREATE DATABASE {DBNAME} DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci";
	}

	@Override
	public String getAutoincrement() {
		return "AUTO_INCREMENT";
	}

	@Override
	public String getLimit() {
		return "LIMIT {offset},{rowcount}";
	}

	@Override
	public String getCreateTableAppend() {
		return "ENGINE = InnoDB CHARSET=utf8mb4 COLLATE utf8mb4_general_ci";
	}

	@Override
	public String getCreateIndex() {
		/*
		 * MySQL cannot query: create index if not exists
		 * 
		 * It throws exception when creating index, if exists!
		 */
		return "CREATE INDEX {indexname} ON {tablename} ({tablecolumn})";
	}

	@Override
	public String getTypeString(DbFieldInfo info) {
		String type = types.get(info.getSqlType());
		if (type == null) {
			return null;
		}

		if (info.nativeTypeIs(BigInteger.class)) {
			type = type.replace(",{scale}", "");// no scale for BigInteger
		}

		if (info.nativeTypeIs(char.class) || info.nativeTypeIs(Character.class)) {
			type = type.replace("{strlen}", "1");// length 1 in char type
		}

		return type;
	}

	@Override
	public Object convertObjToDb(Object o, DbFieldInfo info) throws DatabaseException {
		if (o == null) {
			return null;
		}

		// util date must be sent as sql timestamp (for save time, not only date)
		if (info.getNativeType().isAssignableFrom(java.util.Date.class)) {
			java.util.Date date = (java.util.Date) o;
			return new java.sql.Timestamp(date.getTime());
		}

		return super.convertObjToDb(o, info);
	}

	@Override
	public Object convertDbToObj(Object o, DbFieldOrViewInfo info) throws DatabaseException {
		if (o == null) {
			return null;
		}

		// JSON convert
		if (info instanceof DbFieldInfo) {
			DbFieldInfo df = (DbFieldInfo) info;
			if (df.getFieldAnnotation().type() == SqlType.JSON) {
				if (o instanceof byte[]) {// JSON as bytes array
					return new String((byte[]) o, StandardCharsets.UTF_8);
				}
			}
		}

		// BLOB
		Class<?> nativeType = info.getNativeType();
		if (nativeType.isAssignableFrom(byte[].class)) {
			if (o instanceof MariaDbBlob) {
				try {
					return ((MariaDbBlob) o).getBinaryStream().readAllBytes();
				} catch (IOException | SQLException e) {
					throw new DatabaseException("MySQL: Cannot read BLOB value", e);
				}
			}
		}

		return super.convertDbToObj(o, info);
	}
}
