package integration.tables;

import org.junit.Ignore;

import lukas.database.annotations.DbField;
import lukas.database.annotations.DbPrimaryKey;
import lukas.database.annotations.DbTable;
import lukas.database.types.SqlType;

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
}
