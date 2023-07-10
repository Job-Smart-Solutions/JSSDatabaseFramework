package lukas.database.types;

/**
 * SQL data type
 * 
 * @author lukas
 */
public enum SqlType {
	// number and boolean types
	BOOLEAN, // boolean
	TINYINT, // byte 1b
	SMALLINT, // short 2b
	MEDIUMINT, // int (3b)
	INTEGER, // int 4b
	BIGINT, // long 8b
	FLOAT, // float 4b
	DOUBLE, // double 8b

	// number for decimals
	DECIMAL, // BigInteger, BigDecimal

	// string types
	CHAR, // fixex-length string
	VARCHAR, // variable-length string
	TEXT, // text (long string)
	LONGTEXT, // longtext (very long string)
	CLOB, // char large object

	// date and time types
	DATETIME, // date and time
	DATE, // only date
	TIME, // only time
	TIMESTAMP, // timestamp

	// binary
	BLOB, // binary large object
	LONGBLOB, // binary large object (larger)

	// json
	JSON, // json data type (if database supports it, otherwise should use text)

	DEFAULT, // default type (for use in annotations only)
	UNKNOWN; // type cannot be determined
}
