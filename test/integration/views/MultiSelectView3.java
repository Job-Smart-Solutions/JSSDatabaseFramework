package integration.views;

import java.util.Objects;

import org.junit.Ignore;

import integration.tables.multiselect.Category;
import integration.tables.multiselect.Item;
import integration.tables.multiselect.ItemPhoto;
import integration.tables.multiselect.Photo;
import jss.database.annotations.DbView;
import jss.database.annotations.DbViewField;
import jss.database.annotations.DbViewObject;

@Ignore
@DbView(name = "multi_view3", query = "SELECT {LISTFIELDS:Item}, {LISTFIELDS:Category}, {LISTFIELDS:ItemPhoto}, {LISTFIELDS:Photo}, "
		+ "{FIELD:Item:countWare}*10 as maxQty, {FIELD:Item:id} as item_key_id FROM {TABLE:Item} "
		+ "LEFT JOIN {TABLE:Category} ON {FIELD:Item:categoryId} = {FIELD:Category:id} "
		+ "LEFT JOIN {TABLE:ItemPhoto} ON {FIELD:Item:id} = {FIELD:ItemPhoto:itemId} "
		+ "LEFT JOIN {TABLE:Photo} ON {FIELD:ItemPhoto:photoId} = {FIELD:Photo:id}", preparingClases = { Item.class,
				Category.class, ItemPhoto.class, Photo.class })
public class MultiSelectView3 {

	@DbViewObject(Item.class)
	private Item item;

	@DbViewField()
	private long maxQty;

	@DbViewField("item_key_id")
	private long itemKeyId;

	public Item getItem() {
		return item;
	}

	public Long getMaxQty() {
		return maxQty;
	}

	@Override
	public String toString() {
		return "MultiSelectView3 [item=" + item + ", maxQty=" + maxQty + ", itemKeyId=" + itemKeyId + "]";
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
		MultiSelectView3 other = (MultiSelectView3) obj;
		return itemKeyId == other.itemKeyId;
	}

}
