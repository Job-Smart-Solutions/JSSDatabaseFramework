package lukas.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import lukas.database.types.AbstractDatabase;
import lukas.database.types.DatabaseFactory;

/**
 * Database framework
 * 
 * @author lukas
 */
public class Database {
	private final DatabaseConfig config; // database config
	private final AbstractDatabase db;// database type
	private final Logger log;// logger

	private final Map<Class<?>, FieldLister> fieldListers = new HashMap<>();// field listers cache

	private Connection connection; // connection with database
	private boolean inTransaction;// czy aktualnie trwa transakcja?

	// timers
	private long lastConnectedTime;// time of last connetion established
	private long lastActiveTime;// time of last activity

	public Database(DatabaseConfig config) {
		this.config = config;
		this.db = DatabaseFactory.getDatabase(config.dbType);

		// logger
		this.log = Logger.getLogger(getClass().getName());
	}

	/**
	 * Tworzy połączenie z bazą danych.
	 * 
	 * Jeżeli w konfiguracji ustawiono tworzenie tabel, zostaną one utworzone.
	 * 
	 * @throws DatabaseException driver error, connection error, create table error
	 */
	public void connect() throws DatabaseException {
		closeQuietly();// usuń cache i zakończ ewentualne połączenie

		// load database driver class
		try {
			Class.forName(db.getDriver());
		} catch (ClassNotFoundException e) {// w przypadku braku sterownika rzuć wyjątek
			throw new DatabaseException("No database driver: " + config.dbType, e);
		}

		// connection parameters
		Properties prop = createDbConnProperties();

		// get connection url
		String connectionUrl = createConnectionUrl();

		// connect to database
		try {
			DriverManager.setLoginTimeout(config.queryTimeout);
			connection = DriverManager.getConnection(connectionUrl, prop);
		} catch (SQLException e) {// w przypadku błędu rzuć wyjątek
			throw new DatabaseException("Error when connecting to database! " + e.getMessage(), e);
		}

		lastConnectedTime = getUnixTimestamp();
		lastActiveTime = getUnixTimestamp();

		// tworzenie tabel, jeżeli tak ustawiono w konfiguracji
		if (config.createTables) {
			createTablesIfNotExists();
		}
		// tworzenie widoków, jeżeli tak ustawiono w konfiguracji
		if (config.createViews) {
			createViewsIfNotExists();
		}
	}

	/**
	 * Tworzy nową bazę danych na serwerze
	 * 
	 * @param dbname nazwa bazy danych
	 * @throws DatabaseException w przypadku błędu tworzenia bazy danych
	 */
	private void createDatabase(String dbname) throws DatabaseException {
		checkMustReconnect();

		try (Statement stmt = connection.createStatement()) {
			stmt.setQueryTimeout(config.queryTimeout);

			String create = db.getCreateDatabase().replace("{DBNAME}", dbname);
			logQuery(create);

			stmt.execute(create);
			stmt.close();
		} catch (SQLTimeoutException e) {
			throw new DatabaseException("Timeout when creating database!");
		} catch (SQLException e) {
			throw new DatabaseException("SQL error when creating database: " + e.getMessage(), e);
		}
	}

	/**
	 * Tworzy nową bazę danych na serwerze i nawiązuje z nią połączenie. Nazwa bazy
	 * danych przekazywana jest w konfiguracji
	 * 
	 * @throws DatabaseException error
	 */
	public void createDatabaseAndConnect() throws DatabaseException {
		createDatabaseAndConnect(config.dbName);
	}

	/**
	 * Tworzy nową bazę danych o podanej nazwie na serwerze i nawiązuje z nią
	 * połączenie.
	 * 
	 * @param dbname nazwa bazy danych
	 * @throws DatabaseException error
	 */
	public void createDatabaseAndConnect(String dbname) throws DatabaseException {
		config.dbName = "";// current dbName

		// przy tworzeniu bazy danych nie tworzymy tabel ani widoków
		boolean createTables = config.createTables;
		boolean createViews = config.createViews;
		config.createTables = false;
		config.createViews = false;

		connect();
		createDatabase(dbname);
		close();

		// tworzymy nowe połączenie by wybrać bazę danych
		config.dbName = dbname;// utworzona baza
		config.createTables = createTables;// przywróć poprzednie ustawienia tworzenia tabel
		config.createViews = createViews;// przywróc poprzednie ustawienia tworzenia widoków
		connect();
	}

	/**
	 * Kończy połączenie z bazą danych
	 * 
	 * @throws DatabaseException w przypadku błędu zakończenia połączenia
	 */
	public void close() throws DatabaseException {
		// brak transakcji
		inTransaction = false;

		try {
			if (connection != null && (!connection.isClosed())) {
				connection.close();// zakończ połączenie
			}
		} catch (SQLException e) {// w przypadku błędu rzuć wyjątek
			throw new DatabaseException("Error when closing database connection: " + e.getMessage(), e);
		} finally {
			connection = null;// usuń obiekt połączenia
		}
	}

	/**
	 * Kończy połączenie z ignorowaniem błędów
	 */
	public void closeQuietly() {
		try {
			close();
		} catch (DatabaseException e) {
			// ignore closing exception, only log
			log().log(Level.INFO, "Error when closing database connection: " + e.getMessage(), e);
		}
	}

	/**
	 * Czy jest połączenie z bazą danych?
	 */
	public boolean isConnected() {
		if (connection == null)
			return false;
		else {
			try {
				return !connection.isClosed();
			} catch (SQLException e) {
				return false;
			}
		}
	}

	/**
	 * Rozpoczęcie transakcji
	 * 
	 * @throws DatabaseException błąd rozpoczęcia transakcji
	 */
	public void beginTransaction() throws DatabaseException {
		checkMustReconnect();// sprawdź, czy przed transakcją nie jest wymagane wznowienie połączenia

		try {
			connection.setAutoCommit(false);
			inTransaction = true;
		} catch (SQLException e) {
			throw new DatabaseException("DB: BEGIN TRANSACTION error! " + e.getMessage(), e);
		}
	}

	/**
	 * Potwierdzenie transakcji i wyłączenie trybu transakcyjnego
	 * 
	 * @throws DatabaseException błąd wycofywania transakcji lub błąd wyłączania
	 *                           trybu transakcyjnego
	 */
	public void commit() throws DatabaseException {
		try {
			connection.commit();
		} catch (SQLException e) {
			throw new DatabaseException("DB: COMMIT error! " + e.getMessage(), e);
		}

		disableTransactionMode();
	}

	/**
	 * Wycofanie transakcji i wyłączenie trybu transakcyjnego
	 * 
	 * @throws DatabaseException błąd wycofywania transakcji lub błąd wyłączania
	 *                           trybu transakcyjnego
	 */
	public void rollback() throws DatabaseException {
		try {
			connection.rollback();
		} catch (SQLException e) {
			throw new DatabaseException("DB: ROLLBACK error! " + e.getMessage(), e);
		}

		disableTransactionMode();
	}

	/**
	 * Wyłączenie trybu transakcyjnego
	 * 
	 * @throws DatabaseException błąd wyłączania trybu transakcyjnego
	 */
	private void disableTransactionMode() throws DatabaseException {
		try {
			inTransaction = false;
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			throw new DatabaseException("DB autocommit enabling error!  + e.getMessage()", e);
		}
	}

	/**
	 * Restartuje połączenie z bazą danych
	 * 
	 * @throws DatabaseException w przypadku błędu połączenia
	 */
	public void reconnect() throws DatabaseException {
		log().info("Database: Reconnecting...");
		closeQuietly();
		connect();
	}

	/**
	 * Sprawdza, czy należy nawiązać nowe połączenie, oraz resetuje czas od
	 * ostatniej aktywności
	 * 
	 * @throws DatabaseException w przypadku błędu ponawiania połączenia
	 */
	void checkMustReconnect() throws DatabaseException {
		if (inTransaction) {
			return;// do nothing, when is in transaction!
		}

		long currentTimestamp = getUnixTimestamp();

		if (config.autoReconnectAfterLastConnectionSecs > 0) {
			long diff = currentTimestamp - lastConnectedTime;

			if (diff >= config.autoReconnectAfterLastConnectionSecs) {
				reconnect();
			}
		} else if (config.autoReconnectInactiveSecs > 0) {
			long diff = currentTimestamp - lastActiveTime;

			if (diff >= config.autoReconnectInactiveSecs) {
				reconnect();
			}
		}

		// resetuj czas nieaktywności (reset inactive timer)
		lastActiveTime = getUnixTimestamp();
	}

	/**
	 * Get table manager for creating and deleting table
	 * 
	 * @return table manager
	 */
	public TableManager getTableManager() {
		return new TableManager(this);
	}

	/**
	 * Get view manage for creating and deleting views
	 * 
	 * @return view manager
	 */
	public ViewManager getViewManager() {
		return new ViewManager(this);
	}

	/**
	 * Get data saver for saving and deleting data
	 * 
	 * @return data saver
	 */
	public DataSaver getDataSaver() {
		return new DataSaver(this);
	}

	/**
	 * Zwraca napis do obsługi limitów pobranych obiektów we własnych zapytaniach.
	 * Należy w nim zmienić {offset} na offset i {rowcount} na ilość pobieranych
	 * wierszy.
	 * 
	 * @return limit string
	 */
	public String getLimitString() {
		return db.getLimit();
	}

	/**
	 * Tworzy obiekt do obsługi prostych zapytań SELECT
	 * 
	 * @param from klasa encji do pobrania
	 * @return obiekt od obsługi prostych zapytań SELECT
	 */
	public <T> SelectQuery<T> createSelectQuery(Class<T> from) {
		return new SelectQuery<T>(this, from);
	}

	/**
	 * Tworzy obiekt do obsługi własnych zapytań SELECT
	 * 
	 * @return obiekt od obsługi własnych zapytań SELECT
	 */
	public OwnSelectQuery createOwnSelectQuery() {
		return new OwnSelectQuery(this);
	}

	/**
	 * Tworzy obiekt do obsługi własnych zapytań innych niż select
	 * 
	 * @return obiekt do obsługi własnych zapytań
	 */
	public OwnQuery createOwnQuery() {
		return new OwnQuery(this);
	}

	/**
	 * Tworzy obiekt do obsługi własnego zapytania SELECT ze zwracaniem utworzonych
	 * pełnych obiektów
	 * 
	 * @param from klasa encji głównej
	 * @return obiekt do obsługi zapytań
	 */
	public <T> MultiSelectQuery<T> createMultiSelectQuery(Class<T> from) {
		return new MultiSelectQuery<T>(this, from);
	}

	/**
	 * Tworzy obiekt do obsługi zapytania SELECT dla widoku (VIEW)
	 * 
	 * @param from klasa widoku
	 * @return obiekt do obsługi zapytania dla widoku
	 * @throws DatabaseException
	 */
	public <T> ViewQuery<T> createViewQuery(Class<T> from) throws DatabaseException {
		return new ViewQuery<>(this, from);
	}

	/**
	 * Create tables if not exists
	 * 
	 * @throws DatabaseException creating table error
	 */
	public void createTablesIfNotExists() throws DatabaseException {
		getTableManager().createTables(config.tableClasses);
	}

	/**
	 * Create view if not exists
	 * 
	 * @throws DatabaseException creating view error
	 */
	public void createViewsIfNotExists() throws DatabaseException {
		getViewManager().createViews(config.viewClasses);
	}

	/**
	 * @return connection with database (or null, if not connected)
	 * @throws DatabaseException error when reconnecting
	 */
	Connection getConnection() throws DatabaseException {
		if (connection == null) {
			throw new DatabaseException("Not connected!");
		}

		checkMustReconnect();
		return connection;
	}

	/**
	 * @return database config
	 */
	DatabaseConfig getConfig() {
		return config;
	}

	/**
	 * @return database type
	 */
	AbstractDatabase getDb() {
		return db;
	}

	/**
	 * Get logger
	 */
	Logger log() {
		return log;
	}

	/**
	 * Log query
	 */
	void logQuery(String query) {
		log.info(query);
	}

	/**
	 * Get field lister for specified class
	 * 
	 * @throws DatabaseException
	 */
	FieldLister getFieldLister(Class<?> clazz) throws DatabaseException {
		if (!fieldListers.containsKey(clazz)) {// not found field lister - create
			FieldLister lister = new FieldLister(this, clazz);
			fieldListers.put(clazz, lister);
		}

		return fieldListers.get(clazz);
	}

	/**
	 * Create connection URL for database
	 */
	private String createConnectionUrl() {
		String connectionUrl;

		if (config.dbName != null) {// with database
			connectionUrl = db.getConnectionStringWithDatabase();
			connectionUrl = connectionUrl.replace("{DBNAME}", config.dbName);
		} else {// without database
			connectionUrl = db.getConnectionString();
		}
		connectionUrl = connectionUrl.replace("{SERVER}", config.server);// server

		// port
		if (config.port == -1) {
			connectionUrl = connectionUrl.replace(":{PORT}", "");// usuń całkowicie port, sterownik wybierze sam
		} else {
			connectionUrl = connectionUrl.replace("{PORT}", Integer.toString(config.port));
		}

		return connectionUrl;
	}

	/**
	 * Create database connection properties
	 */
	private Properties createDbConnProperties() {
		Properties prop = new Properties();

		if (config.user != null) {// auth parameters
			prop.put("user", config.user);
			prop.put("password", config.pass);
		}

		prop.put("connectTimeout", config.queryTimeout);
		prop.put("autoReconnect", config.autoReconnect);

		db.setPropertiesForConnection(prop);

		return prop;
	}

	/**
	 * UNIX time stamp
	 */
	public static long getUnixTimestamp() {
		return System.currentTimeMillis() / 1000L;
	}

}
