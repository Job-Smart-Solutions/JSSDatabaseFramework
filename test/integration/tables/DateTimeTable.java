package integration.tables;

import org.junit.Ignore;

import lukas.database.annotations.DbField;
import lukas.database.annotations.DbPrimaryKey;
import lukas.database.annotations.DbTable;

@Ignore
@DbTable("date_time_table")
public class DateTimeTable {

	@DbPrimaryKey
	@DbField
	public short id;// test short id

	@DbField
	public java.util.Date utilDate;

	@DbField(canNull = true)
	public java.util.Date utilDateNullable;

	@DbField
	public java.sql.Date sqlDate;

	@DbField(canNull = true)
	public java.sql.Date sqlDateNullable;

	@DbField
	public java.sql.Time sqlTime;

	@DbField(canNull = true)
	public java.sql.Time sqlTimeNullable;

	@DbField
	public java.sql.Timestamp sqlTimestamp;

	@DbField(canNull = true)
	public java.sql.Timestamp sqlTimestampNullable;

	public short getId() {
		return id;
	}
}
