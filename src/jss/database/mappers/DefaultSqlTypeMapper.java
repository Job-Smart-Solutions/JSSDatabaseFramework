package jss.database.mappers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import jss.database.types.SqlType;

/**
 * Domyślne mapowanie typów Java na typy SQL
 * 
 * @author lukas
 */
public class DefaultSqlTypeMapper implements SqlTypeMapper {
	private static final Map<Class<?>, SqlType> mapTypes = new HashMap<>();
	
	static {
		//boolean and numbers
		mapTypes.put(boolean.class, SqlType.BOOLEAN);
		mapTypes.put(Boolean.class, SqlType.BOOLEAN);
		
		mapTypes.put(byte.class, SqlType.TINYINT);
		mapTypes.put(Byte.class, SqlType.TINYINT);
		mapTypes.put(short.class, SqlType.SMALLINT);
		mapTypes.put(Short.class, SqlType.SMALLINT);
		mapTypes.put(int.class, SqlType.INTEGER);
		mapTypes.put(Integer.class, SqlType.INTEGER);
		mapTypes.put(long.class, SqlType.BIGINT);
		mapTypes.put(Long.class, SqlType.BIGINT);
		
		mapTypes.put(float.class, SqlType.FLOAT);
		mapTypes.put(Float.class, SqlType.FLOAT);
		mapTypes.put(double.class, SqlType.DOUBLE);
		mapTypes.put(Double.class, SqlType.DOUBLE);
		
		//decimal numbers
		mapTypes.put(BigInteger.class, SqlType.DECIMAL);
		mapTypes.put(BigDecimal.class, SqlType.DECIMAL);
		
		//string types
		mapTypes.put(char.class, SqlType.CHAR);
		mapTypes.put(Character.class, SqlType.CHAR);
		
		mapTypes.put(String.class, SqlType.VARCHAR);
		mapTypes.put(char[].class, SqlType.VARCHAR);
		
		//binary type
		mapTypes.put(byte[].class, SqlType.BLOB);

		//date and time
		mapTypes.put(java.util.Date.class, SqlType.DATETIME);
		mapTypes.put(java.sql.Date.class, SqlType.DATE);
		mapTypes.put(java.sql.Time.class, SqlType.TIME);
		mapTypes.put(java.sql.Timestamp.class, SqlType.TIMESTAMP);
	}
	
	public DefaultSqlTypeMapper() {

	}
	
	@Override
	public SqlType getSqlType(Class<?> nativeType) {
		SqlType type = mapTypes.get(nativeType);
		
		if(type == null) {
			return SqlType.UNKNOWN;
		}
		
		return type;
	}

}
