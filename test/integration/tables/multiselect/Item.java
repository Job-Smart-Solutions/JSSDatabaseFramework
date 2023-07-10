package integration.tables.multiselect;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

import lukas.database.annotations.DbField;
import lukas.database.annotations.DbForeignKey;
import lukas.database.annotations.DbManyToOne;
import lukas.database.annotations.DbPrimaryKey;
import lukas.database.annotations.DbTable;
import lukas.database.types.SqlType;

@Ignore
@DbTable("items")
public class Item {

	@DbPrimaryKey
	@DbField
	public long id;
	
	@DbField(canNull=true)
	public Integer countOffer;
	
	@DbField
	public int countWare;
	
	@DbField
	public String name;
	
	/*@DbField(isUnique=true) //TODO stringlen - ile?
	public String code;
	
	@DbField(isUnique=true) //TODO stringlen - ile?
	public String barcode;*/
	
	@DbField(type = SqlType.LONGTEXT)//TEXT
	public String description;
	
	@DbField
	public BigDecimal priceNet;
	
	@DbField
	public BigDecimal taxPercent;
	
	/*@DbForeignKey(refObject=Place.class, refField="id", thisField="place")
	@DbField(canNull=true)
	public Integer placeId;
	
	public Place place;*/
	
	@DbForeignKey(refObject=Category.class, refField="id", thisField="category")
	@DbField(canNull=true)
	public Integer categoryId;
	
	public Category category;
	
	@DbManyToOne(refObject=ItemPhoto.class, refField="itemId")
	public List<ItemPhoto> photos;
	
	public Item() {
		name = "";
		//code = "";
		//barcode = "";
		description="";
		
		priceNet = BigDecimal.ZERO;
		taxPercent = BigDecimal.ZERO;
		
		photos = new ArrayList<>();
	}
	
	public long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Item [id=" + id + ", countOffer=" + countOffer + ", countWare=" + countWare + ", name=" + name
				+ ", description=" + description + ", priceNet=" + priceNet + ", taxPercent=" + taxPercent
				+ ", \n categoryId=" + categoryId + ", category=" + category + ", \n photos=(" + photos.size() + ")="
				+ photos + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
