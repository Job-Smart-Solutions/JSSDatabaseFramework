package integration.tables;

import org.junit.Ignore;

import lukas.database.annotations.DbField;
import lukas.database.annotations.DbPrimaryKey;
import lukas.database.annotations.DbTable;
import lukas.database.types.SqlType;

@Ignore
@DbTable("text_table")
public class TextTable {

	@DbPrimaryKey
	@DbField
	private long id;//test long primary key

	// one char
	@DbField
	public char oneChar1;

	@DbField
	public Character oneChar2;

	@DbField(canNull = true)
	public Character oneChar3;

	// strings
	@DbField
	public String strNormal;

	@DbField(stringLen = 100)
	public String strWithCustomLen;

	@DbField(canNull = true)
	public String strNullable;

	@DbField(canNull = true, stringLen = 100)
	public String strNullableWithCustomLen;

	// string as text
	@DbField(type = SqlType.TEXT)
	public String strText;

	@DbField(type = SqlType.LONGTEXT)
	public String strTextLong;

	@DbField(type = SqlType.TEXT, canNull = true)
	public String strTextNullable;

	@DbField(type = SqlType.LONGTEXT, canNull = true)
	public String strTextLongNullable;

	// string as char type
	@DbField(type = SqlType.CHAR)
	public String strChar;

	@DbField(type = SqlType.CHAR, stringLen = 15)
	public String strCharCustomLen;

	@DbField(type = SqlType.CHAR, canNull = true)
	public String strCharNullable;

	@DbField(type = SqlType.CHAR, stringLen = 7, canNull = true)
	public String strCharNullableCustomLen;

	// char arrays
	@DbField
	public char[] chArr;

	@DbField(stringLen = 15)
	public char[] chArrCustomLen;

	@DbField(canNull = true)
	public char[] chArrNullable;

	@DbField(stringLen = 15, canNull = true)
	public char[] chArrNullableCustomLen;

	@DbField(type = SqlType.CHAR)
	public char[] chFixedArr;

	@DbField(type = SqlType.CHAR, stringLen = 15)
	public char[] chFixedArrCustomLen;

	@DbField(type = SqlType.CHAR, canNull = true)
	public char[] chFixedArrNullable;

	@DbField(type = SqlType.CHAR, stringLen = 15, canNull = true)
	public char[] chFixedArrNullableCustomLen;

	public long getId() {
		return id;
	}
}
