package jss.database;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import jss.database.DatabaseConfig;
import jss.database.DatabaseException;
import jss.database.annotations.DbTable;

/**
 * @author lukas
 * Testing database configuration
 */
public class DatabaseConfigTest {

	/**
	 * Should add if correct class
	 */
	@Test
	public void testShouldAddCorrectClass() throws DatabaseException {
		//given
		DatabaseConfig cfg = new DatabaseConfig(null);
		//when
		DatabaseConfig cfgRet = cfg.addTableClass(ClassCorrect.class);
		//then (should no throw exception)
		assertSame(cfg, cfgRet);
	}
	
	/**
	 * Should thrown exception if incorrect class
	 */
	@Test
	public void testShouldNotAddIncorrectClass() {
		//given
		DatabaseConfig cfg = new DatabaseConfig(null);
		//when/then
		assertThrows(DatabaseException.class, () -> cfg.addTableClass(ClassIncorrect.class));
	}
	
	@DbTable("correct_class")
	private class ClassCorrect { }//correct class - annotation exists
	
	private class ClassIncorrect { }//incorrect class - no annotation
	
}
