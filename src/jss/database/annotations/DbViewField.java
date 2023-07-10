package jss.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FIELD in view
 * 
 * @author lukas
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME) // dostepne w trakcie wykonywania prog
public @interface DbViewField {

	/**
	 * Nazwa pola w tabeli, jeżeli puste (domyślnie), to pole przyjmie taką samą
	 * nazwę jak w klasie.
	 */
	String value() default "";
}
