package integration.tables.multiselect;

import java.util.Objects;

import org.junit.Ignore;

import jss.database.annotations.DbField;
import jss.database.annotations.DbForeignKey;
import jss.database.annotations.DbPrimaryKey;
import jss.database.annotations.DbTable;

@Ignore
@DbTable("carts_items")
public class CartItem {

	@DbPrimaryKey(autoIncrement = false)
	@DbField
	@DbForeignKey(refObject = Cart.class, refField = "id", thisField = "cart")
	private long cartId;
	private Cart cart;

	@DbPrimaryKey(autoIncrement = false)
	@DbField
	@DbForeignKey(refObject = Item.class, refField = "id", thisField = "item")
	private long itemId;
	private Item item;

	@DbField
	private short quantity;

	public CartItem() {

	}

	public CartItem(Cart cart, Item item) {
		setCart(cart);
		setItem(item);
	}

	/**
	 * ID koszyka
	 */
	public long getCartId() {
		return cartId;
	}

	/**
	 * Koszyk
	 */
	public Cart getCart() {
		return cart;
	}

	public void setCart(Cart cart) {
		this.cart = cart;
		this.cartId = cart.getId();
	}

	/**
	 * ID towaru
	 */
	public long getItemId() {
		return itemId;
	}

	/**
	 * Towar
	 */
	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
		this.itemId = item.getId();
	}

	/**
	 * Ilość
	 */
	public short getQuantity() {
		return quantity;
	}

	/**
	 * Ustaw ilość
	 */
	public void setQuantity(short quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "CartItem [cartId=" + cartId + ", itemId=" + itemId + ", item=" + item + ", quantity=" + quantity + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(cartId, itemId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CartItem other = (CartItem) obj;
		return cartId == other.cartId && itemId == other.itemId;
	}

	
}
