package jss.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for database view. Views are only for SELECT query.
 * 
 * @author lukas
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME) // dostepne w trakcie wykonywania prog
public @interface DbView {

	/**
	 * View name
	 */
	String name();

	/**
	 * Query for view
	 */
	String query();

	/**
	 * Preparing classes for view
	 */
	Class<?>[] preparingClases() default {};

	/**
	 * Preparing classes for view
	 */
	Class<?>[] skipClases() default {};

	/**
	 * Simulate view only (do not CREATE VIEW)
	 */
	boolean simulate() default false;

}
