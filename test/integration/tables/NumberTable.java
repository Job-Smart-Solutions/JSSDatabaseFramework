package integration.tables;

import org.junit.Ignore;

import lukas.database.annotations.DbField;
import lukas.database.annotations.DbPrimaryKey;
import lukas.database.annotations.DbTable;
import lukas.database.types.SqlType;

@Ignore
@DbTable("number_table")
public class NumberTable {

	@DbPrimaryKey
	@DbField
	private int id;//test int primary key
	
	//number
	@DbField
	public boolean boolType;
	
	@DbField
	public byte byteType;
	
	@DbField
	public short shortType;
	
	@DbField(type = SqlType.MEDIUMINT)
	public int mediumIntType;
	
	@DbField
	public int intType;
	
	@DbField
	public long bigIntType;
	
	@DbField
	public float floatType;
	
	@DbField
	public double doubleType;
	
	public int getId() {
		return id;
	}
}
