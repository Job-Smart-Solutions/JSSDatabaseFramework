package jss.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.Collection;

import jss.database.annotations.DbView;

/**
 * Manage views
 * 
 * @author lukas
 */
public class ViewManager {
	private final Database db;

	ViewManager(Database db) {
		this.db = db;
	}

	/**
	 * Create views
	 * 
	 * @param viewClasses view classes
	 * @throws DatabaseException no annotation in one of classes, or database error
	 */
	public void createViews(Collection<Class<?>> viewClasses) throws DatabaseException {
		// sprawdz, czy są adnotacje w każdej z klas
		for (Class<?> viewClass : viewClasses) {
			checkAnnotationInView(viewClass);
		}

		// jeżeli są adnotacje, utwórz widoki
		for (Class<?> viewClass : viewClasses) {
			createView(viewClass);
		}
	}

	/**
	 * Create view
	 * 
	 * @param viewClass view class
	 * @throws DatabaseException
	 */
	public void createView(Class<?> viewClass) throws DatabaseException {
		checkAnnotationInView(viewClass);

		DbView dbView = viewClass.getAnnotation(DbView.class);
		if (dbView.simulate()) {// do not create view when is simulation
			return;
		}

		String query = buildQueryForCreateView(viewClass);
		db.logQuery(query);

		// pobierz istniejące połączenie lub utwórz nowe
		Connection connection = db.getConnection();

		try (Statement stmt = connection.createStatement()) {// TODO refactor
			stmt.setQueryTimeout(db.getConfig().queryTimeout);// TODO refactor
			stmt.execute(query);// wykonaj zapytanie
		} catch (SQLTimeoutException e) {
			throw new DatabaseException("SQL create view timeout!", e);
		} catch (SQLException e) {// w przypadku błędu SQL zwróc błąd
			throw new DatabaseException("SQL error when creating view for class: " + viewClass.getName(), e);
		}
	}

	/**
	 * Get builded query for create view
	 * 
	 * @param viewClass view class
	 * @return builded query
	 * @throws DatabaseException
	 */
	public String getQueryForCreateView(Class<?> viewClass) throws DatabaseException {
		checkAnnotationInView(viewClass);
		return buildQueryForCreateView(viewClass);
	}

	/**
	 * Build query for create view
	 * 
	 * @param viewClass view class
	 * @throws DatabaseException
	 */
	private String buildQueryForCreateView(Class<?> viewClass) throws DatabaseException {
		char escape = db.getDb().getEscapeChar();

		DbView dbView = viewClass.getAnnotation(DbView.class);

		FieldLister lister = db.getFieldLister(viewClass);
		lister.listFields();
		lister.loadDatabaseTypes(db.getConfig().sqlTypeMapper);
		String viewName = lister.getTableName();

		// build own view query
		ViewQueryCreator vQuery = new ViewQueryCreator(db, dbView);
		vQuery.setOwnQuery(dbView.query());
		for (Class<?> clazz : dbView.preparingClases()) {
			vQuery.addPreparingClass(clazz);
		}
		vQuery.execute();// build query

		// query for create view
		StringBuilder sb = new StringBuilder();

		if (!dbView.simulate()) {// when not simulate - create view
			sb.append(db.getDb().getCreateView().replace("{viewname}", escape + viewName + escape)).append(' ');
		}
		sb.append(vQuery.getBuildedQuery());

		// builded query
		return sb.toString();
	}

	/**
	 * Get builded query for delete view
	 * 
	 * @param viewClass view class
	 * @return builded query
	 * @throws DatabaseException
	 */
	public String getQueryForDeleteView(Class<?> viewClass) throws DatabaseException {
		checkAnnotationInView(viewClass);
		return buildQueryForDeleteView(viewClass);
	}

	/**
	 * Delete view
	 * 
	 * @param viewClass view class
	 * @throws DatabaseException
	 */
	public void deleteView(Class<?> viewClass) throws DatabaseException {
		checkAnnotationInView(viewClass);

		DbView dbView = viewClass.getAnnotation(DbView.class);
		if (dbView.simulate()) {// do not delete view when is simulation
			return;
		}

		String query = buildQueryForDeleteView(viewClass);
		db.logQuery(query);

		// pobierz istniejące połączenie lub utwórz nowe
		Connection connection = db.getConnection();

		try (Statement stmt = connection.createStatement()) {// TODO refactor
			stmt.setQueryTimeout(db.getConfig().queryTimeout);// TODO refactor
			stmt.execute(query);// wykonaj zapytanie
		} catch (SQLTimeoutException e) {
			throw new DatabaseException("SQL drop view timeout!");
		} catch (SQLException e) {// w przypadku błędu SQL zwróc błąd
			throw new DatabaseException("SQL error when deleting view for class: " + viewClass.getName(), e);
		}
	}

	/**
	 * Build query for delete view
	 * 
	 * @param viewClass view class
	 * @return query
	 * @throws DatabaseException
	 */
	private String buildQueryForDeleteView(Class<?> viewClass) throws DatabaseException {
		checkAnnotationInView(viewClass);

		char escape = db.getDb().getEscapeChar();

		// pobierz nazwę widoku
		FieldLister lister = db.getFieldLister(viewClass);
		String viewName = lister.getTableName();

		// zapytanie
		return db.getDb().getDropView().replace("{viewname}", escape + viewName + escape);
	}

	/**
	 * Check, annotation {@link DbView} exists in class
	 * 
	 * @param viewClass class to check
	 * @throws DatabaseException annotation not exists
	 */
	static void checkAnnotationInView(Class<?> viewClass) throws DatabaseException {
		if (!viewClass.isAnnotationPresent(DbView.class)) {// sprawdź, czy jest adnotacja
			throw new DatabaseException("No 'DbView' annotation in class " + viewClass.getName());
		}
	}

}
