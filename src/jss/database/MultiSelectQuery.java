package jss.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jss.database.FieldLister.DbFieldInfo;
import jss.database.FieldLister.DbManyToOneInfo;
import jss.database.FieldLister.DbOneToOneInfo;
import jss.database.OwnSelectQuery.SelectRow;

import java.util.Set;

/**
 * Select multiple objects from query. Supports nested queries.
 * 
 * Every entity classes must have definied equals and hascode methods!!!
 * 
 * @param <T> class for main object
 * 
 * @author lukas
 */
public class MultiSelectQuery<T> extends AbstractOwnQuery {
	protected final Class<T> from;
	protected final List<T> entities;

	// Used objects - for checking has any instance.
	// Map has better performance than search in lists.
	// Map - object by hashcode and equals (key) for find object instance (value)
	private final Map<Object, Object> usedObjects = new HashMap<>();

	// obiekty
	private final Set<Class<?>> skipClasses = new LinkedHashSet<Class<?>>();// klasy encji której obiektów nie tworzyć

	/**
	 * @param db   połączenie z bazą danych
	 * @param from klasa główna
	 */
	MultiSelectQuery(Database db, Class<T> from) {
		super(db);
		addPreparingClass(from);
		this.from = from;
		this.entities = new ArrayList<T>();
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
		Map<Class<?>, FieldLister> listers = createFieldListers();

		SelectProcessor proc = new SelectProcessor(db, buildedQuery, objects);
		List<SelectRow> results = proc.processMultiple(listers.values());

		// utwórz obiekty
		List<Object> list = processObject(results, listers, from);

		// cast ok - processObject class is from
		@SuppressWarnings("unchecked")
		List<T> listOk = (List<T>) list;
		this.entities.addAll(listOk);
	}

	/**
	 * Create fields listers
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	protected Map<Class<?>, FieldLister> createFieldListers() throws DatabaseException {
		Map<Class<?>, FieldLister> listers = new LinkedHashMap<>();
		for (Class<?> clz : prepClasses) {
			if (!skipClasses.contains(clz)) {
				FieldLister lister = db.getFieldLister(clz);
				lister.listFields();

				listers.put(clz, lister);
			}
		}
		return listers;
	}

	/**
	 * Utwórz obiekty
	 * 
	 * @param results   dane z bazy
	 * @param listers   listery
	 * @param mainClass klasa głowna
	 * @return
	 * @throws DatabaseException
	 */
	protected List<Object> processObject(List<SelectRow> results, Map<Class<?>, FieldLister> listers,
			Class<?> mainClass) throws DatabaseException {

		List<Object> objs = createObjs(results, listers.get(mainClass), mainClass);

		for (Entry<Class<?>, FieldLister> lister : listers.entrySet()) {
			if (lister.getValue() != listers.get(mainClass) && lister.getValue() != listers.get(from)) {
				loadObjs(results, lister.getValue());
			}
		}

		return objs;
	}

	/**
	 * Tworzy obiekty. Ładuje obiekty do obiektu głównego.
	 * 
	 * @param results dane z bazy
	 * @return
	 * @throws DatabaseException
	 */
	private List<Object> createObjs(List<SelectRow> results, FieldLister mainLister, Class<?> fromClass)
			throws DatabaseException {

		List<DbFieldInfo> foreignKeys = mainLister.getForeignKeys();// all foreign keys
		List<DbManyToOneInfo> manyToOneList = mainLister.getDbManyToOne();// all ManyToOne relations
		List<DbOneToOneInfo> oneToOneList = mainLister.getDbOneToOne();// all OneToOne relations

		List<Object> ret = new ArrayList<>();

		for (SelectRow res : results) {
			List<Object> listE = res.getEntities();

			Object current = null;

			for (Object e : listE) {
				e = getFromUsedObjects(e);

				if (fromClass.isAssignableFrom(e.getClass())) {
					Object item = e;
					if (!ret.contains(item)) {
						ret.add(item);
						current = item;
					} else {// list contains this element
						int index = ret.indexOf(item);
						current = ret.get(index);
					}
				} else {
					loadForeign(foreignKeys, manyToOneList, oneToOneList, current, e);
				}
			}

		}

		return ret;
	}// createObjs

	/**
	 * Ładuje dodatkowe obiekty wewnątrz zapytania. Ładuje te obiekty, których nie
	 * załadowano do obiektu głownego.
	 * 
	 * @param results wyniki z bazy danych
	 * @param lister  aktualny lister - aktualnie przetwarzana
	 * @throws DatabaseException
	 */
	private void loadObjs(List<SelectRow> results, FieldLister lister) throws DatabaseException {
		List<DbFieldInfo> foreignKeys = lister.getForeignKeys();// all foreign keys
		List<DbManyToOneInfo> manyToOneList = lister.getDbManyToOne();// all ManyToOne relations
		List<DbOneToOneInfo> oneToOneList = lister.getDbOneToOne();// all OneToOne relations

		for (SelectRow res : results) {
			List<Object> listE = res.getEntities();

			Object current = null;

			for (Object e : listE) {
				e = getFromUsedObjects(e);

				if (lister.getTableClass().isAssignableFrom(e.getClass())) {
					current = e;
				} else {
					if (current == null) {
						continue;
					}
					loadForeign(foreignKeys, manyToOneList, oneToOneList, current, e);
				}
			}
		}
	}

	/**
	 * Ładuje obce obiekty
	 * 
	 * @param foreignKeys   klucze obce
	 * @param manyToOneList relacje wiele do jednego
	 * @param to            obiekt do którego załadować
	 * @param obj           aktualnie przeszukiwany obiekt
	 * @throws DatabaseException
	 */
	@SuppressWarnings("unchecked")
	protected void loadForeign(List<DbFieldInfo> foreignKeys, List<DbManyToOneInfo> manyToOneList,
			List<DbOneToOneInfo> oneToOneList, Object to, Object obj) throws DatabaseException {

		for (DbFieldInfo fk : foreignKeys) {// one key
			if (fk.getForeignKeyInfo().getFkAnnotation().refObject().isAssignableFrom(obj.getClass())) {
				try {
					fk.getForeignKeyInfo().thisField.set(to, obj);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					String fname = fk.getFieldInClass().getName();
					String cname = to.getClass().getSimpleName();
					throw new DatabaseException("Cannot set field value " + fname + " in class " + cname, e);
				}
			}
		}

		for (DbManyToOneInfo mto : manyToOneList) {// collection
			if (mto.getMtoAnnotation().refObject().isAssignableFrom(obj.getClass())) {
				// pobierz z pola obiekt kolekcji, do którego zostaną zapisane wyniki
				Collection<Object> collection = null;
				try {
					collection = (Collection<Object>) mto.fieldInClass.get(to);// rzutowanie zawsze poprawne, mozna
																				// wyłaczyc warning
				} catch (IllegalArgumentException | IllegalAccessException | ClassCastException e) {
					String fname = mto.fieldInClass.getName();
					throw new DatabaseException("Field " + fname + " cannot be cast to Collection!", e);
				}

				if (!collection.contains(obj)) {
					collection.add(obj);

					// dodaj referencje do obiektu na który wskazywało ManyToOne
					if (mto.refFieldObj != null) {// jeżeli istnieje pole, gdzie ją zapisać
						try {
							mto.refFieldObj.set(obj, to);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							String fname = mto.refFieldObj.getName();
							String cname = obj.getClass().getSimpleName();
							throw new DatabaseException("Cannot set field value " + fname + " in class " + cname, e);
						}
					}
				}

			}
		} // many to one

		for (DbOneToOneInfo oto : oneToOneList) {// one to one relation
			if (oto.getOtoAnnotation().refObject().isAssignableFrom(obj.getClass())) {
				try {
					oto.fieldInClass.set(to, obj);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					String fname = oto.fieldInClass.getName();
					String cname = to.getClass().getSimpleName();
					throw new DatabaseException("OneToOne: Cannot set field value " + fname + " in class " + cname, e);
				}

				// dodaj referencje do obiektu na który wskazywało OneToOne
				if (oto.refFieldObj != null) {// jeżeli istnieje pole, gdzie ją zapisać
					try {
						oto.refFieldObj.set(obj, to);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						String fname = oto.refFieldObj.getName();
						String cname = obj.getClass().getSimpleName();
						throw new DatabaseException("OneToOne: Cannot set field value " + fname + " in class " + cname,
								e);
					}
				}
			}
		}
	}

	private Object getFromUsedObjects(Object obj) throws DatabaseException {
		if (obj != null) {
			Object o = usedObjects.get(obj);
			if (o != null) {
				return o;
			}

			// if obj not found - add to map
			usedObjects.put(obj, obj);
		}

		return obj;
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
	 * @return pierwszy obiekt z listy, lub NULL gdy lista jest pusta
	 */
	public T getSingleEntity() {
		return (entities.size() > 0) ? entities.get(0) : null;
	}

}
