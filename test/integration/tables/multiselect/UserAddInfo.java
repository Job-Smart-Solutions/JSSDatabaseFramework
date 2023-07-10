package integration.tables.multiselect;

import java.util.Objects;

import org.junit.Ignore;

import jss.database.annotations.DbField;
import jss.database.annotations.DbForeignKey;
import jss.database.annotations.DbPrimaryKey;
import jss.database.annotations.DbTable;

/**
 * Dodatkowe info o userze - do testów one to one
 * 
 * @author lukas
 */
@Ignore
@DbTable("users_add_info")
public class UserAddInfo {

	@DbPrimaryKey(autoIncrement = false)
	@DbForeignKey(refObject = User.class, refField = "id", thisField = "user")
	@DbField
	private int userId;
	private User user;

	@DbField
	private String nameSurname;

	@DbField
	private String city;

	UserAddInfo() {
		nameSurname = "";
		city = "";
	}

	public UserAddInfo(User u) {
		this();
		setUser(u);
	}

	public int getUserId() {
		return userId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
		this.userId = user.getId();
	}

	public String getNameSurname() {
		return nameSurname;
	}

	public void setNameSurname(String nameSurname) {
		this.nameSurname = nameSurname;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public String toString() {
		return "UserAddInfo [userId=" + userId + ", nameSurname=" + nameSurname + ", city=" + city + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserAddInfo other = (UserAddInfo) obj;
		return userId == other.userId;
	}

}
