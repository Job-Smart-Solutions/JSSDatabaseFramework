package integration.tables;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Ignore;

import lukas.database.annotations.DbField;
import lukas.database.annotations.DbPrimaryKey;
import lukas.database.annotations.DbTable;

@Ignore
@DbTable("decimal_table")
public class DecimalTable {

	@DbPrimaryKey
	@DbField//cannot be null
	private Integer id;//test if id is an nullable object, but cannot be null
	
	//big integer
	@DbField
	public BigInteger bi;
	
	@DbField(canNull = true)
	public BigInteger biNullable;
	
	@DbField(decimalPrecision = 5)
	public BigInteger biPrec;
	
	@DbField(decimalPrecision = 8, canNull = true)
	public BigInteger biPrecNullable;
	
	//big decimal
	@DbField
	public BigDecimal bd1;
	
	@DbField(canNull = true)
	public BigDecimal bdNullable;
	
	@DbField(decimalPrecision = 5)
	public BigDecimal bdPrec;
	
	@DbField(decimalPrecision = 8, canNull = true)
	public BigDecimal bdPrecNullable;
	
	@DbField(decimalScale = 4)
	public BigDecimal bdScale;
	
	@DbField(decimalScale = 6, canNull = true)
	public BigDecimal bdScaleNullable;
	
	@DbField(decimalPrecision = 9, decimalScale = 4)
	public BigDecimal bdPrecScale;
	
	@DbField(decimalPrecision = 7, decimalScale = 3, canNull = true)
	public BigDecimal bdPrecScaleNullable;
}
