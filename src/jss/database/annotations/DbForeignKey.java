package jss.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jss.database.DatabaseException;

/**
 * Klucz obcy w tabeli. Używane razem z {@link DbField}
 * 
 * @author lukas
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME) // dostepne w trakcie wykonywania prog
public @interface DbForeignKey {

	/**
	 * Klasa encji obcej
	 */
	Class<?> refObject();

	/**
	 * Nazwa pola do którego występuje relacja w klasie obcej
	 */
	String refField();

	/**
	 * Nazwa pola w obecnej klasie, do którego można zapisać pobrany obiekt. Jeżeli
	 * puste, klucz obcy nie będzie skojarzony z żadnym obiektem w klasie, jednak
	 * wtedy próba załadowania obiektu obcego zakończy się wyjątkiem
	 * {@link DatabaseException}.
	 */
	String thisField() default "";
}
