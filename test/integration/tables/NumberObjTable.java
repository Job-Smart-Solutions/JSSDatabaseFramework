package integration.tables;

import org.junit.Ignore;

import jss.database.annotations.DbField;
import jss.database.annotations.DbPrimaryKey;
import jss.database.annotations.DbTable;
import jss.database.types.SqlType;

@Ignore
@DbTable("number_objs_table")
public class NumberObjTable {

	@DbPrimaryKey
	@DbField
	private byte id;//test byte primary key

	// number non null
	@DbField
	public Boolean bool1;

	@DbField
	public Byte byte1;

	@DbField
	public Short short1;

	@DbField(type = SqlType.MEDIUMINT)
	public Integer medium1;

	@DbField
	public Integer int1;

	@DbField
	public Long long1;

	@DbField
	public Float float1;

	@DbField
	public Double double1;

	// number null
	@DbField(canNull = true)
	public Boolean bool2;

	@DbField(canNull = true)
	public Byte byte2;

	@DbField(canNull = true)
	public Short short2;

	@DbField(canNull = true, type = SqlType.MEDIUMINT)
	public Integer medium2;

	@DbField(canNull = true)
	public Integer int2;

	@DbField(canNull = true)
	public Long long2;

	@DbField(canNull = true)
	public Float float2;

	@DbField(canNull = true)
	public Double double2;

	public byte getId() {
		return id;
	}
}
