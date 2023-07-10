package jss.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jss.database.FieldLister.DbFieldInfo;
import jss.database.FieldLister.DbFieldOrViewInfo;

/**
 * Own query to database
 * 
 * @author lukas
 */
public abstract class AbstractOwnQuery {
	protected final Database db;
	private boolean wasExecuted = false;

	// wlasne zapytanie
	protected String ownQuery = null;// user own query
	protected String buildedQuery = null;// builded query for prepared statement

	// obiekty
	protected final Set<Class<?>> prepClasses = new LinkedHashSet<Class<?>>();// klasy do preparowania zapytania
	protected final Map<String, Object> ownParams = new HashMap<String, Object>();// parametry zapytania
	protected final List<Object> objects = new ArrayList<Object>();// do wstawienia za pytajniki w stmt

	/**
	 * Konstruktor
	 * 
	 * @param db połączenie z bazą danych
	 */
	AbstractOwnQuery(Database db) {
		this.db = db;
	}

	/**
	 * Ustawia własne zparametryzowane zapytanie. Parametry te są zastąpione czystym
	 * SQL po przekonwertowaniu zapytania. <br>
	 * 
	 * Parametry: <br>
	 * {TABLE:className} - klasa encji <br>
	 * {FIELD:className:fieldName} - pole w klasie <br>
	 * {PARAM:name} - parametr wpłasny o nazwie 'name' <br>
	 * {LISTFIELDS:className} - lista pol w klasie, przydatne do SELECT <br>
	 * 
	 * @param query własne zapytanie
	 * @throws DatabaseException error (can throw only when sets own query in
	 *                           simulated view)
	 */
	public void setOwnQuery(String query) throws DatabaseException {
		this.ownQuery = query;
	}

	/**
	 * Dodaje klase do przygotowania zapytania
	 * 
	 * @param c klasa do preparowania zapytania
	 */
	public void addPreparingClass(Class<?> c) {
		prepClasses.add(c);
	}

	/**
	 * Ustawia wartość parametru własnego.
	 * 
	 * @param param nazwa parametru
	 * @param value wartość
	 */
	public void setParam(String param, Object value) {
		ownParams.put(param, value);
	}

	/**
	 * Wykonuje zapytanie do bazy danych. Metoda może być wysyłana tylko raz.
	 * 
	 * @throws DatabaseException błąd, lub wywołanie execute drugi raz
	 */
	public void execute() throws DatabaseException {
		if (wasExecuted) {// rzuć wyjątek, gdy metoda wykonana została już wcześniej
			throw new DatabaseException("This query was already executed!");
		}

		// build query
		buildQuery();
		db.logQuery(buildedQuery);

		// execute
		executeQuery();

		wasExecuted = true;
	}

	/**
	 * Wykonanie zapytania
	 * 
	 * @throws DatabaseException błąd bazy danych
	 */
	protected abstract void executeQuery() throws DatabaseException;

	/**
	 * Buduje zapytanie dla dodanych wcześniej klas.
	 * 
	 * @throws DatabaseException not found field in class
	 */
	private void buildQuery() throws DatabaseException {
		char escape = db.getDb().getEscapeChar();// escape char

		buildedQuery = ownQuery;
		List<String[]> params = getParams();

		for (Class<?> c : prepClasses) {
			FieldLister lister = db.getFieldLister(c);
			lister.listFields();

			String tblName = escape + lister.getTableName() + escape;

			// get parameters only for current processing class
			List<String[]> paramsInClass = getParamsByClassName(params, c.getSimpleName());

			for (String[] param : paramsInClass) {

				// LISTFIELDS - list fields from class
				if (param[0].equals("LISTFIELDS")) {
					StringBuilder fnamesStr = new StringBuilder();

					for (DbFieldInfo df : lister.getDbFields()) {
						if (fnamesStr.length() > 0) {
							fnamesStr.append(", ");
						}

						fnamesStr.append(tblName);
						fnamesStr.append('.').append(escape).append(df.inTableName).append(escape);
						fnamesStr.append(" AS ").append(escape).append(df.getInQueryName()).append(escape);
					}

					buildedQuery = buildedQuery.replace(getAsStringInQuery(param), fnamesStr.toString());
				}

				// TABLE name
				if (param[0].equals("TABLE")) {// zamien nazwe tabeli
					buildedQuery = buildedQuery.replace(getAsStringInQuery(param), tblName);
				}

				// FIELD in class
				if (param[0].equals("FIELD")) {// zamien nazwe pola
					DbFieldOrViewInfo field = lister.getFieldByInClassName(param[2]);// pobierz nazwe pola
					if (field == null) {
						throw new DatabaseException("Not found field " + param[2] + " in class " + c.getSimpleName());
					}

					String fname = getFieldInQueryName(field, lister);
					buildedQuery = buildedQuery.replace(getAsStringInQuery(param), fname);
				}
			}
		}

		// own params
		buildQueryOwnParams(params);
	}

	/**
	 * Build own params in query
	 * 
	 * @param params params in query
	 * @throws DatabaseException
	 */
	protected void buildQueryOwnParams(List<String[]> params) throws DatabaseException {
		// all own parameters
		for (String[] param : params) {
			if (param[0].equals("PARAM")) {// own parameter
				buildedQuery = buildedQuery.replace(getAsStringInQuery(param), "?");// parameter
				objects.add(ownParams.get(param[1]));// add parameter to list
			}
		}
	}

	/**
	 * Name for field in query
	 * 
	 * @param field  field
	 * @param lister field lister
	 * @return name for query
	 */
	protected String getFieldInQueryName(DbFieldOrViewInfo field, FieldLister lister) {
		char escape = db.getDb().getEscapeChar();// escape char
		String tblName = escape + lister.getTableName() + escape;
		return tblName + '.' + escape + field.getInTableName() + escape;
	}

	/**
	 * Zwraca liste parametrów jako lista tablicy stringów. <br>
	 * 
	 * Dla TABLE tablica 2 elementy: [0] - TABLE, [1] - class name <br>
	 * Dla FIELD tablica 3 elementy: [0] - FIELD, [1] - class, [2] - field<br>
	 * Dla PARAM tablica 2 elementy: [0] - PARAM, [1] - nazwa parametru <br>
	 * Dla LISTFIELDS tablica 2 elementy: [0] - LISTFIELDS, [1] - nazwa klasy <br>
	 * 
	 * @return lista parametrów
	 */
	private List<String[]> getParams() {
		List<String[]> ret = new ArrayList<String[]>();

		String[] splited = ownQuery.split("[{}]");// regex - start '{', end '}'

		// add only parameters
		for (String s : splited) {
			if (s.startsWith("TABLE") || s.startsWith("FIELD") || s.startsWith("PARAM") || s.startsWith("LISTFIELDS")) {
				ret.add(s.split(":"));
			}
		}

		return ret;
	}

	/**
	 * Na podstawie parametrów zwraca pełny String, który ma zostać zamieniony
	 * (replace)
	 * 
	 * @param params parametry
	 * @return pełny String do zamiany (replace)
	 */
	private static String getAsStringInQuery(String[] params) {
		StringBuilder sb = new StringBuilder("{");

		for (int i = 0; i < params.length; i++) {
			if (i > 0) {
				sb.append(':');
			}

			sb.append(params[i]);
		}

		return sb.append('}').toString();
	}

	/**
	 * Pobiera liste parametrów TABLE i FIELD z listy parametrów, na podstawie
	 * podanej klasy encji
	 * 
	 * @param params    lista parametrów do przeszukania
	 * @param className nazwa klasy
	 * @return lista parametrów dla zamiany w danej klasie encji
	 */
	private static List<String[]> getParamsByClassName(List<String[]> params, String className) {
		List<String[]> ret = new ArrayList<String[]>();

		for (String[] str : params) {
			if (str[0].equals("TABLE") || str[0].equals("FIELD") || str[0].equals("LISTFIELDS")) {
				if (str[1].trim().equals(className)) {
					ret.add(str);
				}
			}
		}

		return ret;
	}

}
