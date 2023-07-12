package integration.tables;

import org.junit.Ignore;

import jss.database.annotations.DbField;
import jss.database.annotations.DbPrimaryKey;
import jss.database.annotations.DbTable;

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

	@Override
	public String toString() {
		return "DateTimeTable [id=" + id + ", utilDate=" + utilDate + ", utilDateNullable=" + utilDateNullable
				+ ", sqlDate=" + sqlDate + ", sqlDateNullable=" + sqlDateNullable + ", sqlTime=" + sqlTime
				+ ", sqlTimeNullable=" + sqlTimeNullable + ", sqlTimestamp=" + sqlTimestamp + ", sqlTimestampNullable="
				+ sqlTimestampNullable + "]";
	}

}
