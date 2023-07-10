package lukas.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Relacja typu ManyToOne. Nie jest wykorzystywana razem z {@link DbField}!
 * Relacja ta jest przypisywana do kolecji.
 * 
 * @author lukas
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME) // dostepne w trakcie wykonywania prog
public @interface DbManyToOne {

	/**
	 * Klasa encji obcej
	 */
	Class<?> refObject();

	/**
	 * Nazwa pola z którego występuje relacja z klasy obcej, do klasy obecnej (tej w
	 * której jest kolekcja).
	 */
	String refField();
}
