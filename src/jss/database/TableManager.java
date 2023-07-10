package jss.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

import jss.database.FieldLister.DbFieldInfo;
import jss.database.FieldLister.DbForeignKeyInfo;
import jss.database.FieldLister.DbPrimaryKeyInfo;
import jss.database.annotations.DbTable;

/**
 * Manage tables
 * 
 * @author lukas
 */
public class TableManager {
	private final Database db;

	TableManager(Database db) {
		this.db = db;
	}

	/**
	 * Tworzy tabele z kolekcji (listy, zbioru) klas encji.
	 * 
	 * @param tables kolekcja klas encji
	 * @throws DatabaseException gdy brak adnotacji {@link DbTable} w przynajmniej
	 *                           jednej z klas, lub gdy wystąpi błąd podczas
	 *                           tworzenia tabeli
	 * @see DbTable
	 */
	public void createTables(Collection<Class<?>> tableClasses) throws DatabaseException {
		// sprawdz, czy są adnotacje w każdej z klas
		for (Class<?> tableClass : tableClasses) {
			checkAnnotationInTable(tableClass);
		}

		// jeżeli są adnotacje, utwórz tabele
		for (Class<?> tableClass : tableClasses) {
			createTable(tableClass);
		}
	}

	/**
	 * Tworzy tabele z pojedyńczej kasy encji
	 * 
	 * @param table klasa encji
	 * @throws DatabaseException gdy brak adnotacji {@link DbTable} w klasie, lub
	 *                           gdy wystąpi błąd podczas tworzenia tabeli
	 * @see DbTable
	 */
	public void createTable(Class<?> tableClass) throws DatabaseException {
		checkAnnotationInTable(tableClass);

		char escape = db.getDb().getEscapeChar();

		// wylistuj wszystkie pola do utworzenia
		FieldLister lister = db.getFieldLister(tableClass);
		lister.listFields();
		lister.loadDatabaseTypes(db.getConfig().sqlTypeMapper);
		String tableName = lister.getTableName();

		// zapytanie SQL utworzenia tabeli
		StringBuilder sb = new StringBuilder();
		sb.append(db.getDb().getCreateTable().replace("{tablename}", escape + tableName + escape)).append(" ( ");

		// utwórz pola
		List<DbFieldInfo> fields = lister.getDbFields();
		List<DbFieldInfo> primaryKeys = lister.getPrimaryKeys();
		List<DbFieldInfo> foreignKeys = lister.getForeignKeys();

		for (int i = 0; i < fields.size(); i++) {
			DbFieldInfo fieldInfo = fields.get(i);

			if (i > 0) {// fields separator
				sb.append(", ");
			}

			// field
			sb.append(escape).append(fieldInfo.inTableName).append(escape).append(' ').append(fieldInfo.sqlTypeString);
			if (!fieldInfo.getFieldAnnotation().canNull()) {// not null fields
				sb.append(" NOT NULL");
			}
			if (fieldInfo.getFieldAnnotation().isUnique()) {// unique field
				sb.append(" UNIQUE");
			}

			// only one primary key can be autoincrement
			// if exists, add to table, if only is one primary key
			if (primaryKeys.size() == 1 && fieldInfo.getPrimaryKeyInfo() != null) {
				DbPrimaryKeyInfo pk = fieldInfo.getPrimaryKeyInfo();
				sb.append(" PRIMARY KEY");
				if (pk.pkAnnotation.autoIncrement()) {// autoincrement
					sb.append(' ').append(db.getDb().getAutoincrement());
				}
			}
		}

		// if there are more than one primary key
		if (primaryKeys.size() > 1) {
			sb.append(", PRIMARY KEY (");

			for (int i = 0; i < primaryKeys.size(); i++) {
				DbFieldInfo pk = primaryKeys.get(i);
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(escape).append(pk.inTableName).append(escape);
			}

			sb.append(')');
		}

		// utwórz klucze obce
		for (DbFieldInfo fk : foreignKeys) {
			DbForeignKeyInfo fkInfo = fk.getForeignKeyInfo();

			sb.append(", FOREIGN KEY (");
			sb.append(escape).append(fk.inTableName).append(escape);
			sb.append(") REFERENCES ").append(fkInfo.refTableName);
			sb.append('(').append(escape).append(fkInfo.refInTableName).append(escape).append(')');
		}

		// zakończenie zapytania
		sb.append(") ");
		sb.append(db.getDb().getCreateTableAppend());// append create table string

		// wyświelt zapytanie
		String query = sb.toString();
		db.logQuery(query);

		// pobierz istniejące połączenie lub utwórz nowe
		Connection connection = db.getConnection();

		try (Statement stmt = connection.createStatement()) {// TODO refactor
			stmt.setQueryTimeout(db.getConfig().queryTimeout);// TODO refactor
			stmt.execute(sb.toString());// wykonaj zapytanie
		} catch (SQLTimeoutException e) {
			throw new DatabaseException("SQL create table timeout!", e);
		} catch (SQLException e) {// w przypadku błędu SQL zwróc błąd
			throw new DatabaseException("SQL error when creating table for class: " + tableClass.getName(), e);
		}

		// CREATE INDEX
		List<DbFieldInfo> indexes = lister.getIndexes();

		// utwórz indeksy
		try (Statement stmt = connection.createStatement()) {// TODO refactor
			stmt.setQueryTimeout(db.getConfig().queryTimeout);// TODO refactor

			for (DbFieldInfo index : indexes) {
				String q = db.getDb().getCreateIndex();
				q = q.replace("{indexname}", escape + tableName + "_" + index.inTableName + escape);// index name
				q = q.replace("{tablename}", escape + tableName + escape);
				q = q.replace("{tablecolumn}", escape + index.inTableName + escape);

				db.logQuery(q);
				stmt.execute(q);
			}
		} catch (SQLTimeoutException e) {
			throw new DatabaseException("SQL create table timeout!", e);
		} catch (SQLException e) {// w przypadku błędu SQL zwróc błąd
			throw new DatabaseException("SQL error when creating table for class: " + tableClass.getName(), e);
		}
	}

	/**
	 * Usuwa tabele klasy encji
	 * 
	 * @param tableClass klasa encji
	 * @throws DatabaseException w przypadku błędu
	 */
	public void deleteTable(Class<?> tableClass) throws DatabaseException {
		checkAnnotationInTable(tableClass);

		char escape = db.getDb().getEscapeChar();

		// pobierz nazwę tabeli
		FieldLister lister = db.getFieldLister(tableClass);
		String tableName = lister.getTableName();

		// zapytanie
		String query = db.getDb().getDropTable().replace("{tablename}", escape + tableName + escape);
		db.logQuery(query);

		// pobierz istniejące połączenie lub utwórz nowe
		Connection connection = db.getConnection();

		try (Statement stmt = connection.createStatement()) {// TODO refactor
			stmt.setQueryTimeout(db.getConfig().queryTimeout);// TODO refactor
			stmt.execute(query);// wykonaj zapytanie
		} catch (SQLTimeoutException e) {
			throw new DatabaseException("SQL drop table timeout!");
		} catch (SQLException e) {// w przypadku błędu SQL zwróc błąd
			throw new DatabaseException("SQL error when deleting table for class: " + tableClass.getName(), e);
		}
	}

	/**
	 * Check, annotation {@link DbTable} exists in class
	 * 
	 * @param tableClass class to check
	 * @throws DatabaseException annotation not exists
	 */
	static void checkAnnotationInTable(Class<?> tableClass) throws DatabaseException {
		if (!tableClass.isAnnotationPresent(DbTable.class)) {// sprawdź, czy jest adnotacja
			throw new DatabaseException("No 'DbTable' annotation in class " + tableClass.getName());
		}
	}

}
