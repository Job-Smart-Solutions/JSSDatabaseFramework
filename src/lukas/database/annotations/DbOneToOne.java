package lukas.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Relacja typu OneToOne. Nie jest wykorzystywana razem z {@link DbField}!
 * Relacja ta jest przypisywana do pojedyńczego pola w klasie.
 * 
 * @author lukas
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME) // dostepne w trakcie wykonywania prog
public @interface DbOneToOne {

	/**
	 * Klasa encji obcej
	 */
	Class<?> refObject();

	/**
	 * Nazwa pola z którego występuje relacja z klasy obcej, do klasy obecnej.
	 */
	String refField();
}
