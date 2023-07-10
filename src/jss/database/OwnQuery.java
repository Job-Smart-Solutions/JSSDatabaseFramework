package jss.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

/**
 * Own query, other than select
 * 
 * @author lukas
 */
public class OwnQuery extends AbstractOwnQuery {
	private int updatedCount = -1;// -1 if not executed

	/**
	 * Konstruktor
	 * 
	 * @param db połączenie z bazą danych
	 */
	OwnQuery(Database db) {
		super(db);
	}

	@Override
	protected void executeQuery() throws DatabaseException {
		Connection conn = db.getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(buildedQuery, PreparedStatement.KEEP_CURRENT_RESULT)) {
			stmt.setQueryTimeout(db.getConfig().queryTimeout);
			DatabaseUtils.prepareQueryForObjects(stmt, objects);

			updatedCount = stmt.executeUpdate();
		} catch (SQLTimeoutException e) {// timeout
			throw new DatabaseException("OwnQuery: SQL query timeout!", e);
		} catch (SQLException e) {// w przypadku błędu SQL rzuć wyjątek
			throw new DatabaseException("OwnQuery: SQL query error!", e);
		}
	}

	/**
	 * Get updated rows count
	 * 
	 * @return updated rows count, or -1 if query not executed yet
	 */
	public int getUpdatedCount() {
		return updatedCount;
	}

}
