package jss.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jss.database.FieldLister.DbFieldInfo;
import jss.database.FieldLister.DbFieldOrViewInfo;
import jss.database.OwnSelectQuery.SelectRow;

/**
 * SELECT processor
 * 
 * @author lukas
 */
class SelectProcessor {
	private final Database db;
	private final String query;
	private final List<Object> objects;

	/**
	 * @param db      database
	 * @param query   zapytanie
	 * @param objects obiekty do wstawienia za pytajniki w stmt
	 */
	SelectProcessor(Database db, String query, List<Object> objects) {
		this.db = db;
		this.query = query;
		this.objects = objects;
	}

	/**
	 * Wykonaj pojedyńcze zapytanie SELECT
	 * 
	 * @param lister obiekt zawierający informacje o polach w klasie
	 * @return lista obiektów
	 * @throws DatabaseException błąd SQL, lub błąd tworzenia obiektu
	 */
	@SuppressWarnings("unchecked")
	<T> List<T> processSingle(FieldLister lister) throws DatabaseException {
		List<T> ret = new ArrayList<T>();// zwracana lista obiektów

		Class<T> clazz = (Class<T>) lister.getTableClass();

		Connection conn = db.getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.KEEP_CURRENT_RESULT)) {
			stmt.setQueryTimeout(db.getConfig().queryTimeout);
			DatabaseUtils.prepareQueryForObjects(stmt, objects);

			ResultSet res = stmt.executeQuery();// wykonaj zapytanie

			while (res.next()) {// iteruj po wynikach
				List<DbFieldInfo> fields = lister.getDbFields();

				// load fields data and objects
				Map<DbFieldOrViewInfo, Object> map = new HashMap<>();
				for (DbFieldInfo df : fields) {
					Object obj = res.getObject(df.inTableName);// in table name
					map.put(df, obj);
				}

				// utwórz nową instancję klasy
				T newobj = DatabaseUtils.createInstance(clazz, db.getDb(), map);

				if (newobj instanceof DbEntity) {// informacje o pobranym obiekcie dla DbEntity
					((DbEntity) newobj).setQueryUpdate(true);
				}

				ret.add(newobj);// dodaj obiekt do zwracanej listy obiektów
			}

		} catch (SQLTimeoutException e) {
			throw new DatabaseException("SelectProcessor: SQL query timeout!", e);
		} catch (SQLException e) {// w przypadku błędu SQL rzuć wyjątek
			throw new DatabaseException("SelectProcessor: SQL query error!", e);
		}

		return ret;
	}

	/**
	 * Wykonaj złożone zapytanie SELECT.
	 * 
	 * @param listers listery dla pól w klasach przetwarzanych
	 * @return lista, w której znajduje się lista encji reprezentująca jeden wiersz
	 * @throws DatabaseException błąd SQL, lub błąd tworzenia obiektu
	 */
	List<SelectRow> processMultiple(Collection<FieldLister> listers) throws DatabaseException {
		List<SelectRow> ret = new ArrayList<SelectRow>();

		// names used in query, for adding others columns to map
		Set<String> usedInQueryNames = new HashSet<>();

		Connection conn = db.getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.KEEP_CURRENT_RESULT)) {
			stmt.setQueryTimeout(db.getConfig().queryTimeout);
			DatabaseUtils.prepareQueryForObjects(stmt, objects);

			ResultSet res = stmt.executeQuery();// wykonaj zapytanie
			ResultSetMetaData metaData = res.getMetaData();// metadane zapytania
			int columns = metaData.getColumnCount();// ilosc kolumn

			while (res.next()) {// iteruj po wynikach
				SelectRow row = new SelectRow();

				for (FieldLister lis : listers) {// dla wszystkich FieldListerow - pobierz jeden wiersz
					Class<?> clazz = (Class<?>) lis.getTableClass();
					List<DbFieldOrViewInfo> fields = lis.getDbFieldOrViewInfo();

					// load fields data and objects
					Map<DbFieldOrViewInfo, Object> map = new HashMap<>();
					for (DbFieldOrViewInfo df : fields) {
						String fieldName = df.getInQueryName();// unique name in query
						usedInQueryNames.add(fieldName);

						Object obj = res.getObject(fieldName);
						map.put(df, obj);
					}

					// utwórz nową instancję klasy, jeżeli nie wystąpiły same NULL (oznacza to, ze
					// nie ma obiektu w zapytaniu np JOIN)
					boolean createInstance = false;

					// sprawdz, czy mozna tworzyć obiekt
					for (Object f : map.values()) {
						if (f != null) {
							createInstance = true;// jeżeli wartośc inna niż NULL, można utworzyć instancje klasy
							break;
						}
					}

					if (createInstance) {
						Object newobj = DatabaseUtils.createInstance(clazz, db.getDb(), map);

						if (newobj instanceof DbEntity) {// informacje o pobranym obiekcie dla DbEntity
							((DbEntity) newobj).setQueryUpdate(true);
						}

						row.entities.add(newobj);// dodaj obiekt do zwracanej listy obiektow w wierszu
					}
				}

				// a teraz sprawdz czy sa jakies inne pola
				for (int i = 1; i <= columns; i++) {// liczymy kolumny od 1
					String colName = metaData.getColumnLabel(i);

					// check column name used in query, if not - add to map
					if (!usedInQueryNames.contains(colName)) {
						row.otherResults.put(colName, res.getObject(i));// add to map
					}
				}

				// dodaj wiersz
				ret.add(row);
			}

		} catch (SQLTimeoutException e) {
			throw new DatabaseException("SelectProcessor: SQL query timeout!", e);
		} catch (SQLException e) {// w przypadku błędu SQL rzuć wyjątek
			throw new DatabaseException("SelectProcessor: SQL query error!", e);
		}

		return ret;
	}

}
