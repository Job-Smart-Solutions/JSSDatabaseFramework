package jss.database;

import java.util.List;

import jss.database.annotations.DbView;

/**
 * View query prepare for creating view in ViewManager
 * 
 * @author lukas
 */
class ViewQueryCreator extends AbstractOwnQuery {
	private final DbView dbView;

	ViewQueryCreator(Database db, DbView dbView) {
		super(db);
		this.dbView = dbView;
	}

	@Override
	protected void executeQuery() throws DatabaseException {
		// do nothing
	}

	@Override
	protected void buildQueryOwnParams(List<java.lang.String[]> params) throws DatabaseException {
		// do nothing, only check has no parameters here (only when not simulate)
		if (!dbView.simulate()) {
			for (String[] param : params) {
				if (param[0].equals("PARAM")) {
					throw new DatabaseException("SQL Query for VIEW cannot has parameters!");
				}
			}
		}
	}

	/**
	 * Get builded query
	 */
	String getBuildedQuery() {
		return buildedQuery;
	}

}
