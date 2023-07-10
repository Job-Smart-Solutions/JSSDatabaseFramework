package integration.tables.multiselect;
import org.junit.Ignore;

import jss.database.annotations.DbField;
import jss.database.annotations.DbForeignKey;
import jss.database.annotations.DbPrimaryKey;
import jss.database.annotations.DbTable;

/**
 * @author lukas
 * Place in warehose
 * Miejsce w magazynie
 */
@Ignore
@DbTable("places")
public class Place  {

	@DbPrimaryKey
	@DbField
	private int id;
	
	@DbForeignKey(refObject=Place.class, refField="id", thisField="parent")
	@DbField(canNull=true)
	private Integer parentId;
	
	private Place parent;
	
	@DbField
	private String name;
	
	@DbField(stringLen=15)
	private String number;
	
	@DbField(isUnique=true) //TODO stringlen - ile?
	private String barcode;//inner barcode for scanner
	
	
	public Place() {
		name="";
		number="";
		barcode="";
	}

	/**
	 * ID in database
	 */
	public int getId() {
		return id;
	}

	/**
	 * Place name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set place name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get number or symbol. 
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Set number or symbol. Max 15 chars.
	 */
	public void setNumber(String number) {
		//TODO wyjątek gdy powyżej 15 znaków!
		this.number = number;
	}

	/**
	 * Barcode for scanner.
	 */
	public String getBarcode() {
		return barcode;
	}

	/**
	 * Set barcode for scanner. Must be unique!
	 */
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}


	/**
	 * Parent ID (in database)
	 */
	public Integer getParentId() {
		return parentId;
	}
	
	/**
	 * Get parent place object
	 */
	public Place getParent() {
		return parent;
	}

	/**
	 * Set parent place object
	 */
	public void setParent(Place parent) {
		this.parent = parent;
		if(parent!=null) {
			this.parentId = parent.getId();
		} else {
			this.parentId = null;
		}
	}

}