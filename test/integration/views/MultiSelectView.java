package integration.views;

import java.util.Objects;

import org.junit.Ignore;

import integration.tables.multiselect.Category;
import integration.tables.multiselect.Item;
import integration.tables.multiselect.ItemPhoto;
import integration.tables.multiselect.Photo;
import lukas.database.annotations.DbView;
import lukas.database.annotations.DbViewField;
import lukas.database.annotations.DbViewObject;

@Ignore
@DbView(name = "multi_view", query = "SELECT {LISTFIELDS:Item}, {LISTFIELDS:Category}, {LISTFIELDS:ItemPhoto}, {LISTFIELDS:Photo},"
		+ "{FIELD:Item:id} as itemKeyId FROM {TABLE:Item} "
		+ "LEFT JOIN {TABLE:Category} ON {FIELD:Item:categoryId} = {FIELD:Category:id} "
		+ "LEFT JOIN {TABLE:ItemPhoto} ON {FIELD:Item:id} = {FIELD:ItemPhoto:itemId} "
		+ "LEFT JOIN {TABLE:Photo} ON {FIELD:ItemPhoto:photoId} = {FIELD:Photo:id} ", preparingClases = { Item.class,
				Category.class, ItemPhoto.class, Photo.class })
public class MultiSelectView {

	@DbViewObject(Item.class)
	private Item item;

	@DbViewField
	private long itemKeyId;

	public Item getItem() {
		return item;
	}

	@Override
	public String toString() {
		return "MultiSelectView [item=" + item + ", itemKeyId=" + itemKeyId + "]";
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
		MultiSelectView other = (MultiSelectView) obj;
		return itemKeyId == other.itemKeyId;
	}

}
