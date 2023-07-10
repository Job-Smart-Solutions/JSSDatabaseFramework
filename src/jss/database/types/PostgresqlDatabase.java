package jss.database.types;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.postgresql.util.PGobject;

import jss.database.FieldLister.DbFieldInfo;
import jss.database.FieldLister.DbFieldOrViewInfo;

class PostgresqlDatabase extends AbstractDatabase {
	private final static Map<SqlType, String> types = new HashMap<>();
	private final static Map<SqlType, String> serialTypes = new HashMap<>();// types for serial fields

	static {
		// number and boolean types
		types.put(SqlType.BOOLEAN, "BOOLEAN");
		types.put(SqlType.TINYINT, "SMALLINT");// no type tinyint
		types.put(SqlType.SMALLINT, "SMALLINT");
		types.put(SqlType.MEDIUMINT, "INTEGER");// no type mediumint
		types.put(SqlType.INTEGER, "INTEGER");
		types.put(SqlType.BIGINT, "BIGINT");
		types.put(SqlType.FLOAT, "REAL");
		types.put(SqlType.DOUBLE, "DOUBLE PRECISION");

		// decimal
		types.put(SqlType.DECIMAL, "DECIMAL({precision},{scale})");

		// string
		types.put(SqlType.CHAR, "CHAR({strlen})");
		types.put(SqlType.VARCHAR, "VARCHAR({strlen})");
		types.put(SqlType.TEXT, "TEXT");
		types.put(SqlType.LONGTEXT, "TEXT");
		types.put(SqlType.CLOB, "TEXT");

		// date and time
		types.put(SqlType.DATETIME, "TIMESTAMP");
		types.put(SqlType.DATE, "DATE");
		types.put(SqlType.TIME, "TIME");
		types.put(SqlType.TIMESTAMP, "TIMESTAMP");

		// binary
		types.put(SqlType.BLOB, "BYTEA");
		types.put(SqlType.LONGBLOB, "BYTEA");

		// json
		types.put(SqlType.JSON, "JSON");

		// SERIAL FIELDS
		serialTypes.put(SqlType.TINYINT, "SMALLSERIAL");
		serialTypes.put(SqlType.SMALLINT, "SMALLSERIAL");
		serialTypes.put(SqlType.MEDIUMINT, "SERIAL");
		serialTypes.put(SqlType.INTEGER, "SERIAL");
		serialTypes.put(SqlType.BIGINT, "BIGSERIAL");
	}

	PostgresqlDatabase() {

	}

	@Override
	public String getDriver() {
		return "org.postgresql.Driver";
	}

	@Override
	public String getConnectionString() {
		return "jdbc:postgresql://{SERVER}:{PORT}/";
	}

	@Override
	public String getConnectionStringWithDatabase() {
		return "jdbc:postgresql://{SERVER}:{PORT}/{DBNAME}";
	}

	@Override
	public String getCreateDatabase() {
		return "CREATE DATABASE {DBNAME} ENCODING = 'UTF8' LC_COLLATE = 'pl_PL.UTF-8' LC_CTYPE = 'pl_PL.UTF-8'";
	}

	@Override
	public String getAutoincrement() {
		return "";// empty string - postrgreSQL has SQL type for autoincrement
	}

	@Override
	public String getLimit() {
		return "LIMIT {rowcount} OFFSET {offset}";
	}

	@Override
	public char getEscapeChar() {
		return '"';
	}

	@Override
	public String getTypeString(DbFieldInfo info) {
		// check is serial type (autoincrement)
		if (info.getPrimaryKeyInfo() != null && info.getPrimaryKeyInfo().getPkAnnotation().autoIncrement()) {
			return serialTypes.get(info.getSqlType());
		}

		// normal type
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
	public Object convertObjToDb(Object o, DbFieldInfo info) {
		if (o == null) {
			return null;
		}

		// util date must be sent as sql timestamp
		if (info.getNativeType().isAssignableFrom(java.util.Date.class)) {
			java.util.Date date = (java.util.Date) o;
			return new java.sql.Timestamp(date.getTime());
		}

		// json value
		if (info.getFieldAnnotation().type() == SqlType.JSON) {
			try {
				PGobject obj = new PGobject();
				obj.setType("json");
				obj.setValue((String) o);
				return obj;
			} catch (SQLException e) {
				// do nothing - setValue not thrown any exception
				e.printStackTrace();
			}
		}

		return super.convertObjToDb(o, info);// default otherwise
	}

	@Override
	public Object convertDbToObj(Object o, DbFieldOrViewInfo info) {
		if (o == null) {
			return null;
		}

		// JSON field
		if (info instanceof DbFieldInfo) {
			DbFieldInfo df = (DbFieldInfo) info;
			if (df.getFieldAnnotation().type() == SqlType.JSON) {
				if (o instanceof PGobject) {
					return ((PGobject) o).getValue();
				}
			}
		}

		return super.convertDbToObj(o, info);
	}
}
