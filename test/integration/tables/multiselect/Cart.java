package integration.tables.multiselect;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.Ignore;

import lukas.database.annotations.DbField;
import lukas.database.annotations.DbForeignKey;
import lukas.database.annotations.DbManyToOne;
import lukas.database.annotations.DbPrimaryKey;
import lukas.database.annotations.DbTable;

/**
 * Koszyk
 * @author lukas
 */
@Ignore
@DbTable("carts")
public class Cart {

	@DbPrimaryKey
	@DbField
	private long id;
	
	@DbField(canNull = true)
	@DbForeignKey(refObject = User.class, refField = "id", thisField = "user")
	private Integer userId;//user (dla zalogowanych)
	private User user;
	
	@DbField(canNull = true)
	private String sessionId;//sesja (dla niezalogowanych)
	
	@DbField
	private Date createdAt;
	
	@DbField
	private Date lastModAt;

	//lista towarów w koszyku
	@DbManyToOne(refObject = CartItem.class, refField = "cartId")
	private List<CartItem> items;
	
	public Cart() {
		long current = new Date().getTime();
		createdAt = new Date(current);
		lastModAt = new Date(current);
		
		items = new ArrayList<>();
	}
	
	/**
	 * ID
	 */
	public long getId() {
		return id;
	}

	/**
	 * ID usera (lub null, gdy niezalogowany)
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 * User (lub null, gdy niezalogowany)
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Ustaw usera (lub null, gdy niezalogowany)
	 */
	public void setUser(User user) {
		this.user = user;
		if(user == null) {
			this.userId = null;
		} else {
			this.userId = user.getId();
		}
	}

	/**
	 * ID sesji (lub null, gdy zalogowany)
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Ustaw ID sesji (lub null, gdy zalogowany)
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Data utworzenia koszyka
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * Data ostatniej modyfikacji
	 */
	public Date getLastModAt() {
		return lastModAt;
	}

	/**
	 * Ustaw datę ostatniej modyfikacji
	 */
	public void setLastModAt(Date lastModAt) {
		this.lastModAt = lastModAt;
	}
	
	/**
	 * Towary w koszyku
	 */
	public List<CartItem> getItems() {
		return items;
	}
	
	/**
	 * Ilość RÓŻNYCH przedmiotów w koszyku
	 */
	public int getItemsSize() {
		return items.size();
	}
	
	/**
	 * Ilość WSZYSTKICH przedmiotów w koszyku
	 */
	public int getItemsQty() {
		int ret = 0;
		
		for(CartItem it : items) {
			ret += it.getQuantity();
		}
		
		return ret;
	}
	
	
	/**
	 * Zwraca przedmiot z koszyka po jego ID
	 * @param itemid ID przedmiotu
	 * @return
	 */
	public Optional<CartItem> getItemById(int itemid) {
		return items.parallelStream().filter(it -> it.getItemId() == itemid).findAny();
	}

	@Override
	public String toString() {
		return "Cart [id=" + id + ", sessionId=" + sessionId + ", createdAt="
				+ createdAt + ", lastModAt=" + lastModAt
				+ "\n, userId=" + userId + ", user=" + user 
				+ "\n, items("+items.size()+")=" + items + "]";
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
		Cart other = (Cart) obj;
		return id == other.id;
	}
	
	
}
