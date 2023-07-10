package integration.views;

import java.math.BigDecimal;
import java.util.Objects;

import org.junit.Ignore;

import integration.tables.multiselect.Item;
import jss.database.annotations.DbView;
import jss.database.annotations.DbViewField;
import jss.database.annotations.DbViewObject;

/**
 * Widok towarów z dodatkowym polem
 * 
 * @author lukas
 */
@Ignore
@DbView(name = "items_view2", query = "SELECT {LISTFIELDS:Item}, {FIELD:Item:id} as itemKeyId, "
		+ "({FIELD:Item:priceNet}*(({FIELD:Item:taxPercent}/100)+1)) as priceGross, "
		+ "({FIELD:Item:priceNet}*{FIELD:Item:taxPercent}/100) as item_vat "
		+ "FROM {TABLE:Item}", preparingClases = Item.class)
public class ItemsView2 {

	@DbViewObject(Item.class)
	private Item item;

	@DbViewField
	private long itemKeyId;

	@DbViewField
	private BigDecimal priceGross;

	@DbViewField("item_vat")
	private BigDecimal vatValue;

	public Item getItem() {
		return item;
	}

	public BigDecimal getPriceGross() {
		return priceGross;
	}

	public BigDecimal getVatValue() {
		return vatValue;
	}

	@Override
	public String toString() {
		return "ItemsView2 [item=" + item + ", itemKeyId=" + itemKeyId + ", priceGross=" + priceGross + ", vatValue="
				+ vatValue + "]";
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
		ItemsView2 other = (ItemsView2) obj;
		return itemKeyId == other.itemKeyId;
	}

}
