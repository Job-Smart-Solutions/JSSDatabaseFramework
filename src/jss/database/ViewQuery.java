package jss.database;

import java.util.List;

import jss.database.FieldLister.DbFieldInfo;
import jss.database.FieldLister.DbFieldOrViewInfo;
import jss.database.FieldLister.DbManyToOneInfo;
import jss.database.FieldLister.DbOneToOneInfo;
import jss.database.FieldLister.DbViewObjectInfo;
import jss.database.annotations.DbView;

/**
 * View query
 * 
 * @param <T> view class
 * 
 * @author lukas
 */
public class ViewQuery<T> extends MultiSelectQuery<T> {

	private final FieldLister viewLister;
	private final DbView dbView;
	private String injectable = "";// to inject in query

	/**
	 * @param db   połączenie z bazą danych
	 * @param from klasa główna widoku
	 * @throws DatabaseException
	 */
	ViewQuery(Database db, Class<T> from) throws DatabaseException {
		super(db, from);

		viewLister = db.getFieldLister(from);
		dbView = viewLister.getDbView();

		// preparing classes
		for (Class<?> c : dbView.preparingClases()) {
			addPreparingClass(c);
		}
		for (Class<?> c : dbView.skipClases()) {
			addSkipClass(c);
		}

		setOwnQuery("");// default query
	}

	/**
	 * Set to inject in query
	 * 
	 * @param injectable
	 */
	public void setInjectable(String injectable) {
		this.injectable = injectable;
	}

	/**
	 * This method only appends query!
	 */
	@Override
	public void setOwnQuery(String query) throws DatabaseException {// only append query
		String startQuery;

		if (dbView.simulate()) {// only simulate query
			ViewQueryCreator creator = new ViewQueryCreator(db, dbView);
			creator.setOwnQuery(dbView.query());
			for (Class<?> clazz : dbView.preparingClases()) {
				creator.addPreparingClass(clazz);
			}
			creator.execute();// build start query
			startQuery = creator.getBuildedQuery();
		} else {// normal select from view
			char escape = db.getDb().getEscapeChar();
			String viewName = escape + viewLister.getDbView().name() + escape;
			startQuery = "SELECT * FROM " + viewName;
		}

		// inject into query
		startQuery = startQuery.replace("{{INJECT}}", injectable);

		super.setOwnQuery(startQuery + ' ' + query);
	}

	@Override
	protected void loadForeign(List<DbFieldInfo> foreignKeys, List<DbManyToOneInfo> manyToOneList,
			List<DbOneToOneInfo> oneToOneList, Object to, Object obj) throws DatabaseException {

		DbViewObjectInfo dvo = null;
		if (obj != null) {// gdy istnieje obiekt przetwarzanej klasy
			dvo = getDbViewObjectByClass(obj.getClass());// sprawdź, czy jest on DbViewObject
		}

		// gdy obiekt DbViewObject istnieje i jest to klasa widoku
		if (to != null && to.getClass() == from && dvo != null) {// load object to view
			if (dvo.fieldInClass.getType().isAssignableFrom(obj.getClass())) {
				try {
					dvo.fieldInClass.set(to, obj);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new DatabaseException("Cannot set object into view in " + from.getSimpleName());
				}
			}
		} else {
			super.loadForeign(foreignKeys, manyToOneList, oneToOneList, to, obj);
		}
	}

	@Override
	protected String getFieldInQueryName(DbFieldOrViewInfo field, FieldLister lister) {
		if (dbView.simulate()) {// simulate view only (normal query)
			return super.getFieldInQueryName(field, lister);
		}

		// not simulate - query is from VIEW
		char escape = db.getDb().getEscapeChar();// escape char
		return escape + field.getInQueryName() + escape;
	}

	/**
	 * Pobiera adnotację DbViewObject po przetwarzanej klasie
	 * 
	 * @param clazz przetwarzana klasa
	 * @return adnotacja DbViewObject lub null gdy nie znaleziono
	 */
	private DbViewObjectInfo getDbViewObjectByClass(Class<?> clazz) {
		List<DbViewObjectInfo> dvoList = viewLister.getDbViewObjects();

		for (DbViewObjectInfo dvo : dvoList) {
			// System.out.println("F: " + dvo.fieldInClass);
			// System.out.println("VAL: " + dvo.fieldAnnotation);
			if (dvo.fieldAnnotation.value() == clazz) {
				return dvo;
			}
		}

		return null;
	}

}
