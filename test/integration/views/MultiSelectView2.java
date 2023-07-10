package integration.views;

import java.util.Objects;

import org.junit.Ignore;

import integration.tables.multiselect.Category;
import integration.tables.multiselect.Item;
import integration.tables.multiselect.ItemPhoto;
import jss.database.annotations.DbView;
import jss.database.annotations.DbViewField;
import jss.database.annotations.DbViewObject;

@Ignore
@DbView(name = "multi_view2", query = "SELECT {LISTFIELDS:Item}, {LISTFIELDS:Category}, t.photos_count, {FIELD:Item:id} as item_key_id "
		+ "FROM {TABLE:Item} "
		+ "LEFT JOIN (SELECT {FIELD:ItemPhoto:itemId} as ph_item_id, COUNT({FIELD:ItemPhoto:itemId}) AS photos_count "
		+ "FROM {TABLE:ItemPhoto} GROUP BY {FIELD:ItemPhoto:itemId}) t ON t.ph_item_id = {FIELD:Item:id} "
		+ "LEFT JOIN {TABLE:Category} ON {FIELD:Item:categoryId} = {FIELD:Category:id} ", preparingClases = {
				Item.class, Category.class, ItemPhoto.class }, skipClases = { ItemPhoto.class })
public class MultiSelectView2 {

	@DbViewObject(Item.class)
	private Item item;

	@DbViewField("photos_count")
	private Long photosCount;// can be null

	@DbViewField("item_key_id")
	private long itemKeyId;

	public Item getItem() {
		return item;
	}

	public long getPhotosCount() {
		return photosCount;
	}

	@Override
	public String toString() {
		return "MultiSelectView2 [item=" + item + ", photosCount=" + photosCount + ", itemKeyId=" + itemKeyId + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemKeyId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MultiSelectView2 other = (MultiSelectView2) obj;
		return itemKeyId == other.itemKeyId;
	}

}
