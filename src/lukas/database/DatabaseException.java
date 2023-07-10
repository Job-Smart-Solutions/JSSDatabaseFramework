package lukas.database;

/**
 * Wyjątek dotyczący obsługi bazy danych. Może być utworzony tylko z klas
 * pakietu 'database'. Może wystąpić podczas połączenia, braku sterownika bazy,
 * wykonywania zapytania SQL, braku odpowiednich adnotacji w klasie, lub innych
 * czynności.
 * 
 * @author lukas
 */
public class DatabaseException extends Exception {
	private static final long serialVersionUID = -8122843157962617759L;

	/**
	 * Konstruktor z wiadomością
	 * 
	 * @param message wiadomość
	 */
	public DatabaseException(String message) {
		super(message);
	}

	/**
	 * Konstruktor z wiadomością, oraz poprzednim wyjątkiem
	 * 
	 * @param message wiadomość
	 * @param inner   poprzedni wyjątek
	 */
	public DatabaseException(String message, Throwable inner) {
		super(message, inner);
	}

}
