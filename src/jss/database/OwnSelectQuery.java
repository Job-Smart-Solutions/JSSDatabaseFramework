package jss.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Klasa tworząca własne zapytanie SELECT
 * 
 * @author lukas
 */
public class OwnSelectQuery extends AbstractOwnQuery {
	private final List<SelectRow> results = new ArrayList<>();
	private final Set<Class<?>> skipClasses = new HashSet<>();// klasy encji której obiektów nie tworzyć

	/**
	 * Konstruktor
	 * 
	 * @param db połączenie z bazą danych
	 */
	OwnSelectQuery(Database db) {
		super(db);
	}

	/**
	 * Pomija wybraną klasę przy tworzeniu obiektów encji. Przydatne przy pobieraniu
	 * nie wszystkich pól z tabeli.
	 * 
	 * @param c klasa do pominięcia
	 */
	public void addSkipClass(Class<?> c) {
		skipClasses.add(c);
	}

	@Override
	protected void executeQuery() throws DatabaseException {
		// create field listers
		List<FieldLister> listers = new ArrayList<>();
		for (Class<?> clz : prepClasses) {
			if (!skipClasses.contains(clz)) {
				FieldLister lister = db.getFieldLister(clz);
				lister.listFields();

				listers.add(lister);
			}
		}

		SelectProcessor proc = new SelectProcessor(db, buildedQuery, objects);
		List<SelectRow> rows = proc.processMultiple(listers);

		results.addAll(rows);
	}

	/**
	 * Zwraca liste pobranych wierszy. Lista ta zawiera obiekty encji, oraz mape
	 * innych wyników.
	 * 
	 * @return lista pobranych wierszy
	 */
	public List<SelectRow> getRows() {
		return results;
	}

	/**
	 * Pojedyńczy wiersz pobrany z bazy danych. Zawiera liste obiektów encji, oraz
	 * mapę innych wyników.
	 */
	public static class SelectRow {
		List<Object> entities = new ArrayList<>();// entities
		Map<String, Object> otherResults = new HashMap<String, Object>();// map of other results

		SelectRow() {
		}

		/**
		 * Zwraca liste encji. Nie muszą być one jednego typu i zazwyczaj nie są.
		 * 
		 * @return lista encji
		 */
		public List<Object> getEntities() {
			return entities;
		}

		/**
		 * Zwraca mape innych wyników. String (klucz) to nazwa kolumny zwrócona w
		 * wyniku, a wartość to pole w wierszu.
		 * 
		 * @return mapa innych wyników
		 */
		public Map<String, Object> getOtherResults() {
			return otherResults;
		}
	}

}
