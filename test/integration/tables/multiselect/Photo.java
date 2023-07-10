package integration.tables.multiselect;
import java.util.Objects;

import org.junit.Ignore;

import jss.database.annotations.DbField;
import jss.database.annotations.DbPrimaryKey;
import jss.database.annotations.DbTable;

@Ignore
@DbTable("photos")
public class Photo {
	
	@DbPrimaryKey
	@DbField
	private int id;
	
	@DbField
	private String data;
	
	public Photo() {
		data = "";
	}
	
	public int getId() {
		return id;
	}
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Photo [id=" + id + ", data=" + data + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Photo other = (Photo) obj;
		return id == other.id;
	}
	
	
}
