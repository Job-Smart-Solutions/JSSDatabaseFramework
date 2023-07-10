package jss.database;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

import jss.database.annotations.DbTable;
import jss.database.annotations.DbView;
import jss.database.mappers.DefaultSqlTypeMapper;
import jss.database.mappers.SqlTypeMapper;
import jss.database.types.DatabaseType;

/**
 * Konfiguracja bazy danych, obowiązkowa do przekazania do klasy.
 * 
 * Oprócz konfiguracji zawiera liste klas encji opisujących tabele w bazie
 * danych.
 * 
 * @author lukas
 * @see Database
 */
public class DatabaseConfig {

	// typ bazy danych
	DatabaseType dbType;

	// konfiguracja
	boolean createTables = false;// twórz tabelę
	boolean createViews = false;// twórz widoki
	int defaultStringLen = 255;
	int defaultDecimalPrecision = 15;
	int defaultDecimalScale = 2;
	int queryTimeout = 20;
	boolean autoReconnect = true;

	// mapowanie
	SqlTypeMapper sqlTypeMapper = new DefaultSqlTypeMapper();

	// autoreconnecting
	int autoReconnectInactiveSecs = -1;// autopołączanie po czasie nieaktywności
	int autoReconnectAfterLastConnectionSecs = -1;// autopoł. po czasie od nawiązania ost. poł.

	// logowanie
	boolean logQueries = false;// loguj zapytania
	Level logQueriesLevel = Level.FINEST;// poziom logowania zapytań

	// konfiguracja połączenia
	String server;
	int port = -1;
	String dbName;
	String user;
	String pass;

	// zbiór klas zawierajacy adnotacje DbTable
	Set<Class<?>> tableClasses = new LinkedHashSet<Class<?>>();

	// zbiór klas zawierajacy adnotacje DbView
	Set<Class<?>> viewClasses = new LinkedHashSet<Class<?>>();

	/**
	 * Konstruktor konfiguracji
	 * 
	 * @param dbType typ bazy danych
	 * @see DatabaseType
	 */
	public DatabaseConfig(DatabaseType dbType) {
		this.dbType = dbType;
	}

	/**
	 * Czy logować zapytania SQL?
	 * 
	 * @param logQueries should log SQL queries?
	 * @return this object
	 */
	public DatabaseConfig setLogQueries(boolean logQueries) {
		this.logQueries = logQueries;
		return this;
	}

	/**
	 * Poziom logowania zaytań SQL
	 * 
	 * @param logQueriesLevel log SQL queries level
	 * @return this object
	 */
	public DatabaseConfig setLogQueriesLevel(Level logQueriesLevel) {
		this.logQueriesLevel = logQueriesLevel;
		return this;
	}

	/**
	 * Domyślny limit ilości znaków dla pola VARCHAR. Wartości 1...2048 - ilość
	 * znaków w polu VARCHAR. Wartość 0 i wartości powyżej 2048 - pole TEXT.
	 * 
	 * @param defaultStringLen domyślny limit znaków
	 * @return this object
	 */
	public DatabaseConfig setDefaultStringLen(int defaultStringLen) {
		this.defaultStringLen = defaultStringLen;
		return this;
	}

	/**
	 * Domyślna precyzja w polu DECIMAL.
	 * 
	 * @param defaultDecimalPrecision domyślna precyzja
	 * @return this object
	 */
	public DatabaseConfig setDefaultDecimalPrecision(int defaultDecimalPrecision) {
		this.defaultDecimalPrecision = defaultDecimalPrecision;
		return this;
	}

	/**
	 * Domyślna ilość liczb po przecinku w polu DECIMAL.
	 * 
	 * @param defaultDecimalScale ilośc liczb po przecinku
	 * @return this object
	 */
	public DatabaseConfig setDefaultDecimalScale(int defaultDecimalScale) {
		this.defaultDecimalScale = defaultDecimalScale;
		return this;
	}

	/**
	 * Ustawia timeout dla zapytań SQL.
	 * 
	 * @param seconds ilość sekund timeoutu
	 * @return this object
	 */
	public DatabaseConfig setQueryTimeout(int seconds) {
		this.queryTimeout = seconds;
		return this;
	}

	/**
	 * Ustawia czy baza danych ma ponawiać połączenie automatycznie po zerwaniu
	 * 
	 * @param autoReconnect czy ponawiać połączenie automatycznie po zerwaniu?
	 * @return this object
	 */
	public DatabaseConfig setAutoReconnect(boolean autoReconnect) {
		this.autoReconnect = autoReconnect;
		return this;
	}

	/**
	 * Ustawia czas automatycznego nawiązania nowego połączania po czasie
	 * nieaktywności. Połączenie zostanie nawiązane przy dowolnej operacji na bazie
	 * danych. Jeżeli ustawiono ten parametrt razem z
	 * {@link #setAutoReconnectAfterLastConnectionMs(int)} zostanie on zignorowany.
	 * 
	 * @param autoReconnectInactiveMs czas nieaktywności w sekundach który musi
	 *                                minąć by nawiązać nowe połączenie, lub -1
	 *                                pomija
	 * @return this object
	 */
	public DatabaseConfig setAutoReconnectInactiveSecs(int autoReconnectInactiveSecs) {
		this.autoReconnectInactiveSecs = autoReconnectInactiveSecs;
		return this;
	}

	/**
	 * Ustawia czas automatycznego nawiązania nowego połączania po czasie od
	 * nawiązania ostatniego połączenia. Połączenie zostanie nawiązane przy dowolnej
	 * operacji na bazie danych. Jeżeli ustawiono ten parametr, zostanie zignorowany
	 * parametr ({@link #setAutoReconnectInactiveMs(int)}.
	 * 
	 * @param autoReconnectAfterLastConnectionMs czas w sekundach który musi minąć
	 *                                           od ost. poł., by nawiązać nowe, lub
	 *                                           -1 pomija
	 * @return this object
	 */
	public DatabaseConfig setAutoReconnectAfterLastConnectionSecs(int autoReconnectAfterLastConnectionSecs) {
		this.autoReconnectAfterLastConnectionSecs = autoReconnectAfterLastConnectionSecs;
		return this;
	}

	/**
	 * Ustawia, czy mają być tworzone tabele po połączeniu (jeżeli nie istnieją).
	 * Domyślnie tabele NIE są tworzone po połączeniu.
	 * 
	 * @param createTables czy mają być tworzone tabele po połączeniu (jeżeli nie
	 *                     istnieją)
	 * @return this object
	 */
	public DatabaseConfig setCreateTables(boolean createTables) {
		this.createTables = createTables;
		return this;
	}

	/**
	 * Ustawia adres serwera bazy danych. Nieistotne w przypadku bazy SQLite.
	 * 
	 * @param server Adres serwera bazy
	 * @return this object
	 */
	public DatabaseConfig setServer(String server) {
		this.server = server;
		return this;
	}

	/**
	 * Ustawia port bazy danych. Wartość -1 oznacza, iż zostanie wybrany domyślny
	 * port dla danego typu bazy. Nieistotne w przypadku SQLite.
	 * 
	 * @param port Port bazy danych (lub -1 dla domyślnego portu bazy danego typu)
	 * @return this object
	 */
	public DatabaseConfig setPort(int port) {
		this.port = port;
		return this;
	}

	/**
	 * Ustawia nazwę bazy danych która zostanie wybrania po nawiązaniu połączenia.
	 * Nieistotne w przypadku SQLite. UWAGA: Baza powinna posiadać odpowiednie
	 * kodowanie znaków!
	 * 
	 * @param dbName Nazwa bazy danych
	 * @return this object
	 */
	public DatabaseConfig setDbName(String dbName) {
		this.dbName = dbName;
		return this;
	}

	/**
	 * Nazwa użytkownika bazy danych. Nieistotne w przypadku SQLite.
	 * 
	 * @param user Nazwa użytkownika
	 * @return this object
	 */
	public DatabaseConfig setUser(String user) {
		this.user = user;
		return this;
	}

	/**
	 * Hasło użytkownika bazy danych. Nieistotne w przypadku SQLite.
	 * 
	 * @param pass Hasło użytkownika
	 * @return this object
	 */
	public DatabaseConfig setPass(String pass) {
		this.pass = pass;
		return this;
	}

	/**
	 * Dodaje do konfiguracji klasę encji, która reprezentuje tabele w bazie danych.
	 * Na podstawie dodanych tutaj klas tworzone są tabele w bazie danych. Klasa
	 * powinna posiadać adnotacje {@link DbTable}. Należy je dodawać w takiej
	 * kolejności w jakiej mają być utworzone!
	 * 
	 * @param tableClass entity class
	 * @return this object
	 * @throws DatabaseException gdy brak adnotacji {@link DbTable} w klasie
	 */
	public DatabaseConfig addTableClass(Class<?> tableClass) throws DatabaseException {
		if (!tableClass.isAnnotationPresent(DbTable.class)) {// sprawdź, czy jest adnotacja
			throw new DatabaseException("No 'DbTable' annotation in class: " + tableClass.getName());
		}

		tableClasses.add(tableClass);
		return this;
	}

	/**
	 * Dodaje do konfiguracji klasę widoku VIEW.
	 * 
	 * @param viewClass view class
	 * @return this object
	 * @throws DatabaseException gdy brak adnotacji {@link DbView} w klasie
	 */
	public DatabaseConfig addViewClass(Class<?> viewClass) throws DatabaseException {
		if (!viewClass.isAnnotationPresent(DbView.class)) {// sprawdź, czy jest adnotacja
			throw new DatabaseException("No 'DbView' annotation in class: " + viewClass.getName());
		}

		viewClasses.add(viewClass);
		return this;
	}

}
