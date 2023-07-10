package jss.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for database table. Must be in entity class!
 * 
 * @author lukas
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME) // dostepne w trakcie wykonywania prog
public @interface DbTable {

	/**
	 * Table name
	 */
	String value();
}
