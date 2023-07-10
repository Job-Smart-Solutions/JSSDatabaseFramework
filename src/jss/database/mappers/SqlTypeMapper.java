package jss.database.mappers;

import jss.database.types.SqlType;

/**
 * Mapowanie typów Java na SQL
 * 
 * @author lukas
 */
public interface SqlTypeMapper {

	/**
	 * Mapuje typ Java na SQL
	 * 
	 * @param nativeType typ natywny Java 
	 * @return typ SQL
	 */
	public SqlType getSqlType(Class<?> nativeType);
	
}
