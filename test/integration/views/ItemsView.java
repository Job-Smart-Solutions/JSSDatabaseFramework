package integration.views;

import java.util.Objects;

import org.junit.Ignore;

import integration.tables.multiselect.Item;
import lukas.database.annotations.DbView;
import lukas.database.annotations.DbViewField;
import lukas.database.annotations.DbViewObject;

/**
 * Prosty widok towarów
 * 
 * @author lukas
 */
@Ignore
@DbView(name = "items_view", query = "SELECT {LISTFIELDS:Item}, {FIELD:Item:id} as itemKeyId "
		+ "FROM {TABLE:Item}", preparingClases = Item.class)
public class ItemsView {

	@DbViewObject(Item.class)
	private Item item;

	@DbViewField
	private long itemKeyId;

	public Item getItem() {
		return item;
	}

	@Override
	public String toString() {
		return "ItemsView [item=" + item + ", itemKeyId=" + itemKeyId + "]";
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
		ItemsView other = (ItemsView) obj;
		return itemKeyId == other.itemKeyId;
	}

}
