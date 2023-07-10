package lukas.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lukas.database.FieldLister.DbFieldInfo;

/**
 * Data saver. For INSERT, UPDATE and DELETE.
 * 
 * @author lukas
 */
public class DataSaver {
	private final Database db;

	public DataSaver(Database db) {
		this.db = db;
	}

	/**
	 * Check is DbEntity new object in database? If true, next will be INSERT.
	 * 
	 * @param obj object to chech
	 * @return is DbEntity new object?
	 */
	public static boolean isNewObjectInDatabase(DbEntity obj) {
		return !(obj.isQueryUpdate());
	}

	/**
	 * Save single DbEntity. Determine INSERT or UPDATE
	 * 
	 * @param obj object to save
	 * @return modified rows count
	 * @throws DatabaseException
	 */
	public int save(DbEntity obj) throws DatabaseException {
		if (isNewObjectInDatabase(obj)) {
			return insert(obj);
		} else {
			return update(obj);
		}
	}

	/**
	 * Save collection of DbEntity objects. Determine which will be INSERT and which
	 * will be UPDATE.
	 * 
	 * @param objs collection od DbEntity objects to save (INSERT or UPDATE)
	 * @return modified rows count
	 * @throws DatabaseException
	 */
	public int save(Collection<? extends DbEntity> objs) throws DatabaseException {
		List<DbEntity> toInsert = new ArrayList<>();
		List<DbEntity> toUpdate = new ArrayList<>();

		for (DbEntity ent : objs) {
			if (isNewObjectInDatabase(ent)) {
				toInsert.add(ent);
			} else {
				toUpdate.add(ent);
			}
		}

		int ret = insert(toInsert);
		ret += update(toUpdate);
		return ret;
	}

	/**
	 * Insert one object
	 * 
	 * @param obj object to insert
	 * @return modified rows count
	 * @throws DatabaseException
	 */
	public int insert(Object obj) throws DatabaseException {
		return insert(Collections.singleton(obj));
	}

	/**
	 * Insert collection of objects
	 * 
	 * @param obj collection of objects
	 * @return modified rows count
	 * @throws DatabaseException
	 */
	public int insert(Collection<? extends Object> objs) throws DatabaseException {
		return doQuery(objs, QueryType.INSERT);
	}

	/**
	 * Insert collection of objects
	 * 
	 * @param objs                collection of objects
	 * @param ignoreAutoincrement ignore autoincrement?
	 * @return modified rows count
	 * @throws DatabaseException
	 */
	public int insert(Collection<? extends Object> objs, boolean ignoreAutoincrement) throws DatabaseException {
		return doQuery(objs, QueryType.INSERT, ignoreAutoincrement);
	}

	/**
	 * Update one object
	 * 
	 * @param obj object to update
	 * @return modified rows count
	 * @throws DatabaseException
	 */
	public int update(Object obj) throws DatabaseException {
		return update(Collections.singleton(obj));
	}

	/**
	 * Update collection of objects
	 * 
	 * @param objs collection of objects
	 * @return modified rows count
	 * @throws DatabaseException
	 */
	public int update(Collection<? extends Object> objs) throws DatabaseException {
		return doQuery(objs, QueryType.UPDATE);
	}

	/**
	 * Delete one object
	 * 
	 * @param obj object to delete
	 * @return modified rows count
	 * @throws DatabaseException
	 */
	public int delete(Object obj) throws DatabaseException {
		return delete(Collections.singleton(obj));
	}

	/**
	 * Delete collection of objects
	 * 
	 * @param objs collection of objects
	 * @return modified rows count
	 * @throws DatabaseException
	 */
	public int delete(Collection<? extends Object> objs) throws DatabaseException {
		return doQuery(objs, QueryType.DELETE);
	}

	/**
	 * Build and execute query
	 * 
	 * @param objs collection of objects
	 * @param type query type
	 * @return modified rows count
	 * @throws DatabaseException
	 */
	private int doQuery(Collection<? extends Object> objs, QueryType type) throws DatabaseException {
		return doQuery(objs, type, false);
	}

	/**
	 * Build and execute query
	 * 
	 * @param objs                collection of objects
	 * @param type                query type
	 * @param ignoreAutoincrement ignore autoincrement?
	 * @return modified rows count
	 * @throws DatabaseException
	 */
	private int doQuery(Collection<? extends Object> objs, QueryType type, boolean ignoreAutoincrement)
			throws DatabaseException {

		if (objs.size() < 1) {
			return 0;// jeżeli brak obiektów, nic nie rób
		}

		Class<?> clazz = checkIsSameClasses(objs);
		FieldLister lister = db.getFieldLister(clazz);
		lister.listFields();

		String query = buildQuery(lister, type, ignoreAutoincrement);// zbuduj zapytanie insert

		int modifiedRows = 0;

		// try connection
		Connection conn = db.getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setQueryTimeout(db.getConfig().queryTimeout);

			for (Object obj : objs) {
				// wykonaj zapytanie
				prepareQuery(lister, obj, type, stmt, ignoreAutoincrement);
				modifiedRows += stmt.executeUpdate();

				if(obj instanceof DbEntity) {
					((DbEntity) obj).setQueryUpdate(true);//object saved to database
				}
				
				// jeżeli wygenerowano ID zapisz je do obiektu
				if (ignoreAutoincrement == false && type == QueryType.INSERT) {
					getGeneratedId(lister, obj, stmt, conn);
				}
			}
		} catch (SQLTimeoutException e) {
			throw new DatabaseException("DataSaver: SQL query timeout!", e);
		} catch (SQLException e) {
			throw new DatabaseException("DataSaver: SQL query error in class " + clazz.getName(), e);
		}

		return modifiedRows;
	}

	/**
	 * All classes is the same on one query?
	 * 
	 * @param objs objects to check classes
	 * @return objects class
	 * @throws DatabaseException if not the same classes
	 */
	private Class<?> checkIsSameClasses(Collection<? extends Object> objs) throws DatabaseException {
		Class<?> last = null;

		for (Object o : objs) {
			if (last != null) {
				if (!last.equals(o.getClass())) {
					throw new DatabaseException("DataSaver: not the same classes in query!");
				}
			} else {
				last = o.getClass();
			}
		}

		return last;
	}

	/**
	 * Get last insert generated id, and save it into object
	 * 
	 * @param lister     field lister
	 * @param obj        object, to save generated id in
	 * @param stmt       SQL query statement
	 * @param connection connection wit database
	 * @return generated ID
	 * @throws DatabaseException
	 */
	private long getGeneratedId(FieldLister lister, Object obj, PreparedStatement stmt, Connection connection)
			throws DatabaseException {

		DbFieldInfo autoincrementPk = null;
		for (DbFieldInfo pkInfo : lister.getPrimaryKeys()) {
			if (pkInfo.getPrimaryKeyInfo().getPkAnnotation().autoIncrement()) {
				autoincrementPk = pkInfo;
				break;
			}
		}

		long id = -1;
		try {
			if (autoincrementPk != null) {
				ResultSet idResult = stmt.getGeneratedKeys();// pobierz wygenerowane ID
				if (idResult.next()) {
					id = idResult.getLong(1);

					// ustaw nowe ID w obiekcie
					try {
						Object idObj = db.getDb().convertDbToObj(id, autoincrementPk);
						autoincrementPk.fieldInClass.set(obj, idObj);
					} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
						throw new DatabaseException("Cannot set ID field in object: " + obj.getClass().getName(), e);
					}
				} else {
					throw new DatabaseException("Cannot read ID for object: " + obj.getClass().getName());
				}
			}
		} catch (SQLTimeoutException e) {
			throw new DatabaseException("DataSaver: SQL query timeout!", e);
		} catch (SQLException e) {
			throw new DatabaseException("DataSaver: SQL query error in class " + obj.getClass().getName(), e);
		}

		return id;
	}

	/**
	 * Build query. For argument in connection.prepareStatement()
	 * 
	 * @param lister              field lister
	 * @param queryType           query type
	 * @param ignoreAutoIncrement ignore autoincrement, save PK from object
	 * @return builded query
	 */
	private String buildQuery(FieldLister lister, QueryType queryType, boolean ignoreAutoIncrement) {
		char escape = db.getDb().getEscapeChar();
		StringBuilder sb = new StringBuilder();

		// pobierz informacje o polach
		List<DbFieldInfo> fields = lister.getDbFields();
		List<DbFieldInfo> primaryKeys = lister.getPrimaryKeys();

		// jeżeli istnieje pole autoinkrementacji, pomiń je w polach (ale nie w kluczach
		// podstawowowych)
		if (ignoreAutoIncrement == false) {// tylko gdy brak pomijania autoinkrementacji
			for (DbFieldInfo pkField : primaryKeys) {
				if (pkField.getPrimaryKeyInfo().getPkAnnotation().autoIncrement()) {
					fields.remove(pkField);
				}
			}
		}

		// zapytanie INSERT
		if (queryType.equals(QueryType.INSERT)) {
			sb.append("INSERT INTO ").append(escape).append(lister.getTableName()).append(escape).append(" ( ");

			for (int i = 0; i < fields.size(); i++) {
				if (i > 0) {
					sb.append(',');
				}

				sb.append(escape).append(fields.get(i).inTableName).append(escape);
			}

			sb.append(" ) VALUES ( ");

			for (int i = 0; i < fields.size(); i++) {
				if (i == 0) {
					sb.append('?');
				} else {
					sb.append(", ?");
				}
			}

			sb.append(')');
		}
		// zapytanie UPDATE
		else if (queryType.equals(QueryType.UPDATE)) {
			sb.append("UPDATE ").append(escape).append(lister.getTableName()).append(escape).append(" SET ");

			for (int i = 0; i < fields.size(); i++) {
				if (i > 0) {
					sb.append(',');
				}

				sb.append(escape).append(fields.get(i).inTableName).append(escape).append(" = ?");
			}

			// który rekord mamy edytować?
			sb.append(" WHERE ");

			// po kluczach podstawowoych
			for (int i = 0; i < primaryKeys.size(); i++) {
				if (i > 0) {
					sb.append(" AND ");
				}

				sb.append(escape).append(primaryKeys.get(i).inTableName).append(escape).append(" = ?");
			}
		}
		// zapytanie DELETE
		else if (queryType.equals(QueryType.DELETE)) {
			sb.append("DELETE FROM ").append(escape).append(lister.getTableName()).append(escape).append(" WHERE ");

			for (int i = 0; i < primaryKeys.size(); i++) {// usuwanie tylko po kluczach podstawowowych
				if (i > 0) {
					sb.append(" AND ");
				}

				sb.append(escape).append(primaryKeys.get(i).inTableName).append(escape).append(" = ?");
			}
		}

		// generated SQL
		String query = sb.toString();
		db.logQuery(query);

		return query;
	}

	/**
	 * Set data in SQL query. Can be used multiple times, for many objects in the
	 * same query.
	 * 
	 * @param lister              field lister
	 * @param queryType           query type
	 * @param stmt                SQL query statement
	 * @param ignoreAutoIncrement ignore autoincrement?
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	private void prepareQuery(FieldLister lister, Object obj, QueryType queryType, PreparedStatement stmt,
			boolean ignoreAutoIncrement) throws SQLException, DatabaseException {
		int index = 1;// licznik dla ustawiania obiektów

		// pobierz pola z bazy danych
		List<DbFieldInfo> fields = lister.getDbFields();
		List<DbFieldInfo> primaryKeys = lister.getPrimaryKeys();

		// jeżeli istnieje pole autoinkrementacji, pomiń je
		if (ignoreAutoIncrement == false) {// tylko gdy brak pomijania autoinkrementacji
			for (DbFieldInfo pkField : primaryKeys) {
				if (pkField.getPrimaryKeyInfo().getPkAnnotation().autoIncrement()) {
					fields.remove(pkField);
				}
			}
		}

		// zapytanie INSERT lub UPDATE - pola w obiekcie
		if (queryType == QueryType.INSERT || queryType == QueryType.UPDATE) {
			// ustaw tylko pola do zapisu
			for (DbFieldInfo f : fields) {
				Object objDb = getDbObject(f, obj);
				stmt.setObject(index, objDb);
				index++;
			}
		}

		// zapytanie UPDATE lub DELETE - ustaw klucze podstawowe
		if (queryType == QueryType.UPDATE || queryType == QueryType.DELETE) {
			// ustaw klucze podstawowe do zidentyfikowania rekordu w bazie
			for (DbFieldInfo pk : primaryKeys) {				
				Object objDb = getDbObject(pk, obj);
				stmt.setObject(index, objDb);
				index++;
			}
		}
	}

	/**
	 * Get database object (for single table field)
	 * 
	 * @param info field info (field from class)
	 * @param obj  current processing object (table row)
	 * @return database object, for single table field
	 * @throws DatabaseException
	 */
	private Object getDbObject(DbFieldInfo info, Object obj) throws DatabaseException {
		try {
			Object objJava = info.getFieldInClass().get(obj);
			return db.getDb().convertObjToDb(objJava, info);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new DatabaseException("DataSaver: Cannot convert object in field " + info.fieldInClass.getName(), e);
		}
	}

	/**
	 * Database query type
	 */
	private enum QueryType {
		INSERT, UPDATE, DELETE
	}

}
