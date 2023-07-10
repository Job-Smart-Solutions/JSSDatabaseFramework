package integration.tables.multiselect;
import java.util.Objects;

import org.junit.Ignore;

import jss.database.annotations.DbField;
import jss.database.annotations.DbPrimaryKey;
import jss.database.annotations.DbTable;

@Ignore
@DbTable("categories")
public class Category {

	@DbField
	@DbPrimaryKey
	public int id;
	
	@DbField
	public String name;
	
	@DbField
	public String opis;
	
	@DbField
	public short addNum;

	@Override
	public String toString() {
		return "Category [id=" + id + ", name=" + name + ", opis=" + opis + ", addNum=" + addNum + "]";
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
		Category other = (Category) obj;
		return id == other.id;
	}
	
	
}
