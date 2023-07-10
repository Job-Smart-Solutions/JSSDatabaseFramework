package lukas.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Klucz podstawowy w tabeli. Używane razem z {@link DbField}.
 * 
 * @author lukas
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME) // dostepne w trakcie wykonywania prog
public @interface DbPrimaryKey {

	/**
	 * Czy autonumerować klucz? Domyślnie tak. Przy kluczu złożonym z wielu pól ta
	 * opcja jest ignorowana, i żadne pole nie jest autonumerowane.
	 */
	boolean autoIncrement() default true;
}
