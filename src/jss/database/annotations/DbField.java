package jss.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jss.database.types.SqlType;

/**
 * Adnotacja dla pola w tabeli. Należy dodać ją do pola, które będzie
 * reprezentowane w bazie danych. <br>
 * <br>
 * 
 * <b>Obsługiwane są następujące typy pól danych:</b> <br>
 * <ul>
 * <li>byte, short, int, long (oraz osłonowe: Byte, Short, Integer, Long)</li>
 * <li>float, double (oraz klasy osłonowe: Float, Double)</li>
 * <li>boolean, char (oraz klasy osłonowe: Boolean, Character)</li>
 * <li>char[] (jako obiekt CLOB lub TEXT)</li>
 * <li>byte[] (jako obiekt BLOB)</li>
 * <li>java.lang.String</li>
 * <li>java.util.Date</li>
 * <li>java.sql.Date</li>
 * <li>java.sql.Time</li>
 * <li>java.sql.Timestamp</li>
 * <li>java.math.BigInteger</li>
 * <li>java.math.BigDecimal</li>
 * </ul>
 * 
 * @author lukas
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME) // dostepne w trakcie wykonywania prog
public @interface DbField {

	/**
	 * Nazwa pola w tabeli, jeżeli puste (domyślnie), to pole przyjmie taką samą
	 * nazwę jak w klasie.
	 */
	String name() default "";

	/**
	 * Czy pole może przyjmować wartości NULL. Domyślnie nie.
	 */
	boolean canNull() default false;

	/**
	 * Czy pole jest unikatowe? Domyślnie nie.
	 */
	boolean isUnique() default false;

	/**
	 * Czy jest to index? Domyślnie nie.
	 */
	boolean isIndex() default false;

	/**
	 * Typ pola w bazie danych, domyślnie automatycznie wybierany
	 */
	SqlType type() default SqlType.DEFAULT;

	/**
	 * Limit ilości znaków dla pola VARCHAR, zastosowanie tylko dla klasy String.
	 * 
	 * Wartość -1 (domyślnie) - wartość przekazana w konfiguracji.
	 */
	int stringLen() default -1;

	/**
	 * Maksymalna ilość liczb przed przecinkiem w polu DECIMAL, zastosowanie tylko
	 * dla klasy BigInteger i BigDecimal.
	 * 
	 * Wartość -1 (domyślnie) - wartość przekazana w konfiguracji.
	 */
	int decimalPrecision() default -1;

	/**
	 * Ilość liczb po przecinku w pole DECIMAL, zastosowanie tylko dla klasy
	 * BigDecimal.
	 * 
	 * Wartość -1 (domyślnie) - wartość przekazana w konfiguracji.
	 */
	int decimalScale() default -1;

}
