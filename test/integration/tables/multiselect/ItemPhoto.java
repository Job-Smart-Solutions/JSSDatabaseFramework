package integration.tables.multiselect;
import java.util.Objects;

import org.junit.Ignore;

import jss.database.annotations.DbField;
import jss.database.annotations.DbForeignKey;
import jss.database.annotations.DbPrimaryKey;
import jss.database.annotations.DbTable;

@Ignore
@DbTable("items_photos")
public class ItemPhoto {
	@DbPrimaryKey
	@DbField
	public long id;
	
	@DbForeignKey(refObject=Item.class, refField="id", thisField="item")
	@DbField
	public long itemId;
	
	public Item item;//towar do którego przypisane jest zdjęcie
	
	@DbField
	public String path;
	
	@DbField
	public boolean isMain;
	
	@DbField
	@DbForeignKey(refObject = Photo.class, refField = "id", thisField = "photo")
	public int photoId;
	
	public Photo photo;
	
	/**
	 * Default constructor
	 */
	public ItemPhoto() {
		path = "";
	}

	public void setPhoto(Photo photo) {
		this.photo = photo;
		this.photoId = photo.getId();
	}
	
	@Override
	public String toString() {
		String itemStr = "null";
		if(item!=null) itemStr = "'"+item.id+"'";
		
		return "ItemPhoto [id=" + id + ", itemId=" + itemId + ", item=" + itemStr + ", path=" + path + ", isMain=" + isMain
				+ ", photo=" + photo + "]";
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
		ItemPhoto other = (ItemPhoto) obj;
		return id == other.id;
	}
	
	
}
