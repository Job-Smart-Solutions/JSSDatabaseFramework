package jss.database.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Properties;

import jss.database.FieldLister.DbFieldInfo;
import jss.database.FieldLister.DbFieldOrViewInfo;

/**
 * Abstrakcyjny typ bazy danych
 * 
 * @author lukas
 */
public abstract class AbstractDatabase {

	public AbstractDatabase() {

	}

	/**
	 * @return sterownik bazy danych
	 */
	public abstract String getDriver();

	/**
	 * @return string połączenia do serwera
	 */
	public abstract String getConnectionString();

	/**
	 * @return string połączenia do serwera i wybranej bazy danych
	 */
	public abstract String getConnectionStringWithDatabase();

	/**
	 * Set properties for connection
	 * 
	 * @param prop properties
	 */
	public void setPropertiesForConnection(Properties prop) {

	}

	/**
	 * @return create database query string
	 */
	public String getCreateDatabase() {
		return "CREATE DATABASE {DBNAME}";
	}

	/**
	 * @return create table query string
	 */
	public String getCreateTable() {
		return "CREATE TABLE IF NOT EXISTS {tablename}";
	}

	/**
	 * @return append string after create table query
	 */
	public String getCreateTableAppend() {
		return "";
	}

	/**
	 * @return drop table query string
	 */
	public String getDropTable() {
		return "DROP TABLE IF EXISTS {tablename}";
	}

	/**
	 * @return create view query string
	 */
	public String getCreateView() {
		return "CREATE OR REPLACE VIEW {viewname} AS";
	}

	/**
	 * @return drop view query string
	 */
	public String getDropView() {
		return "DROP VIEW IF EXISTS {viewname}";
	}

	/**
	 * @return escape char for column and table names
	 */
	public char getEscapeChar() {
		return '`';
	}

	/**
	 * @return create index string
	 */
	public String getCreateIndex() {
		return "CREATE INDEX IF NOT EXISTS {indexname} ON {tablename} ({tablecolumn})";
	}

	/**
	 * @return autoincrement string
	 */
	public abstract String getAutoincrement();

	/**
	 * @return limit for query
	 */
	public abstract String getLimit();

	/**
	 * Get database type as string, for create table
	 * 
	 * @param info database field info
	 * @return string, or null if cannot be determined
	 */
	public abstract String getTypeString(DbFieldInfo info);

	/**
	 * Convert database loaded object to java object (for select)
	 * 
	 * @param o    object from database (nullable)
	 * @param info database field info
	 * @return java object (nullable)
	 */
	public Object convertDbToObj(Object o, DbFieldOrViewInfo info) {
		if (o == null) {
			return null;
		}

		Class<?> nativeType = info.getNativeType();
		if (nativeType.isAssignableFrom(boolean.class) || nativeType.isAssignableFrom(Boolean.class)) {
			if (o instanceof Boolean) {
				return ((Boolean) o).booleanValue();
			} else {
				Number num = (Number) o;
				boolean b = (num.intValue() == 0) ? false : true;
				return b;
			}
		} else if (nativeType.isAssignableFrom(byte.class) || nativeType.isAssignableFrom(Byte.class)) {
			Number num = (Number) o;
			return num.byteValue();
		} else if (nativeType.isAssignableFrom(short.class) || nativeType.isAssignableFrom(Short.class)) {
			Number num = (Number) o;
			return num.shortValue();
		} else if (nativeType.isAssignableFrom(int.class) || nativeType.isAssignableFrom(Integer.class)) {
			Number num = (Number) o;
			return num.intValue();
		} else if (nativeType.isAssignableFrom(long.class) || nativeType.isAssignableFrom(Long.class)) {
			Number num = (Number) o;
			return num.longValue();
		} else if (nativeType.isAssignableFrom(float.class) || nativeType.isAssignableFrom(Float.class)) {
			Number num = (Number) o;
			return num.floatValue();
		} else if (nativeType.isAssignableFrom(double.class) || nativeType.isAssignableFrom(Double.class)) {
			Number num = (Number) o;
			return num.doubleValue();
		} else if (nativeType.isAssignableFrom(char.class) || nativeType.isAssignableFrom(Character.class)) {
			String s = (String) o;
			return s.charAt(0);
		} else if (nativeType.isAssignableFrom(BigInteger.class)) {
			BigDecimal dec = (BigDecimal) o;// readed as bigdecimal
			return dec.toBigInteger();// convert to biginteger
		} else if (nativeType.isAssignableFrom(char[].class)) {// CLOB (char[] readed as String)
			String s = (String) o;
			return s.toCharArray();
		}

		return o;
	}

	/**
	 * Convert java object to database object (for saving)
	 * 
	 * @param o    java object (nullable)
	 * @param info database field info
	 * @return database object to save (nullable)
	 */
	public Object convertObjToDb(Object o, DbFieldInfo info) {
		if (o == null) {
			return null;
		}

		Class<?> nativeType = info.getNativeType();
		// one character to string
		if (nativeType.isAssignableFrom(char.class) || nativeType.isAssignableFrom(Character.class)) {
			char[] chr = new char[1];
			chr[0] = (char) o;
			return new String(chr);
		}

		// CLOB (char[] to String)
		if (nativeType.isAssignableFrom(char[].class)) {
			return new String((char[]) o);
		}

		return o;
	}

}
