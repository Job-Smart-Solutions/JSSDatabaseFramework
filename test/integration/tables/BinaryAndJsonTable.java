package integration.tables;

import org.junit.Ignore;

import jss.database.annotations.DbField;
import jss.database.annotations.DbPrimaryKey;
import jss.database.annotations.DbTable;
import jss.database.types.SqlType;

@Ignore
@DbTable("binary_and_json_table")
public class BinaryAndJsonTable {

	@DbPrimaryKey
	@DbField(type = SqlType.MEDIUMINT)
	private int id;// test medium int primary key

	@DbField
	public byte[] binData;

	@DbField(canNull = true)
	public byte[] binDataNullable;

	@DbField(type = SqlType.LONGBLOB)
	public byte[] binLongData;

	@DbField(type = SqlType.LONGBLOB, canNull = true)
	public byte[] binLongDataNullable;

	@DbField(type = SqlType.JSON)
	public String jsonField;

	@DbField(type = SqlType.JSON, canNull = true)
	public String jsonFieldNullable;
	
	//clob
	@DbField(type = SqlType.CLOB)
	public String clob1;
	
	@DbField(type = SqlType.CLOB)
	public char[] clob2;
	
	@DbField(type = SqlType.CLOB, canNull = true)
	public String clob1nl;
	
	@DbField(type = SqlType.CLOB, canNull = true)
	public char[] clob2nl;
	
}
