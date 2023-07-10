package lukas.database;

import java.util.ArrayList;
import java.util.List;

import lukas.database.FieldLister.DbFieldOrViewInfo;
import lukas.database.SelectOrderBy.OrderBy;

/**
 * Simple SELECT query
 * 
 * @author lukas
 */
public class SelectQuery<T> {
	private final Database db;

	private final Class<T> from;
	private final List<SelectOrderBy> orderBy = new ArrayList<SelectOrderBy>(1);// ORDER BY - sortowanie
	private final List<T> entities = new ArrayList<T>(1);

	private final List<Object> objects = new ArrayList<>(1);// objects to set in query
	private String buildedQuery;// builded query
	private FieldLister lister;
	private boolean wasExecuted = false;

	private int limitOffset = 0;// LIMIT: ilośc wyników do pominięcia
	private int limitCount = -1;// LIMIT: ilość wyników do pobrania
	private SelectWhere firstWhere;// WHERE

	/**
	 * @param db   połączenie z baza danych
	 * @param from klasa encji do pobrania
	 */
	SelectQuery(Database db, Class<T> from) {
		this.db = db;
		this.from = from;
	}

	/**
	 * Dodaje parametr LIMIT do zapytania, z ilością wyników jaka ma zostać pobrana
	 * 
	 * @param count ilość wyników do pobrania, lub -1 by pominąć parametr
	 */
	public void setLimit(int count) {
		setLimit(0, count);
	}

	/**
	 * Dodaje paramert LIMIT do zapytania, z ilością wyników do pominięcia oraz do
	 * pobrania. Jeżeli wartość count wynosi -1, a offset jest inna niż 0, zostanie
	 * to pominięte.
	 * 
	 * @param offset ilość wyników do pominięcia, lub 0 by pokazać wyniki od
	 *               początku
	 * @param count  ilość wyników do pobrania, lub -1 by pominąć parametr
	 */
	public void setLimit(int offset, int count) {
		if (count == -1 && offset != 0) {
			return;
		}

		this.limitOffset = offset;
		this.limitCount = count;
	}

	/**
	 * Dodaje sortowanie. Sortowanie jest wykonywane w takiej kolejności jak zostało
	 * dodane.
	 * 
	 * @param pole  nazwa pola w klasie po którym jest sortowanie
	 * @param order kolejność sortowania
	 * @see SelectOrderBy
	 */
	public void addOrderBy(String pole, OrderBy order) {
		addOrderBy(new SelectOrderBy(pole, order));
	}

	/**
	 * Dodaje sortowanie. Sortowanie jest wykonywane w takiej kolejności jak zostało
	 * dodane.
	 * 
	 * @param selectOrderBy obiekt opisujący pojedyńcze sortowanie
	 * @see SelectOrderBy
	 */
	public void addOrderBy(SelectOrderBy selectOrderBy) {
		orderBy.add(selectOrderBy);
	}

	/**
	 * Ustawia pierwszą instancję klasy {@link SelectWhere} po której nastąpi
	 * iterowanie dla WHERE w zapytaniu SELECT.
	 * 
	 * @param firstWhere piersza instancja klasy {@link SelectWhere}
	 * @see SelectWhere
	 */
	public void setFirstWhere(SelectWhere firstWhere) {
		this.firstWhere = firstWhere;
	}

	/**
	 * Zwraca liste pobranych obiektów encji
	 * 
	 * @return obiekty encji
	 */
	public List<T> getEntities() {
		return entities;
	}

	/**
	 * Zwraca pojedyńczą encję - pierwszy obiekt z pobranej listy.
	 * 
	 * @return pierwszy obiekt z listy, lub null gdy lista jest pusta
	 */
	public T getSingleEntity() {
		return (entities.size() > 0) ? entities.get(0) : null;
	}

	/**
	 * Wykonaj zapytanie SELECT do bazy danych. Metoda może być wywołana tylko raz.
	 * 
	 * @throws DatabaseException w przypadku błędu, lub ponowne wywołanie
	 */
	public void execute() throws DatabaseException {
		if (wasExecuted) {// rzuć wyjątek, gdy metoda wykonana została już wcześniej
			throw new DatabaseException("This SELECT query was already executed!");
		}

		// utwórz zapytanie
		buildQuery();
		db.logQuery(buildedQuery);

		// przetwarzanie zapytania
		SelectProcessor proc = new SelectProcessor(db, buildedQuery, objects);
		List<T> objs = proc.processSingle(lister);

		entities.addAll(objs);

		wasExecuted = true;// więcej niż raz nie można wykonać tej metody
	}

	/**
	 * Buduje zapytanie SELECT
	 * 
	 * @throws DatabaseException w przypadku błędu
	 */
	private void buildQuery() throws DatabaseException {
		char escape = db.getDb().getEscapeChar();// escape char

		// lister
		FieldLister lister = db.getFieldLister(from);
		lister.listFields();

		// query
		StringBuilder sb = new StringBuilder("SELECT ");

		// jakie pola maja zostac pobrane?
		sb.append("*");

		// FROM
		sb.append(" FROM ").append(escape).append(lister.getTableName()).append(escape);

		// WHERE
		if (firstWhere != null) {
			sb.append(" WHERE ");

			SelectWhere current = firstWhere;
			boolean searchNextElement = false;// czy szukać nastepnego elementu
			boolean canGetInside = true;// czy może wejść do środka elementu?
			while (true) {
				if (searchNextElement) {// szukaj następnego elementu
					if (canGetInside && current.hasInside()) {// czy ma zagnieżdzony warunek?
						current = current.inside();
						sb.append(' ').append(current.connectorToString()).append(" (");
					} else if (current.hasNext()) {// lub czy ma następny element?
						current = current.next();
						sb.append(' ').append(current.connectorToString()).append(' ');
					} else if (current.hasParent()) {// lub czy ma rodzica?
						sb.append(" )");// zakończ zagnieżdzenie
						canGetInside = false;
						current = current.parent();
						continue;// przerwij aktualna iteracje pętli, by szukać następnego elementu lub rodzica
					} else {
						break;// jezeli nic nie znaleziono, to przerwij pętle
					}

					canGetInside = true;// jeżeli nie przerwano przy szukaniu rodzica, znaleziono następny element
				} else {
					searchNextElement = true;// szukaj następnego elementu, po pierwszej iteracji
				}

				// dodaj nazwe pola i typ porównania
				DbFieldOrViewInfo field = lister.getFieldByInClassName(current.field);
				if (field == null) {
					throw new DatabaseException("Not found field " + from.getSimpleName() + "::" + current.field);
				}
				sb.append(escape).append(field.getInTableName()).append(escape);
				sb.append(' ').append(current.typeToString());

				// jeżeli nie jest to IS NULL lub IS NOT NULL dodaj pierwszą wartość
				if (!current.isNullOrNotNull()) {
					sb.append(" ?");
					objects.add(current.value1);
					// jeżeli jest to BETWEEN lub NOT BETWEEN dodaj drugą wartość
					if (current.isBetweenOrNotBetween()) {
						sb.append(" AND ?");
						objects.add(current.value2);
					}
				}
			}
		}

		// ORDER BY
		if (orderBy.size() > 0) {
			sb.append(" ORDER BY ");
			for (int i = 0; i < orderBy.size(); i++) {
				if (i > 0) {
					sb.append(", ");
				}

				SelectOrderBy ob = orderBy.get(i);
				DbFieldOrViewInfo field = lister.getFieldByInClassName(ob.getField());
				if (field == null) {
					throw new DatabaseException("Not found field " + from.getSimpleName() + "::" + ob.getField());
				}
				sb.append(escape).append(field.getInTableName()).append(escape);
				sb.append(' ').append(ob.getOrderByString());
			}
		}

		// LIMIT
		if (limitCount != -1) {
			String limitStr = db.getDb().getLimit();
			limitStr = limitStr.replace("{rowcount}", Integer.toString(limitCount));
			limitStr = limitStr.replace("{offset}", Integer.toString(limitOffset));
			sb.append(' ').append(limitStr);
		}

		buildedQuery = sb.toString();
		this.lister = lister;
	}

}
