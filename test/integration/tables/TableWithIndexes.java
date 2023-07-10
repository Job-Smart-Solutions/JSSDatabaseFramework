package integration.tables;

import org.junit.Ignore;

import lukas.database.annotations.DbField;
import lukas.database.annotations.DbPrimaryKey;
import lukas.database.annotations.DbTable;

@Ignore
@DbTable("table_with_indexes")
public class TableWithIndexes {

	@DbPrimaryKey
	@DbField
	private int id;
	
	@DbField(isIndex = true)
	public int ind1;
	
	@DbField(isIndex = true, canNull = true)
	public Short ind2;
	
	@DbField(isUnique = true)
	public long uniqueField;
	
	public int getId() {
		return id;
	}
}
