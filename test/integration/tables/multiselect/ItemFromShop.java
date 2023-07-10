package integration.tables.multiselect;

import java.util.Date;
import java.util.Objects;

import org.junit.Ignore;

import jss.database.annotations.DbField;
import jss.database.annotations.DbForeignKey;
import jss.database.annotations.DbPrimaryKey;
import jss.database.annotations.DbTable;

/**
 * Towar ze sklepu - test klasy dodatkowej (drugiej)
 * 
 * @author lukas
 */
@Ignore
@DbTable("item_from_shop")
public class ItemFromShop {

	@DbPrimaryKey(autoIncrement = false)
	@DbField
	@DbForeignKey(refObject = Item.class, refField = "id", thisField = "item")
	private long itemId;
	private Item item;

	@DbField
	public String name2;

	@DbField
	public Date data;

	public void setItem(Item item) {
		this.item = item;
		this.itemId = item.getId();
	}

	public Item getItem() {
		return item;
	}

	public long getItemId() {
		return itemId;
	}

	@Override
	public String toString() {
		return "ItemFromShop [itemId=" + itemId + ", name2=" + name2 + ", data=" + data + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemFromShop other = (ItemFromShop) obj;
		return itemId == other.itemId;
	}

}
