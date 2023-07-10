package jss.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Object in VIEW.
 * 
 * @author lukas
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME) // dostepne w trakcie wykonywania prog
public @interface DbViewObject {

	/**
	 * Klasa encji obcej
	 */
	Class<?> value();
}
