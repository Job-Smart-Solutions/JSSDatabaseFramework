package jss.database;

import java.lang.reflect.Constructor;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jss.database.FieldLister.DbFieldOrViewInfo;
import jss.database.types.AbstractDatabase;

/**
 * @author lukas Utils bazy danych
 */
final class DatabaseUtils {
	private DatabaseUtils() {
	} // singleton

	/**
	 * Przygotuj zapytanie. Wstaw obiekty za pytajniki.
	 * 
	 * @param stmt    referencja do zapytania do przygotowania
	 * @param obiekty do wstawienia do zapytania.
	 * @throws SQLException w przypadku błędu
	 */
	static void prepareQueryForObjects(PreparedStatement stmt, List<Object> objects) throws SQLException {
		// prepare statement
		for (int i = 0; i < objects.size(); i++) {
			Object obj = objects.get(i);

			// java.util.Date as java.sql.Timestamp
			if (obj != null && obj.getClass().isAssignableFrom(java.util.Date.class)) {
				java.util.Date d = (java.util.Date) obj;
				obj = new java.sql.Timestamp(d.getTime());
			}

			stmt.setObject(i + 1, obj);
		}
	}

	/**
	 * Create instance of object
	 * 
	 * @param <T>           object class
	 * @param clazz         object class
	 * @param db            database type
	 * @param fieldsObjects map fields info and objects
	 * @return new object instance
	 * @throws DatabaseException error creating object instance
	 */
	static <T> T createInstance(Class<T> clazz, AbstractDatabase db, Map<DbFieldOrViewInfo, Object> fieldsObjects)
			throws DatabaseException {

		try {
			Constructor<T> cons = clazz.getDeclaredConstructor();
			cons.setAccessible(true);
			T obj = cons.newInstance();

			// set values into fields
			for (Entry<DbFieldOrViewInfo, Object> e : fieldsObjects.entrySet()) {
				Object toSet = db.convertDbToObj(e.getValue(), e.getKey());
				e.getKey().getFieldInClass().set(obj, toSet);
			}

			System.out.println(obj);
			return obj;
		} catch (Exception e) {
			throw new DatabaseException("Cannot create instance of class: " + clazz.getName(), e);
		}
	}

}
