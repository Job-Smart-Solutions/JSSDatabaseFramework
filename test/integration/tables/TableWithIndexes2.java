package integration.tables;

import org.junit.Ignore;

import jss.database.annotations.DbField;
import jss.database.annotations.DbPrimaryKey;
import jss.database.annotations.DbTable;

@Ignore
@DbTable("table_with_indexes_2")
public class TableWithIndexes2 {

	@DbField
	@DbPrimaryKey
	private short id;
	
	@DbField
	public String testval1;
	
	@DbField(canNull = true, isIndex = true)
	public String indStr1;
	
	@DbField(isIndex = true)
	public String indStr2;
	
	@DbField
	public char testval2;
	
	@DbField(isIndex = true)
	public long indLong;
}
