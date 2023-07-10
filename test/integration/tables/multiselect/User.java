package integration.tables.multiselect;

import java.util.Objects;

import org.junit.Ignore;

import jss.database.annotations.DbField;
import jss.database.annotations.DbOneToOne;
import jss.database.annotations.DbPrimaryKey;
import jss.database.annotations.DbTable;

/**
 * Użytkownik
 * 
 * @author lukas
 */
@Ignore
@DbTable("users")
public class User {

	@DbPrimaryKey
	@DbField
	private int id;

	@DbField
	private String login;

	@DbField
	private String password;

	@DbField
	private String email;

	@DbField
	private boolean isAdmin;

	@DbField
	private boolean isDeleted;

	// dodatkowe info o userze
	@DbOneToOne(refObject = UserAddInfo.class, refField = "userId")
	private UserAddInfo addInfo;

	public User() {

	}

	public int getId() {
		return id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public UserAddInfo getAddInfo() {
		return addInfo;
	}

	public UserAddInfo getOrCreateAddInfo() {
		if (addInfo == null) {
			addInfo = new UserAddInfo(this);
		}
		return addInfo;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", login=" + login + ", password=" + password + ", email=" + email + ", isAdmin="
				+ isAdmin + ", isDeleted=" + isDeleted + ", addInfo=" + addInfo + "]";
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
		User other = (User) obj;
		return id == other.id;
	}

}
